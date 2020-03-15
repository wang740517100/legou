package cn.wangkf.client;

import cn.wangkf.LgSearchService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by wangk on 2019-01-16.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= LgSearchService.class)
public class CategoryClientTest {

    @Autowired
    CategoryClient categoryClient;

    public void testQueryCategoryClient() {
        List<String> names = categoryClient.queryNameByIds(Arrays.asList(1L, 2L, 3L)).getBody();
        names.forEach(System.out::println);

    }



}