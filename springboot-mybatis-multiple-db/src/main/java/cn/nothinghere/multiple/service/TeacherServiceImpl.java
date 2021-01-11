package cn.nothinghere.multiple.service;

import cn.nothinghere.multiple.dao.beta.entity.Teacher;
import cn.nothinghere.multiple.dao.beta.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 属于 beta 系统
 * @author amos
 */
@Service
public class TeacherServiceImpl implements TeacherService {

    private final TeacherMapper teacherMapper;

    @Autowired
    public TeacherServiceImpl(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    @Override
    public Teacher query(int id) {
        return teacherMapper.selectByPrimaryKey(id);
    }
}
