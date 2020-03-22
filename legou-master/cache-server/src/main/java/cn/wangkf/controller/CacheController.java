package cn.wangkf.controller;

import cn.wangkf.item.api.GoodsApi;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.rebuild.RebuildCacheQueue;
import cn.wangkf.service.CacheService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;



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




	
}
