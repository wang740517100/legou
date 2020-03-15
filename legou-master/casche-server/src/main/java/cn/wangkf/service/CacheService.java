package cn.wangkf.service;


import cn.wangkf.item.bo.SpuBo;

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
	public SpuBo saveSpuBoLocalCache(SpuBo spuBo);

	/**
	 * 从本地缓存中获取商品信息
	 * @param id
	 * @return
	 */
	public SpuBo getSpuBoLocalCache(Long id);


	
}
