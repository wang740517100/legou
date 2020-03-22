package cn.wangkf.blot;

import cn.wangkf.spout.SpuBoInfoSpout;
import cn.wangkf.util.HttpClientUtils;
import cn.wangkf.zk.ZooKeeperClient;
import com.alibaba.fastjson.JSONObject;
import org.apache.storm.shade.org.apache.http.NameValuePair;
import org.apache.storm.shade.org.apache.http.client.utils.URLEncodedUtils;
import org.apache.storm.shade.org.apache.http.message.BasicNameValuePair;
import org.apache.storm.shade.org.apache.http.protocol.HTTP;
import org.apache.storm.shade.org.json.simple.JSONArray;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.trident.util.LRUMap;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by wangk on 2020-03-22.
 */
public class SpuBoCountBlot extends BaseRichBolt {

    private static final long serialVersionUID = -8761807561458126413L;

    private int taskid;

    private ZooKeeperClient zkClient;

    private LRUMap<Long, Long> spuBoCountMap = new LRUMap<Long, Long>(1000);

    private static final Logger logger = LoggerFactory.getLogger(SpuBoInfoSpout.class);

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.taskid = topologyContext.getThisTaskId();
        this.zkClient = ZooKeeperClient.getInstance();
        new Thread(new SpuBoCountThread()).start();
        initTaskId(topologyContext.getThisTaskId());
    }


    /**
     *  缓存热点数据处理，每隔5秒钟计算一次排名前n的spuBo
     *  计算出实时热点数据，发送给 nginx 去缓存服务拉取数据
     *  然后将热点信息保存在所有 nginx 应用层服务器上，采用负载均衡策略去提供服务，避免 nginx 宕机
     *
     */
    private class SpuBoHotInfoThread implements Runnable {

        @Override
        public void run() {
            List<Map.Entry<Long, Long>> spuboCountList = new ArrayList<Map.Entry<Long, Long>>(spuBoCountMap.entrySet());
            List<Long> hotSpuBoIdList = new ArrayList<Long>();
            //上一次计算出的热点数据
            List<Long> lastTimeHotSpuBoIdList = new ArrayList<Long>();


            while (true) {
                // 1、将LRUMap中的数据按照访问次数，进行全局的排序
                // 2、计算95%的商品的访问次数的平均值
                // 3、遍历排序后的商品访问次数，从最大的开始
                // 4、如果某个商品比如它的访问量是平均值的10倍，就认为是缓存的热点
                try {
                    spuboCountList.clear();
                    hotSpuBoIdList.clear();

                    if (spuBoCountMap.size() == 0) {
                        Utils.sleep(100);
                        continue;
                    }

                    logger.info("【SpuBoHotInfoThread打印spuBoCountMap的长度】size=" + spuBoCountMap.size());

                    Collections.sort(spuboCountList, new Comparator<Map.Entry<Long, Long>>() {
                        public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                            Long res = o1.getValue() - o2.getValue();
                            return res.intValue();
                        }
                    });
                    logger.info("【SpuBoHotInfoThread全局排序后的结果】spuboCountList=" + JSONObject.toJSONString(spuboCountList));

                    // 2、计算出95%的商品的访问次数的平均值
                    int calculateCount = (int)Math.floor(spuboCountList.size() * 0.95);
                    Long totalCount = 0L;
                    for(int i = spuboCountList.size() - 1; i >= spuboCountList.size() - calculateCount; i--) {
                        totalCount += spuboCountList.get(i).getValue();
                    }
                    Long avgCount = totalCount / calculateCount;
                    logger.info("【SpuBoHotInfoThread计算出95%的商品的访问次数平均值】avgCount=" + avgCount);



                    // 3、从第一个元素开始遍历，判断是否是平均值得10倍
                    for(Map.Entry<Long, Long> spuBoCountEntry : spuboCountList) {
                        if(spuBoCountEntry.getValue() > 10 * avgCount) {
                            logger.info("【SpuBoHotInfoThread发现一个热点】spuBoCountEntry=" + spuBoCountEntry);
                            hotSpuBoIdList.add(spuBoCountEntry.getKey());

                            if (!lastTimeHotSpuBoIdList.contains(spuBoCountEntry.getKey())) {
                                // 将缓存热点反向推送到流量分发的 nginx 中
                                String distributeNginxURL = "http://192.168.31.136/hot?spuBoId=" + spuBoCountEntry.getKey();
                                HttpClientUtils.sendGetRequest(distributeNginxURL);

                                // 将缓存热点，那个商品对应的完整的缓存数据，发送请求到缓存服务去获取，反向推送到所有的后端应用nginx服务器上去
                                String cacheServiceURL = "http://192.168.31.138:8080/getSpuBoInfo?spuBoId=" + spuBoCountEntry.getKey();
                                String response = HttpClientUtils.sendGetRequest(cacheServiceURL);

                                List<NameValuePair> params = new ArrayList<NameValuePair>();
                                params.add(new BasicNameValuePair("productInfo", response));
                                String spuBoInfo = URLEncodedUtils.format(params, HTTP.UTF_8);

                                String[] appNginxURLs = new String[]{
                                        "http://192.168.31.139/hot?productId=" + spuBoCountEntry.getKey() + "&" + spuBoInfo,
                                        "http://192.168.31.137/hot?productId=" + spuBoCountEntry.getKey() + "&" + spuBoInfo
                                };
                                for(String appNginxURL : appNginxURLs) {
                                    HttpClientUtils.sendGetRequest(appNginxURL);
                                }
                            }
                        }
                    }


                    // 4、实时感知热点数据的消失
                    if(lastTimeHotSpuBoIdList.size() == 0) {
                        if(hotSpuBoIdList.size() > 0) {
                            for(Long spuBoId : hotSpuBoIdList) {
                                lastTimeHotSpuBoIdList.add(spuBoId);
                            }
                            logger.info("【SpuBoHotInfoThread保存上次热点数据】lastTimeHotSpuBoIdList=" +
                                    JSONObject.toJSONString(lastTimeHotSpuBoIdList));
                        }
                    } else {
                        for(Long spuBoId : lastTimeHotSpuBoIdList) {
                            if(!hotSpuBoIdList.contains(spuBoId)) {
                                logger.info("【SpuBoHotInfoThread发现一个热点消失了】spuBoId=" + spuBoId);
                                // 说明上次的那个商品id的热点，消失了
                                // 发送一个http请求给到流量分发的nginx中，取消热点缓存的标识
                                String url = "http://192.168.31.136/cancel_hot?spuBoId=" + spuBoId;
                                HttpClientUtils.sendGetRequest(url);
                            }
                        }

                        if(hotSpuBoIdList.size() > 0) {
                            lastTimeHotSpuBoIdList.clear();
                            for(Long spuBoId : hotSpuBoIdList) {
                                lastTimeHotSpuBoIdList.add(spuBoId);
                            }
                            logger.info("【SpuBoHotInfoThread保存上次热点数据】lastTimeHotSpuBoIdList=" +
                                    JSONObject.toJSONString(lastTimeHotSpuBoIdList));
                        } else {
                            lastTimeHotSpuBoIdList.clear();
                        }
                    }

                    Utils.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  缓存的预热，每隔5秒钟计算一次排名前n的spuBo
     *  把信息放在 zk 上，然后让缓存服务去 zk 读取然后拉取缓存
     */
    private class SpuBoCountThread implements Runnable {

        @Override
        public void run() {
            List<Map.Entry<Long, Long>> topnSpuboList = new ArrayList<Map.Entry<Long, Long>>(spuBoCountMap.entrySet());
            List<Long> spuBoIdList = new ArrayList<Long>();


            while (true) {
                try {
                    topnSpuboList.clear();
                    spuBoIdList.clear();

                    int topN = 3; // 取前几

                    if (spuBoCountMap.size() == 0) {
                        Utils.sleep(100);
                        continue;
                    }

                    Collections.sort(topnSpuboList, new Comparator<Map.Entry<Long, Long>>() {
                        public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                            Long res = o1.getValue() - o2.getValue();
                            return res.intValue();
                        }
                    });

                    // 获取到一个topn list
                    for (int i = 0; i < topN; i++) {
                        spuBoIdList.add(topnSpuboList.get(i).getKey());
                    }
                    String topnSpuBoListJSON = JSONArray.toJSONString(spuBoIdList);
                    zkClient.createNode("/task-hot-spubo-list-" + taskid);
                    zkClient.setNodeData("/task-hot-spubo-list-" + taskid, topnSpuBoListJSON);
                    logger.info("【SpuBoCountThread计算出一份top3热门商品列表】zk path=" + ("/task-hot-spubo-list-" +
                            taskid) + ", topnSpuBoListJSON=" + topnSpuBoListJSON);

                    Utils.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initTaskId(int taskid) {
        // ProductCountBolt所有的task启动的时候， 都会将自己的taskid写到同一个node的值中
        // 格式就是逗号分隔，拼接成一个列表
        zkClient.acquireDistributedLock();
        zkClient.createNode("/taskid-list");
        String taskidList = zkClient.getNodeData();
        logger.info("【SpuBoCountBlot获取到taskid list】taskidList=" + taskidList);
        if(!"".equals(taskidList)) {
            taskidList += "," + taskid;
        } else {
            taskidList += taskid;
        }

        zkClient.setNodeData("/taskid-list", taskidList);
        logger.info("【SpuBoCountBlot设置taskid list】taskidList=" + taskidList);

        zkClient.releaseDistributedLock();
    }


    @Override
    public void execute(Tuple tuple) {
        Long spuBoId = tuple.getLongByField("spuBoId");

        logger.info("【SpuBoCountBlot接收到一个商品id】 spuBoId=" + spuBoId);

        Long count = spuBoCountMap.get(spuBoId);
        if (count == null) {
            count = 0L;
        }
        count ++;

        spuBoCountMap.put(spuBoId, count);

        logger.info("【SpuBoCountBlot完成商品访问次数统计】spuBoId=" + spuBoId + ", count=" + count);
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
