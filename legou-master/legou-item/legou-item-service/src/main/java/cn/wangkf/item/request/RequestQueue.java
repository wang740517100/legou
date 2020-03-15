package cn.wangkf.item.request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 请求内存队列
 * @author Administrator
 *
 */
public class RequestQueue {

	/**
	 * 内存队列
	 */
	private List<ArrayBlockingQueue<RequestDTO>> queues = new ArrayList<ArrayBlockingQueue<RequestDTO>>();
	
	/**
	 * 单例有很多种方式去实现：我采取绝对线程安全的一种方式
	 * 
	 * 静态内部类的方式，去初始化单例
	 * 
	 * @author Administrator
	 *
	 */
	private static class RequestQueueSingleton {
		
		private static RequestQueue instance;
		
		static {
			instance = new RequestQueue();
		}
		
		public static RequestQueue getInstance() {
			return instance;
		}
		
	}
	
	/**
	 * jvm的机制去保证多线程并发安全
	 * 
	 * 内部类的初始化，一定只会发生一次，不管多少个线程并发去初始化
	 * 
	 * @return
	 */
	public static RequestQueue getInstance() {
		return RequestQueueSingleton.getInstance();
	}
	
	/**
	 * 添加一个内存队列
	 * @param queue
	 */
	public void addQueue(ArrayBlockingQueue<RequestDTO> queue) {
		this.queues.add(queue);
	}
	
}
