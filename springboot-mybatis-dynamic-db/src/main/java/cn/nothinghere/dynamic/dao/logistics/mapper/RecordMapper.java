package cn.nothinghere.dynamic.dao.logistics.mapper;

import cn.nothinghere.dynamic.dao.logistics.LogisticsMapper;
import cn.nothinghere.dynamic.dao.logistics.entity.Record;

public interface RecordMapper extends LogisticsMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Record record);

    int insertSelective(Record record);

    Record selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Record record);

    int updateByPrimaryKey(Record record);
}