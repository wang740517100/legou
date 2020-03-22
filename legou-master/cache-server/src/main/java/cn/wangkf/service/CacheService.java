package cn.wangkf.service;


import cn.wangkf.item.bo.SpuBo;

import java.util.List;
import java.util.concurrent.Future;

/**
 * 缓存service接口
 * @author Administrator
 *
 */
public interface CacheService {

	/**
	 * 根据id在缓存中查询商品信息
	 * @param id
	 * @return
	 */
	SpuBo getSpuBoCasche(Long id);

	/**
	 * 更新 redis 商品信息
	 * @param spuBo
	 * @return
	 */
	void saveSpuBoCasche(SpuBo spuBo);

	/**
	 * 将商品信息保存到本地缓存中
	 * @param spuBo
	 * @return
	 */
	SpuBo saveSpuBoLocalCache(SpuBo spuBo);

	/**
	 * 从本地缓存中获取商品信息
	 * @param id
	 * @return
	 */
	SpuBo getSpuBoLocalCache(Long id);



	/**
	 * 当 nginx 中没有缓存信息时会到该缓存服务中来查询
	 * @param id
	 * @return
	 */
	SpuBo querySpuBoInfo(Long id);


	/**
	 * 当 nginx 中没有缓存信息时会到该缓存服务中来查询
	 * @param ids
	 * @return
	 */
	Future<List<SpuBo>> queryBatchSpuBoInfo(List<Long> ids);


	
}
