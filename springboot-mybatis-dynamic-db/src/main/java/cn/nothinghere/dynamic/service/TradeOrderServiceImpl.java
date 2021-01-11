package cn.nothinghere.dynamic.service;

import cn.nothinghere.dynamic.dao.trade.entity.Order;
import cn.nothinghere.dynamic.dao.trade.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author amos
 * @date 2021/1/9
 */
@Service
public class TradeOrderServiceImpl {

    @Autowired(required = false)
    private OrderMapper orderMapper;

    public Order get(int id) {
        return orderMapper.selectByPrimaryKey(id);
    }
}
