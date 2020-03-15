package cn.wangkf.item.service;


import cn.wangkf.common.parameter.SpuQueryByPageParameter;
import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.item.pojo.SpuDetail;
import cn.wangkf.item.pojo.Sku;

import java.util.List;

public interface GoodsService {

    /**
     * 查询商品
     * @param spuQueryByPageParameter
     * @return
     */
    PageResult<SpuBo> queryGoodsWithPage(SpuQueryByPageParameter spuQueryByPageParameter);

    /**
     * 保存商品
     * @param spu
     */
    void saveGoods(SpuBo spu);

    /**
     * 根据id查询商品信息
     * @param id
     * @return
     */
    SpuBo queryGoodsById(Long id);

    /**
     * 更新商品信息
     * @param spuBo
     */
    void updateGoods(SpuBo spuBo);

    /**
     * 商品删除，单个多个二合一
     * @param id
     */
    void deleteGoods(long id);

    /**
     * 根据spu商品id查询详细信息
     * @param id
     * @return
     */
    SpuDetail querySpuDetailBySpuId(long id);

    /**
     * 根据Sku的id查询其下所有的sku
     * @param id
     * @return
     */
    List<Sku> querySkuBySpuId(Long id);


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
    void updateSpuBoCasche(SpuBo spuBo);

    /**
     * 删除 redis 商品信息
     * @param id
     * @return
     */
    Boolean deleteSpuBoCasche(Long id);

}
