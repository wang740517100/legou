package cn.wangkf.item.request;

import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.item.service.GoodsService;

/**
 * Created by wangk on 2020-03-14.
 */
public class GoodsRefreshCacheRequest implements Request {

    private Long spuId;

    private GoodsService goodsService;



    // 是否刷新 redis
    private Boolean isRefreshRedis;


    public GoodsRefreshCacheRequest(Long spuId, GoodsService goodsService, Boolean isRefreshRedis) {
        this.spuId = spuId;
        this.goodsService = goodsService;
        this.isRefreshRedis = isRefreshRedis;
    }


    @Override
    public Long getSpuId() {
        return this.spuId;
    }

    @Override
    public void process() {

        // 查询商品DB信息
        SpuBo spuBo =  goodsService.queryGoodsById(this.spuId);

        // 更新商品缓存信息
        goodsService.updateSpuBoCasche(spuBo);

    }

    @Override
    public boolean isRefreshRedis() {
        return isRefreshRedis;
    }


}
