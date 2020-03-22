package cn.wangkf.service.impl;

import javax.annotation.Resource;

import cn.wangkf.common.utils.DozerUtils;
import cn.wangkf.item.api.GoodsApi;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.item.pojo.Sku;
import cn.wangkf.item.pojo.Spu;
import cn.wangkf.item.pojo.SpuDetail;
import cn.wangkf.rebuild.RebuildCacheQueue;
import cn.wangkf.service.CacheService;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


@Service("cacheService")
public class CacheServiceImpl implements CacheService {

	public static final String CACHE_NAME = "local";

	private static final String REDIS_SPU_KEY_PRE = "item-service_spu_id_";

	private static final String REDIS_SPU_DETAIL_KEY_PRE = "item-service_spu_detail_spuid_";

	private static final String REDIS_SKU_KEY_PRE = "item-service_sku_spuid_";

	@Autowired
	private GoodsApi goodsApi;
	
	@Resource
	private JedisCluster jedisCluster;

	@Override
	public SpuBo getSpuBoCasche(Long id) {
		String spukey = REDIS_SPU_KEY_PRE + id;
		String spuStr = jedisCluster.get(spukey);
		Spu spu = JSONObject.toJavaObject(JSON.parseObject(spuStr), SpuBo.class);

		// 查询 spuDetail 信息
		String spuDetailKey = REDIS_SPU_DETAIL_KEY_PRE + id;
		String spuDetailStr = jedisCluster.get(spuDetailKey);
		SpuDetail spuDetail = JSONObject.toJavaObject(JSON.parseObject(spuDetailStr), SpuDetail.class);

		// 查询 skuLis 信息
		String skuListKey = REDIS_SKU_KEY_PRE + id;
		long size = jedisCluster.llen(skuListKey);
		List<String> skuStrList = jedisCluster.lrange(skuListKey, 0, size-1);
		List<Sku> skuList = new ArrayList<>();
		skuStrList.stream().forEach(s -> {
			skuList.add(JSONObject.toJavaObject(JSON.parseObject(s), Sku.class));
		});

		SpuBo spuBo = new SpuBo(spu.getBrandId(),spu.getCid1(),spu.getCid2(),spu.getCid3(),spu.getTitle(),
				spu.getSubTitle(),spu.getSaleable(),spu.getValid(),spu.getCreateTime(),spu.getLastUpdateTime());
		spuBo.setSpuDetail(spuDetail);
		spuBo.setSkus(skuList);
		return spuBo;
	}

	@Override
	public void saveSpuBoCasche(SpuBo spuBo) {
		Spu spu = DozerUtils.map(spuBo, Spu.class);
		String spuStr = JSONObject.toJSONString(spu);
		String spuKey = REDIS_SPU_KEY_PRE + spuBo.getId();
		jedisCluster.set(spuKey, spuStr);

		String spuDetailStr = JSONObject.toJSONString(spuBo.getSpuDetail());
		String spuDetailKey = REDIS_SPU_DETAIL_KEY_PRE + spuBo.getId();
		jedisCluster.set(spuDetailKey, spuDetailStr);

		String skuListKey = REDIS_SKU_KEY_PRE + spuBo.getId();
		spuBo.getSkus().forEach(s -> {
			jedisCluster.lpush(skuListKey, JSONObject.toJSONString(s));
		});
	}

	@Cacheable(value = CACHE_NAME, key = "'key_'+#spuBo.getId()")
	public SpuBo saveSpuBoLocalCache(SpuBo spuBo) {
		return spuBo;
	}

	@Cacheable(value = CACHE_NAME, key = "'key_'+#id")
	public SpuBo getSpuBoLocalCache(Long id) {
		return null;
	}





	@CacheResult(cacheKeyMethod = "getSpuBoCacheKey")  // 对应 update 方法可使用 @CacheRemove
	@HystrixCommand(fallbackMethod = "getSpuBoFallback",commandKey = "QuerySpuBoInfo", groupKey = "CacheService",
			threadPoolKey = "QuerySpuBoInfoPool", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "10"),
			@HystrixProperty(name = "maxQueueSize", value = "5"),
			@HystrixProperty(name = "keepAliveTimeMinutes", value = "1"),
			@HystrixProperty(name = "queueSizeRejectionThreshold", value = "5"),
			@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "10"),
			@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
	})
	@Override
	public SpuBo querySpuBoInfo(Long id) {
		SpuBo spuBo = this.getSpuBoCasche(id);
		if(spuBo == null) {
			spuBo = this.getSpuBoLocalCache(id);
		}

		if(spuBo == null) {
			// 就需要从数据源重新拉去数据，重建缓存
			spuBo = goodsApi.queryGoodsById(id);
			// 将数据推送到一个内存队列中
			RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
			rebuildCacheQueue.putProductInfo(spuBo);
		}
		return spuBo;
	}



	/**
	 *  getCacheKey 方法的参数要与 querySpuBoInfo 方法保持一致否则会报错
	 * @param id
	 * @return
	 */
	public String getSpuBoCacheKey(Long id) {
		return "spuBo_info_" + id;

	}


    /**
	 * 请求折叠技术
	 * @param ids
	 * @return
     */

	@HystrixCollapser(batchMethod = "querySpuBoList", collapserProperties = {
			//收集1秒内的请求
			@HystrixProperty(name = "timerDelayInMilliseconds", value = "1000")
	})
	@Override
	public Future<List<SpuBo>> queryBatchSpuBoInfo(List<Long> ids) {
		return null;
	}

	@CacheResult(cacheKeyMethod = "getBatchSpuBoCacheKey")
	@HystrixCommand(fallbackMethod = "getSpuBoFallback", commandKey = "QueryBatchSpuBoInfo", groupKey = "CacheService",
			threadPoolKey = "QuerySpuBoInfoPool", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "10"),
			@HystrixProperty(name = "maxQueueSize", value = "5"),
			@HystrixProperty(name = "keepAliveTimeMinutes", value = "1"),
			@HystrixProperty(name = "queueSizeRejectionThreshold", value = "5"),
			@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "10"),
			@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
	})
	public List<List<SpuBo>> querySpuBoList(List<List<Long>> batchIds) {
		List<List<SpuBo>> res = Lists.newArrayList();
		batchIds.forEach(b -> {

			List<SpuBo> spuBoList = Lists.newArrayList();
			for (Long id : b) {
				SpuBo spuBo = this.querySpuBoInfo(id);
				spuBoList.add(spuBo);
			}
			res.add(spuBoList);
		});
		return res;
	}

	/**
	 *  getCacheKey 方法的参数要与 querySpuBoInfo 方法保持一致否则会报错
	 * @param batchIds
	 * @return
	 */
	public String getBatchSpuBoCacheKey(List<List<Long>> batchIds) {
		return "spuBo_info_" + batchIds.toString();

	}


	/**
	 * hystrix 快速失败方法
	 * @param e
	 * @return
	 */
	protected String getSpuBoFallback(Throwable e) {
		e.printStackTrace();
		return "faild";
	}


}
