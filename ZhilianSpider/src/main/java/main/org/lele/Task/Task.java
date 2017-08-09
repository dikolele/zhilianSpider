package main.org.lele.Task;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * 
 * @author LELE 
 *每个抓取请求对应一个task  Callable返回结果并可能抛出异常的任务
 */
public class Task implements Callable<HttpResponse> {
	private CloseableHttpClient client ;
	private String targetUrl ;
	
	public Task(CloseableHttpClient client , String targetUrl){
		this.client = client ;
		this.targetUrl = targetUrl ;
	}
	
	public HttpResponse call() throws Exception{
		return doGet() ;
	}
	
	private HttpResponse doGet(){
		CloseableHttpResponse resp = null ;
		try{
			HttpGet get = new HttpGet(targetUrl) ;
			//resp返回对请求的响应信息  execute执行请求
			resp = client.execute(get) ;
			if(null != resp){
				int statusCode = resp.getStatusLine().getStatusCode();
				String response = null ;
				if(HttpServletResponse.SC_OK == statusCode){
					//response页面源码,将返回的内容转为字符串
					response = EntityUtils.toString(resp.getEntity()) ;
				}
				return new HttpResponse(statusCode, response) ;
			}
		}catch(ClientProtocolException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(resp != null){
				try{
					resp.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return null ;
	}
}
