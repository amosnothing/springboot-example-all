package cn.nothinghere.one.service;

import cn.nothinghere.one.dao.entity.Student;
import cn.nothinghere.one.dao.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
