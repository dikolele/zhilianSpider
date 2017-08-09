package main.org.lele.network;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

/**
 * 单例
 * http连接池 管理所有的http连接
 */
public class MultiplexingHttpConectionPool {
	private static MultiplexingHttpConectionPool connPool = new MultiplexingHttpConectionPool();
	private PoolingHttpClientConnectionManager cmr = null ; //管理
	private SocketConfig sockConf = null ;//套接字配置
	private RequestConfig reqConf = null ;//请求配置
	private HttpClientBuilder clientBuilder = null ;//生成器
	private Thread idleConnMngr ;//闲置连接器管理
	
	//构造函数，初始化各项参数
	private MultiplexingHttpConectionPool(){
		initialize(5000,1000,5000,5000,5000) ;
	}
	
	//取到实例
	public static MultiplexingHttpConectionPool getInstance(){
		return connPool ;
	}
	
	/**
	 * 初始化连接池各项参数
	 * @param cmrConnMaxTotal http连接池大小（个数）
	 * @param cmrDefaultMaxPerRoute 每条路由连接上限
	 * @param connRequestTimeout 连接请求的超时时间（毫秒）
	 * @param connTimeout 连接超时时间（毫秒）
	 * @param socketTimeout 底层socket超时时间（毫秒）
	 */
	public void initialize(int cmrConnMaxTotal , int cmrDefaultMaxPerRoute ,
		int connRequestTimeout,int connTimeout,int socketTimeout){
		cmr = new PoolingHttpClientConnectionManager() ;
		cmr.setMaxTotal(cmrConnMaxTotal);//http连接池大小（个数）
		cmr.setDefaultMaxPerRoute(cmrDefaultMaxPerRoute);//每条路由连接上限
		
		idleConnMngr = new IdleConnectionMonitorTask(cmr) ;//管理空连接和超时连接
		idleConnMngr.start();
		
		sockConf = 
				SocketConfig.custom().setSoReuseAddress(true)//设置是否可在一个进程关闭socket后，即使//端口未被其释放，其它进程还可立刻对该端口重用
				.setTcpNoDelay(true)//是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
		        .setSoLinger(0)//关闭Socket时，要么发送完所有数据，要么等待0s后，就关闭连接，此时socket.close()是阻塞的
		        .build();
		
		reqConf = 
				RequestConfig.custom().setConnectionRequestTimeout(connRequestTimeout)//设置请求连接超时时间
				.setConnectTimeout(connTimeout)//连接超时时间
				.setSocketTimeout(socketTimeout)//等待数据的超时时间
				.build();
		clientBuilder = 
				HttpClients.custom()
				.setDefaultSocketConfig(sockConf)//默认socket设置
				.setDefaultRequestConfig(reqConf)//默认请求配置
				.setConnectionManager(cmr)//设置连接管理器
				.setRetryHandler(new RetryHandler());//重试策略
	}
	
	/**
	 * 关闭连接池
	 * @throws InterruptedException
	 */
	@SuppressWarnings("deprecation")
	public void uninitialize(){
		cmr.close();//关闭这个流并释放任何与其有关的资源
		cmr.shutdown();//关闭连接管理器并释放资源，关闭所有的连接
		idleConnMngr.stop();
	}
	
	/** 
	 * 获取一个长连接或短连接
	 * @param keepAlive true,返回一个长连接；false 返回一个短连接
	 * @return CloseableHttpClient 类型实例
	 */
	public CloseableHttpClient getConnection(boolean keepAlive){
		CloseableHttpClient client = null ;
		if(keepAlive){
			client = clientBuilder.setKeepAliveStrategy(new KeepAliveStrategy()).build();
		}else{
			client = clientBuilder.build();
		}
		return client ;
		}
	
	/**
	 * 这个handler决定如果发生异常是否重试连接
	 */
	private class RetryHandler implements HttpRequestRetryHandler{
		@SuppressWarnings("unused")
		public boolean retryRequest(IOException exception,int executionCount,HttpClientContext context){
			if(executionCount >= 1){//重试次数，暂时写死
				return false ;
			}
			//instanceof测试它左边的对象是否是它右边的类的实例，返回boolean类型的数据
			if(exception instanceof InterruptedIOException){
				return false ;
			}
			if(exception instanceof UnknownHostException){
				return false ;
			}
			if(exception instanceof ConnectTimeoutException){
				return false ;
			}
			if(exception instanceof SSLException){
				return false ;
			}
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			HttpRequest request = clientContext.getRequest() ;
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest) ;
			if(idempotent){
				return true ;
			}
			return false ;
		}
		
		public boolean retryRequest(IOException arg0, int arg1, HttpContext arg2){
			return false ;
		}
}
	private class KeepAliveStrategy implements ConnectionKeepAliveStrategy{
		/**
		 * 返回该连接保持idle的时间（ms），在该时间过时之前，该连接不允许被reuse
		 *@param response  在该连接上收到的最近的一个response
		 *@param context  该连接所处的http上下文
		 */
		public long getKeepAliveDuration(HttpResponse response, HttpContext context){
			//查看response中有无Keep-Alive头
			HeaderElementIterator it =
					new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			//hasNext()指示是否有另一个元素在这个迭代。
			while(it.hasNext()){
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if(value != null && param.equalsIgnoreCase("timeout")){
					try {
						return Long.parseLong(value) * 1000 ;
					}catch(NumberFormatException ignore){
						return 2000 ;//HTTP_DEFAULT_KEEPALIVE暂时写死
					}
				}
			}
			return 20000 ;//HTTP_DEFAULT_KEEPALIVE暂时写死
		}
	}
	private class IdleConnectionMonitorTask extends Thread{
		private final HttpClientConnectionManager connMgr ;
		private volatile boolean shutdown = false ;
		public IdleConnectionMonitorTask(HttpClientConnectionManager connMgr){
			super() ;
			this.connMgr = connMgr ;
		}
		public void run(){
			while(!shutdown){
				synchronized (this) {
					try{
						wait(5000) ; //暂时写死5000ms
						//关闭失效的连接
						connMgr.closeExpiredConnections();
						//关闭闲置超过30分钟的连接
						connMgr.closeIdleConnections(30000,TimeUnit.SECONDS);
					}catch(InterruptedException e){}
					
				}
			}
		}
	}
}
