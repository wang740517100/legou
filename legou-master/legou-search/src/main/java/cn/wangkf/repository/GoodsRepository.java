package cn.wangkf.repository;

import cn.wangkf.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
