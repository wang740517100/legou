package cn.wangkf.rebuild;

import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.service.CacheService;
import cn.wangkf.util.SpringContext;
import cn.wangkf.zk.ZooKeeperSession;

import java.text.SimpleDateFormat;


/**
 * 缓存重建线程
 *
 */
public class RebuildCacheThread implements Runnable {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void run() {
		RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
		CacheService cacheService = (CacheService) SpringContext.getApplicationContext()
				.getBean("cacheService");
		
		while(true) {
			SpuBo spuBo = rebuildCacheQueue.takeProductInfo();
			
			zkSession.acquireDistributedLock(spuBo.getId());

			SpuBo existedSpuBo = cacheService.getSpuBoCasche(spuBo.getId());
			
			if(existedSpuBo != null) {
				// 比较当前数据的时间版本比已有数据的时间版本是新还是旧
				try {
					if(spuBo.getLastUpdateTime().before(existedSpuBo.getLastUpdateTime())) {
						System.out.println("current date[" + spuBo.getLastUpdateTime() + "] is before existed date[" + existedSpuBo.getLastUpdateTime() + "]");
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("current date[" + spuBo.getLastUpdateTime() + "] is after existed date[" + existedSpuBo.getLastUpdateTime() + "]");
			} else {
				System.out.println("existed product info is null......");   
			}
			
			cacheService.saveSpuBoLocalCache(spuBo);
			cacheService.saveSpuBoCasche(spuBo);
			
			zkSession.releaseDistributedLock(spuBo.getId());
		}
	}

}
