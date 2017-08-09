package main.org.lele.threadpool;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import main.org.lele.Task.HttpResponse;
import main.org.lele.Task.Task;

/**
 * 单例。包装了一个线程池，用于执行Task
 * @author LELE 
 *
 */
public class ThreadPool {
	private static int nThreads = 200;
	private static ThreadPool pool = new ThreadPool();
	private static ThreadPoolExecutor realPool ;
	private ThreadPool(){
		//newFixedThreadPool生成固定大小的线程池
		realPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(nThreads) ;
	}
	
	public static ThreadPool getInstance(){
		return pool ;
	}
	public Future<HttpResponse> submit(Task task){
		if(task == null){
			System.out.println("parameter task == null.");
			return null ;
		}
		try{
			return realPool.submit(task) ;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null ;
	}
	
	public void shutdown(){
		if(realPool == null){
			return ;
		}
		realPool.shutdown();
	}
	
	public void shutdownNow(){
		if(realPool == null){
			return ;
		}
		realPool.shutdownNow() ;
	}
}
