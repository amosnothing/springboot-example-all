package cn.nothinghere.multiple.service;

import cn.nothinghere.multiple.dao.alpha.entity.Student;
import cn.nothinghere.multiple.dao.alpha.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 属于 alpha 系统
 * @author amos
 */
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    @Autowired
    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public Student query(int id) {
        return studentMapper.selectByPrimaryKey(id);
    }
}
