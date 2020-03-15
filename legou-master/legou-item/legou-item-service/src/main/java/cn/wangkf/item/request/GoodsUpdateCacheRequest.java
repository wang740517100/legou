package cn.wangkf.item.request;

import cn.wangkf.item.bo.SpuBo;
import cn.wangkf.item.service.GoodsService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangk on 2020-03-14.
 */
public class GoodsUpdateCacheRequest implements Request {

    private SpuBo spuBo;

    private GoodsService goodsService;

    // 是否刷新 redis
    private Boolean isRefreshRedis;

    private Logger logger = LoggerFactory.getLogger(GoodsUpdateCacheRequest.class);


    public GoodsUpdateCacheRequest(SpuBo spuBo, GoodsService goodsService, Boolean isRefreshRedis) {
        this.spuBo = spuBo;
        this.goodsService = goodsService;
        this.isRefreshRedis = isRefreshRedis;
    }

    @Override
    public Long getSpuId() {
        return spuBo.getId();
    }

    @Override
    public void process() {

        // 删除商品缓存
        if (goodsService.deleteSpuBoCasche(this.spuBo.getId())) {
            // 更新商品DB信息
            goodsService.updateGoods(this.spuBo);
        } else {
            logger.error("更新redis缓存失败：spuBo={}", JSONObject.toJSON(this.spuBo));
        }
    }

    @Override
    public boolean isRefreshRedis() {
        return false;
    }
}
