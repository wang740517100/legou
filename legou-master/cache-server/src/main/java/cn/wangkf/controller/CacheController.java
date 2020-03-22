package cn.wangkf.controller;

import cn.wangkf.item.api.GoodsApi;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.rebuild.RebuildCacheQueue;
import cn.wangkf.service.CacheService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@RestController
@RequestMapping("cache/goods")
public class CacheController {

	@Autowired
	private CacheService cacheService;


	@RequestMapping("/spu/{id}")
	@ResponseBody
	public ResponseEntity<SpuBo> querySpuBoInfo(@PathVariable Long id) {
		SpuBo spuBo = cacheService.querySpuBoInfo(id);
		return ResponseEntity.ok(spuBo);
	}

	/**
	 * 批量查询 spuBo ：采用请求折叠技术
	 * @param ids
	 * @return
     */
	@RequestMapping("/spu/{ids}")
	@ResponseBody
	public ResponseEntity<List<SpuBo>> querySpuBoInfo(@PathVariable List<Long> ids) {
		Future<List<SpuBo>> future = cacheService.queryBatchSpuBoInfo(ids);
		List<SpuBo> spuBoList = Lists.newArrayList();
		try {
			spuBoList = future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(spuBoList);
	}

	
}
