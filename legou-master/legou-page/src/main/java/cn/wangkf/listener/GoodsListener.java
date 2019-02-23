package cn.wangkf.listener;

import cn.wangkf.service.GoodsService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class GoodsListener {

    @Autowired
    private GoodsService goodsService;

    /**
     * 处理insert和update的消息
     * @param id
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "page.item.insert.queue", durable = "true"), //队列持久化
            exchange = @Exchange(
                    value = "legou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.insert", "item.update"}
    ))
    public void listenCreate(Long id) throws Exception{
        if (id == null){
            return;
        }
        //创建或更新索引
        goodsService.createHtml(id);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "page.item.delete.queue", durable = "true"), //队列持久化
            exchange = @Exchange(
                    value = "legou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.delete"}
    ))
    public void listenDelete(Long id){
        if (id == null){
            return;
        }

        //删除索引
        goodsService.deleteHtml(id);
    }


}
