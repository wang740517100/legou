package cn.wangkf.service;

import cn.wangkf.service.impl.GoodsServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by wangk on 2019-01-19.
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    GoodsServiceImpl goodsService;

    @Test
    public void createHtml(){
        goodsService.createHtml(141L);
    }


}