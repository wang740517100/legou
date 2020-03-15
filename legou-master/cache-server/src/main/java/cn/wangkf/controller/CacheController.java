package cn.wangkf.controller;

import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.rebuild.RebuildCacheQueue;
import cn.wangkf.service.CacheService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;



@Controller
public class CacheController {

	@Resource
	private CacheService cacheService;
	

	
	@RequestMapping("/getProductInfo")
	@ResponseBody
	public SpuBo getProductInfo(Long productId) {
		SpuBo spuBo = null;

		spuBo = cacheService.getSpuBoCasche(productId);
		System.out.println("=================从redis中获取缓存，商品信息=" + spuBo);
		
		if(spuBo == null) {
			spuBo = cacheService.getSpuBoLocalCache(productId);
			System.out.println("=================从ehcache中获取缓存，商品信息=" + spuBo);
		}
		
		if(spuBo == null) {
			// 就需要从数据源重新拉去数据，重建缓存，但是这里先不讲
			String productInfoJSON = "{\"id\": 5, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1, \"modifiedTime\": \"2017-01-01 12:01:00\"}";
			spuBo = JSONObject.parseObject(productInfoJSON, SpuBo.class);
			// 将数据推送到一个内存队列中
			RebuildCacheQueue rebuildCacheQueue = RebuildCacheQueue.getInstance();
			rebuildCacheQueue.putProductInfo(spuBo);
		}
		
		return spuBo;
	}

	
}
