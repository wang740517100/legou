package cn.wangkf.rebuild;


import cn.wangkf.item.bo.SpuBo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 重建缓存的内存队列
 * @author Administrator
 *
 */
public class RebuildCacheQueue {

	private ArrayBlockingQueue<SpuBo> queue = new ArrayBlockingQueue<SpuBo>(1000);
	
	public void putProductInfo(SpuBo spuBo) {
		try {
			queue.put(spuBo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public SpuBo takeProductInfo() {
		try {
			return queue.take();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 内部单例类
	 * @author Administrator
	 *
	 */
	private static class Singleton {
		
		private static RebuildCacheQueue instance;
		
		static {
			instance = new RebuildCacheQueue();
		}
		
		public static RebuildCacheQueue getInstance() {
			return instance;
		}
		
	}
	
	public static RebuildCacheQueue getInstance() {
		return Singleton.getInstance();
	}
	
	public static void init() {
		getInstance();
	}
	
}
