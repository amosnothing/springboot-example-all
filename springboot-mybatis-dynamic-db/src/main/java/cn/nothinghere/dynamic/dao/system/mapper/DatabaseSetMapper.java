package cn.nothinghere.dynamic.dao.system.mapper;

import cn.nothinghere.dynamic.dao.system.entity.DatabaseSet;

import java.util.List;

public interface DatabaseSetMapper {
    int deleteByPrimaryKey(Long id);

    int insert(DatabaseSet record);

    int insertSelective(DatabaseSet record);

    DatabaseSet selectByPrimaryKey(Long id);

    List<DatabaseSet> listAll();

    int updateByPrimaryKeySelective(DatabaseSet record);

    int updateByPrimaryKey(DatabaseSet record);
}