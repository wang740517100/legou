package cn.wangkf.blot;

import cn.wangkf.spout.SpuBoInfoSpout;
import com.alibaba.fastjson.JSONObject;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.Map;

/**
 * Created by wangk on 2020-03-22.
 */
public class SpuBoInfoParseBlot extends BaseRichBolt {

    private static final long serialVersionUID = -8017609899644290359L;

    private OutputCollector collector;

    private static final Logger logger = LoggerFactory.getLogger(SpuBoInfoSpout.class);

    @Override
    public void prepare(Map map, TopologyContext topologyContext,
                        OutputCollector outputCollector) {
        this.collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        String message = tuple.getStringByField("message");
        logger.info("【SpuBoInfoParseBlot接收到一条日志】message=" + message);

        JSONObject messageJSON = JSONObject.parseObject(message);
        JSONObject uriArgsJSON = messageJSON.getJSONObject("uri_args");
        Long spuBoId = uriArgsJSON.getLong("spuBoId");

        if (spuBoId != null) {
            collector.emit(new Values(spuBoId));
            logger.info("【SpuBoInfoParseBlot发射出去一个商品id】spuBoId=" + spuBoId);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("productId"));
    }
}
