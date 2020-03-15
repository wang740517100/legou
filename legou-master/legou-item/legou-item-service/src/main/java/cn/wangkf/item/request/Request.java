package cn.wangkf.item.request;

/**
 * Created by wangk on 2020-03-14.
 */
public interface Request {

    Long getSpuId();

    void process();

    boolean isRefreshRedis();
}
