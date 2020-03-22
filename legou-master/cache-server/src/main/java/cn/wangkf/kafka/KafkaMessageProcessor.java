package cn.wangkf.kafka;

import cn.wangkf.item.api.GoodsApi;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.service.CacheService;
import cn.wangkf.util.SpringContext;
import cn.wangkf.zk.ZooKeeperSession;
import com.alibaba.fastjson.JSONObject;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

import java.text.SimpleDateFormat;

/**
 * kafka消息处理线程
 *
 */
@SuppressWarnings("rawtypes")
public class KafkaMessageProcessor implements Runnable {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private KafkaStream kafkaStream;

	private CacheService cacheService;

	private GoodsApi goodsApi;
	
	public KafkaMessageProcessor(KafkaStream kafkaStream) {
		this.kafkaStream = kafkaStream;
		this.cacheService = (CacheService) SpringContext.getApplicationContext()
				.getBean("cacheService");
		this.goodsApi = (GoodsApi) SpringContext.getApplicationContext()
				.getBean("goodsApi");
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
        while (it.hasNext()) {
        	String message = new String(it.next().message());
        	
        	// 首先将message转换成json对象
        	JSONObject messageJSONObject = JSONObject.parseObject(message);
        	
        	// 从这里提取出消息对应的服务的标识
        	String serviceId = messageJSONObject.getString("serviceId");  
        	
        	// 如果是商品信息服务
        	if("productInfoService".equals(serviceId)) {
        		processProductInfoChangeMessage(messageJSONObject);
        	}
        }
	}
	
	/**
	 * 处理商品信息变更的消息
	 * @param messageJSONObject
	 */
	private void processProductInfoChangeMessage(JSONObject messageJSONObject) {
		// 提取出商品id
		Long spuId = messageJSONObject.getLong("spuId");
		
		// 调用商品信息服务的接口
		// 龙果有分布式事务的课程，主要讲解的分布式事务几种解决方案，里面也涉及到了一些mq，或者其他的一些技术，但是那些技术都是浅浅的给你搭建一下，使用
		// 你从一个课程里，还是学到的是里面围绕的讲解的一些核心的知识
		// 缓存架构：高并发、高性能、海量数据，等场景
		
		SpuBo spuBo = goodsApi.queryGoodsById(spuId);
		
		
		// 加代码，在将数据直接写入redis缓存之前，应该先获取一个zk的分布式锁
		ZooKeeperSession zkSession = ZooKeeperSession.getInstance();
		zkSession.acquireDistributedLock(spuId);
		
		// 获取到了锁
		// 先从redis中获取数据
		SpuBo existedSpuBo = cacheService.getSpuBoCasche(spuId);
		
		if(existedSpuBo != null) {
			// 比较当前数据的时间版本比已有数据的时间版本是新还是旧
			try {
				if(spuBo.getLastUpdateTime().before(existedSpuBo.getLastUpdateTime())) {
					System.out.println("current date[" + spuBo.getLastUpdateTime() + "] is before existed date[" + existedSpuBo.getLastUpdateTime() + "]");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("current date[" + spuBo.getLastUpdateTime() + "] is after existed date[" + existedSpuBo.getLastUpdateTime() + "]");
		} else {
			System.out.println("existed product info is null......");   
		}
		
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		cacheService.saveSpuBoLocalCache(spuBo);
		System.out.println("===================获取刚保存到本地缓存的商品信息：" + cacheService.getSpuBoLocalCache(spuId));
		cacheService.saveSpuBoCasche(spuBo);
		
		// 释放分布式锁
		zkSession.releaseDistributedLock(spuId);
	}
	


}
