package cn.wangkf.item.thread;

import cn.wangkf.item.queue.MemoryQueue;
import cn.wangkf.item.request.GoodsRefreshCacheRequest;
import cn.wangkf.item.request.GoodsUpdateCacheRequest;
import cn.wangkf.item.request.Request;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

/**
 * Created by wangk on 2020-03-14.
 */
public class WorkThread implements Callable {

    private ArrayBlockingQueue<Request> queue;

    public WorkThread(ArrayBlockingQueue<Request> queue) {
        queue = this.queue;
    }

    @Override
    public Object call() throws Exception {

        try {
            // 这个循环逻辑：
            // 当时 refresh 操作时只负责讲数据从 mysql 更新到 redis 上
            // 当时 delete 操作时只负责 mysql 数据，删除 redis 数据
            while (true)  {
                Request request = queue.take();
                // 判断是否要去刷新缓存
                if (!request.isRefreshRedis()) {
                    MemoryQueue memoryQueue = MemoryQueue.getInstance();
                    Map<Long, Boolean> flagMap = memoryQueue.getFlagMap();

                    if (request instanceof GoodsUpdateCacheRequest) {
                        flagMap.put(request.getSpuId(), true);
                    } else if (request instanceof GoodsRefreshCacheRequest) {
                        Boolean flag = flagMap.get(request.getSpuId());
                        if (flag == null) {
                            flagMap.put(request.getSpuId(), false);
                        }
                        else if (flag != null && flag) {
                            flagMap.put(request.getSpuId(), false);
                        }

                        // 1、前面有 select 操作
                        // 2、前面有 update 和 select 操作
                        else if (flag != null && !flag) {
                            return true;
                        }
                    }
                }
                request.process();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
