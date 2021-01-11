package cn.nothinghere.dynamic.dao.trade.mapper;

import cn.nothinghere.dynamic.dao.trade.TradeMapper;
import cn.nothinghere.dynamic.dao.trade.entity.Order;

public interface OrderMapper extends TradeMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);
}