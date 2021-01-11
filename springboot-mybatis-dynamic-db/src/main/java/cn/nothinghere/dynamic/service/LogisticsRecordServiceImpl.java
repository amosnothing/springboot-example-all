package cn.nothinghere.dynamic.service;

import cn.nothinghere.dynamic.dao.logistics.entity.Record;
import cn.nothinghere.dynamic.dao.logistics.mapper.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author amos
 * @date 2021/1/9
 */
@Service
public class LogisticsRecordServiceImpl {

    @Autowired(required = false)
    private RecordMapper recordMapper;

    public Record get(int id) {
        return recordMapper.selectByPrimaryKey(id);
    }
}
