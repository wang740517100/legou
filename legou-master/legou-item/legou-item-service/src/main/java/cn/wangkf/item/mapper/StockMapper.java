package cn.wangkf.item.mapper;

import cn.wangkf.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;


public interface StockMapper extends Mapper<Stock>, SelectByIdListMapper<Stock,Long> {
}
