package cn.nothinghere.multiple.controller;

import cn.nothinghere.multiple.dao.alpha.entity.Student;
import cn.nothinghere.multiple.dao.beta.entity.Teacher;
import cn.nothinghere.multiple.pojo.BaseRequest;
import cn.nothinghere.multiple.service.StudentService;
import cn.nothinghere.multiple.service.TeacherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HelloController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    /**
     * 接口调用通过 form 表单的形式提交
     *
     * @param request 请求入参
     * @return 查询到的学生信息
     * @throws JsonProcessingException json 解析异常
     */
    @PostMapping("/student")
    public String student(BaseRequest request) throws JsonProcessingException {
        log.info(request.toString());
        Student student = studentService.query(request.getId());
        return new ObjectMapper().writeValueAsString(student);
    }

    /**
     * 接口调用通过 application/json 的方式
     *
     * @param request 请求入参
     * @return 查询到的学生信息
     */
    @PostMapping("/teacher")
    public Teacher teacher(@RequestBody BaseRequest request) {
        log.info(request.toString());
        return teacherService.query(request.getId());
    }
}
