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
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;


import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;


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

	@Override
	@HystrixCommand(fallbackMethod = "getFallBack",commandKey = "QuerySpuBoInfo", groupKey = "CacheService",
			threadPoolKey = "QuerySpuBoInfoPool", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "10"),
			@HystrixProperty(name = "maxQueueSize", value = "5"),
			@HystrixProperty(name = "keepAliveTimeMinutes", value = "1"),
			@HystrixProperty(name = "queueSizeRejectionThreshold", value = "5"),
			@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "10"),
			@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "10000")
	})
	public SpuBo querySpuBoInfo(Long id) {
		SpuBo spuBo = null;

		spuBo = this.getSpuBoCasche(id);
		System.out.println("=================从redis中获取缓存，商品信息=" + spuBo);

		if(spuBo == null) {
			spuBo = this.getSpuBoLocalCache(id);
			System.out.println("=================从ehcache中获取缓存，商品信息=" + spuBo);
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

	protected String getFallback(Throwable e) {
		System.out.println(e.getMessage());
		e.printStackTrace();
		return "faild";
	}



}
