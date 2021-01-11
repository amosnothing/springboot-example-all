package cn.nothinghere.dynamic.controller;

import cn.nothinghere.dynamic.dao.logistics.entity.Record;
import cn.nothinghere.dynamic.dao.trade.entity.Order;
import cn.nothinghere.dynamic.pojo.BaseRequest;
import cn.nothinghere.dynamic.service.LogisticsRecordServiceImpl;
import cn.nothinghere.dynamic.service.TradeOrderServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class HelloController {

    @Autowired
    private TradeOrderServiceImpl tradeOrderService;

    @Autowired
    private LogisticsRecordServiceImpl logisticsRecordService;

    /**
     * 接口调用通过 form 表单的形式提交
     *
     * @param request 请求入参
     * @return 查询到的学生信息
     * @throws JsonProcessingException json 解析异常
     */
    @PostMapping("/hello")
    public String hello1(BaseRequest request) throws JsonProcessingException {
        log.info(request.toString());
        // 从 Trade 子系统获取订单
        Order order = tradeOrderService.get(request.getId());
        // 从 Logistics 子系统获取物流信息
        Record record = logisticsRecordService.get(request.getId());
        Map<String, Object> hashMap = new HashMap<>(8);
        hashMap.put("order", order);
        hashMap.put("record", record);
        hashMap.put("env", request.getEnv());

        return new ObjectMapper().writeValueAsString(hashMap);
    }

}
