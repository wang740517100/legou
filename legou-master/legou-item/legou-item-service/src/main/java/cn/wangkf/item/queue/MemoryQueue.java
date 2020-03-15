package cn.wangkf.item.queue;

import cn.wangkf.item.request.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wangk on 2020-03-14.
 */
public class MemoryQueue {

    private List<ArrayBlockingQueue<Request>> queues = new ArrayList<>();

    // 需要自己写一个回收算法清楚一定时间没有使用的kv
    // 或者直接用 redis 来做就可以了
    private Map<Long, Boolean> flagMap = new HashMap<>();


    private static class SingletonHolder {
        private static final MemoryQueue INSTANCE = new MemoryQueue();
    }

    public static MemoryQueue getInstance() {
        return SingletonHolder.INSTANCE;
    }


    public void addQueue(ArrayBlockingQueue<Request> queue) {
        queues.add(queue);
    }


    public int getSize() {
        return queues.size();
    }

    public ArrayBlockingQueue<Request> getQueue(int index) {
        return queues.get(index);
    }

    public Map<Long, Boolean> getFlagMap() {
        return flagMap;
    }


}
