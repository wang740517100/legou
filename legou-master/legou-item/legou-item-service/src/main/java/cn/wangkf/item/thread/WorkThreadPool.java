package cn.wangkf.item.thread;

import cn.wangkf.item.queue.MemoryQueue;
import cn.wangkf.item.request.Request;
import sun.awt.windows.WToolkit;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wangk on 2020-03-14.
 */
public class WorkThreadPool {

    private ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public WorkThreadPool() {
        MemoryQueue memoryQueue = MemoryQueue.getInstance();

        for (int i = 0; i < 10; i++) {
            ArrayBlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(100);
            memoryQueue.addQueue(queue);
            threadPool.submit(new WorkThread(queue));
        }


    }



    private static class SingletonHolder {
        private static final WorkThreadPool INSTANCE = new WorkThreadPool();
    }


    public static WorkThreadPool getInstance() {
        return SingletonHolder.INSTANCE;
    }


}
