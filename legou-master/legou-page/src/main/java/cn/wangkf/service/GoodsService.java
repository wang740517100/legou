package cn.wangkf.service;

import java.util.Map;


public interface GoodsService {
    /**
     * 商品详细信息
     * @param spuId
     * @return
     */
    Map<String,Object> loadModel(Long spuId);

    /**
     * 创建商品静态页
     * @param spuId
     * @return
     */
    void  createHtml(Long spuId);

    /**
     * 删除商品静态页
     * @param spuId
     * @return
     */
    void  deleteHtml(Long spuId);


}
