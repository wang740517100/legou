package cn.wangkf.item.service.impl;

import cn.wangkf.item.queue.MemoryQueue;
import cn.wangkf.item.request.Request;
import cn.wangkf.item.service.RequestRouteService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wangk on 2020-03-14.
 */
@Service
public class RequestRouteServiceImpl implements RequestRouteService {


    @Override
    public void process(Request request) {
        ArrayBlockingQueue<Request> queue = getRouteQueue(request.getSpuId());
        try {
            /**
             1)add(anObject):把anObject加到BlockingQueue里,即如果BlockingQueue可以容纳,则返回true,否则招聘异常

             2)offer(anObject):表示如果可能的话,将anObject加到BlockingQueue里,即如果BlockingQueue可以容纳,则返回true,否则返回false.

             3)put(anObject):把anObject加到BlockingQueue里,如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.

             4)poll(time):取走BlockingQueue里排在首位的对象,若不能立即取出,则可以等time参数规定的时间,取不到时返回null

             5)take():取走BlockingQueue里排在首位的对象,若BlockingQueue为空,阻断进入等待状态直到Blocking有新的对象被加入为止
             */
            queue.put(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }


    public ArrayBlockingQueue<Request> getRouteQueue(Long spuId) {
        MemoryQueue queues = MemoryQueue.getInstance();
        String key = String.valueOf(spuId);
        int h;
        int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);

        int index = (queues.getSize() - 1) & hash;
        return queues.getQueue(index);

    }


}
