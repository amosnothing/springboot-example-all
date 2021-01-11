1. 项目通过 IDEA 自动创建
1. mybatis 相关文件的生成全部通过 [mbg](http://mybatis.org/generator/) 自动生成

## 单数据源

单数据源一般在子系统内部使用较多，使用步骤相对简单：

1. properties 文件内配置数据源，springboot 会自动读取
2. mbg 自动生成 entity/Mapper/xml 文件
3. 定义 service，service 自动注入数据库表对应的 Mapper 接口
4. 定义 restful 接口，也就是 RestController 注解的类

基本配置类如下：

- `@MapperScan` 申明需要扫描的 Mapper 文件目录（包括子目录）
- 指定上述扫描 Mapper 对应的`sqlSessionFactory`，在生成 Bean 的时候设置对应的 XML 文件
- 定义 Service/Component 等服务被扫描到的目录（包括子目录）
- `baseDataSource()` 这个方法会自动扫描配置文件内数据库配置（注意：key 一定要和下面的一致，否则扫描不到）。
  
    spring.datasource.url=jdbc:mysql://qa.test.com:3306/example?characterEncoding=utf8
    spring.datasource.username=root
    spring.datasource.password=////
    spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver


```java
package cn.nothinghere.one.config;

import javax.sql.DataSource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@Configuration
@MapperScan(basePackages = {"cn.nothinghere.one.dao.mapper"},
        sqlSessionFactoryRef = "sqlSessionFactory")
@ComponentScan(basePackages = {"cn.nothinghere.one.service"})
public class BaseConfig {

    @Bean
    public DataSource baseDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(baseDataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        fb.setMapperLocations(resolver.getResources("classpath*:/xml/*.xml"));
        return fb.getObject();
    }
}
```

服务类，通过实现服务接口，自动注入 Mapper 对象。然后在需要用到该服务的地方都通过注入接口的方式来注入。

```java
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
```

接口实现类，注入上面定义好的服务接口。

```java
package cn.nothinghere.one.controller;

import cn.nothinghere.one.dao.entity.Student;
import cn.nothinghere.one.pojo.BaseRequest;
import cn.nothinghere.one.service.StudentService;
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

    /**
     * 接口调用通过 form 表单的形式提交
     *
     * @param request 请求入参
     * @return 查询到的学生信息
     * @throws JsonProcessingException json 解析异常
     */
    @PostMapping("/hello1")
    public String hello1(BaseRequest request) throws JsonProcessingException {
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
    @PostMapping("/hello2")
    public Student hello2(@RequestBody BaseRequest request) {
        log.info(request.toString());
        return studentService.query(request.getId());
    }
}
```

## 多数据库

多数据库一般是，A/B/C...多个数据库内有不同的数据库表结构，然后在 Springboot 项目初始化的时候就自动生成对应表的 Mapper Bean。

有几个数据库就需要定义几个配置文件，然后申明该配置文件内数据库 SessionFactory 注入的 XML 文件和绑定的 Mapper 对象。下面以两个数据库为例，分别为 alpha 和 beta，三个四个...同理：

重点：

1. 对应的数据库配置申明对应的 `MapperScan` 路径 - cn.nothinghere.multiple.dao.alpha.mapper & cn.nothinghere.multiple.dao.beta.mapper
2. 对应的数据库配置对应的 `xml` 路径 - classpath*:/alpha/*.xml & classpath*:/beta/*.xml
3. sqlSessionFactoryRef 与该类中定义的 Bean 一致

alpha 数据库配置：

```java
package cn.nothinghere.multiple.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author amos
 */
@Configuration
@MapperScan(basePackages = {"cn.nothinghere.multiple.dao.alpha.mapper"},
        sqlSessionFactoryRef = "alphaSessionFactory")
@ComponentScan(basePackages = {"cn.nothinghere.multiple.service"})
public class AlphaConfig {

    @Bean
    public DataSource alphaDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "alphaSessionFactory")
    @Primary
    public SqlSessionFactory alphaSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(alphaDataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        fb.setMapperLocations(resolver.getResources("classpath*:/alpha/*.xml"));
        return fb.getObject();
    }
}
```

beta 数据库配置：

```java

// import 省略，参考上面的代码

/**
 * @author amos
 */
@MapperScan(basePackages = {"cn.nothinghere.multiple.dao.beta.mapper"},
        sqlSessionFactoryRef = "betaSessionFactory")
@Configuration
public class BetaConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.beta.datasource")
    public DataSource betaDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = "betaSessionFactory")
    public SqlSessionFactory betaSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(betaDataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        fb.setMapperLocations(resolver.getResources("classpath*:/beta/*.xml"));
        return fb.getObject();
    }
}
```

然后，其他的步骤与单数据源完全一致。此处省略。

## 动态数据源

动态数据源与网上的方案实际上是有差异的。具体是根据其他人的帖子+我本身的场景差异定制的一套方案。先说一下我的需求：

- 前端传入一个`env`环境参数，对应不同测试环境，业务流程全部走该环境
- 其他子系统对应不同测试环境有不同的数据库配置信息；（实际场景我们有 3 套测试环境，至少有 5 个子系统（有个子系统还有多个数据源），如果全部写在配置文件里可想而知有多复杂）

实现方案：

1. 初始化数据源：定义为 system 数据源，这个配置写在配置文件里，其他的数据库配置是从数据库里面取
2. 测试环境有多套环境（本次以 2 个环境：alpha/beta 为例），多个子系统（本次以 2 个系统：trade/logistics 为例）
3. 子系统的配置全部写到一张表里面，表内字段分别标明，jdbc 四要素：url/username/password/driver

|env|module|url|username|password|driver|enable|
|--|--|--|--|--|--|--|
|alpha|trade|url1|username1|password1|driver1|true|
|alpha|trade|url2|username2|password2|driver2|true|
|beta|logistics|url3|username3|password3|driver3|true|
|beta|logistics|url4|username4|password4|driver4|true|

4. 前端传入过来的 `env` 参数通过切片的方式获取并写入到当前线程里，用于当前线程的所有数据操作
5. springboot 启动的时候会获取到上述的数据库配置，然后根据 Mapper 类型判断当前增删改查操作属于哪一个子系统
6. 根据上面两个步骤拿到 env 与 module，也就对应上了不同的 sessionFactory

当前项目基本配置类，几个重点：

1. 扫描的 mapper 路径只需要指定为 system 数据源即可，也就是说其他动态数据源对应的 mapper 不需要包含进来
2. 设置的 xml 文件路径同上，也只需要包含 system 数据源即可

```java
package cn.nothinghere.dynamic.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = {"cn.nothinghere.dynamic.dao.system.mapper"},
        sqlSessionFactoryRef = "sqlSessionFactory")
@ComponentScan(basePackages = {"cn.nothinghere.dynamic.service", "cn.nothinghere.dynamic.config"})
public class BaseConfig {

    @Bean
    public DataSource baseDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean fb = new SqlSessionFactoryBean();
        fb.setDataSource(baseDataSource());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // 指向默认的 xml 文件
        fb.setMapperLocations(resolver.getResources("classpath*:/system/*.xml"));
        return fb.getObject();
    }
}
```

切面处理类，包含切换 env，以及 Mapper 对象注入 SqlSession，几个重点：

1. `ENV_HOLDER` 用来存储和获取当前线程内的 env 变量
2. 一个面向 service 切面，Mapper 对象一般是在 service 类里面做操作
3. 一个面向 restful 接口切面，因为 env 参数是从这里作为入口传进来的

```java
package cn.nothinghere.dynamic.config;

import cn.nothinghere.dynamic.pojo.BaseRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author amos
 * @date 2021/1/9
 */
@Slf4j
@Aspect
@Component
public class DynamicDataSourceContext {

    private static final ThreadLocal<String> ENV_HOLDER = new ThreadLocal<>();

    public static String getEnv() {
        return ENV_HOLDER.get();
    }

    /**
     * service切面
     */
    @Pointcut("execution(* cn.nothinghere.dynamic.service..*(..))")
    private void servicePointCut() {
        throw new UnsupportedOperationException();
    }

    /**
     * restController 切面
     */
    @Pointcut("execution(* cn.nothinghere.dynamic.controller..*(..))")
    private void controllerPointCut() {
        throw new UnsupportedOperationException();
    }

    /**
     * 根据 controller 入参类型获取到对应的环境
     * 将当前线程内的环境参数为某个固定的值，比如：alpha/beta/...
     *
     * @param joinPoint
     */
    @Before("controllerPointCut()")
    public void setEnv(JoinPoint joinPoint) {
        String env = getEnv();
        Object[] args = joinPoint.getArgs();
        if (args.length != 0) {
            Object arg = args[0];
            // 可以自行添加或者删除某分支
            if (arg instanceof BaseRequest) {
                env = ((BaseRequest) arg).getEnv();
            } else if (arg instanceof Map) {
                env = (String) ((Map) arg).get("env");
            }
            // 如果入参是 String ，请将 env 放置于形参第一位
            else if (arg instanceof String) {
                env = (String) arg;
            }
        }
        if (env == null) {
            // set env as default: alpha
            env = "alpha";
        }
        ENV_HOLDER.set(env);
        log.info("切换为环境{}成功.", env);
    }

    @Before("servicePointCut()")
    public void changeDataSource(JoinPoint joinPoint) {
        Object service = joinPoint.getTarget();
        try {
            // 给切面获取到的 mapper 对象，绑定sqlSession
            for (Field field : service.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class<?> aClass = Class.forName(field.getType().getName());
                String module = ModuleType.ofName(aClass);
                if (module != null) {
                    SqlSessionFactory sqlSessionFactory = DataSourceManager.selectSqlSessionFactory(getEnv() + "-" + module);
                    SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
                    Object mapper = sqlSessionTemplate.getMapper(field.getType());
                    field.set(service, mapper);
                    log.debug("[{}]适配[{}]数据源成功.", fieldName, getEnv());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void removeEnv() {
        ENV_HOLDER.remove();
    }

}
```

数据库内动态数据源初始化操作类，

```java
package cn.nothinghere.dynamic.config;

import cn.nothinghere.dynamic.dao.system.entity.DatabaseSet;
import cn.nothinghere.dynamic.dao.system.mapper.DatabaseSetMapper;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author amos
 * @date 2021/1/9
 */
@Slf4j
@Configuration
public class DataSourceManager {

    @Autowired
    private DatabaseSetMapper databaseSetMapper;

    private static final Map<String, DataSource> DATA_SOURCE_MAP = new HashMap<>(8);

    public static SqlSessionFactory selectSqlSessionFactory(String key) {
        DataSource dataSource = DATA_SOURCE_MAP.get(key);
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        // 根据系统名 指定对应mapper.xml文件
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // mapper.xml文件扫描路径
        try {
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources(
                    MessageFormat.format("classpath:/{0}/*Mapper.xml", module(key))));
            return sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            String format = MessageFormat.format("获取{0}数据库session失败.", key);
            throw new RuntimeException(format, e);
        }
    }

    public void init() {
        // 查询所有的数据库配置
        List<DatabaseSet> databaseList = databaseSetMapper.listAll();
        for (DatabaseSet database : databaseList) {
            String loggerStr = MessageFormat.format("获取配置: {0}环境/{1}子系统数据库连接", database.getEnv(), database.getModule());
            try {
                DruidDataSource dataSource = new DruidDataSource();
                dataSource.setUrl(database.getUrl());
                dataSource.setUsername(database.getUsername());
                dataSource.setPassword(database.getPassword());
                dataSource.setDriverClassName(database.getDriver());
                // 依据环境名 + 系统名作为key 后面来取同理
                String key1 = database.getEnv();
                String key2 = database.getModule();
                DATA_SOURCE_MAP.put(key1 + "-" + key2, dataSource);
                log.debug(loggerStr + "成功.");
            } catch (Exception e) {
                log.error(loggerStr + "失败.");
            }
        }
    }

    private static String env(String key) {
        Objects.requireNonNull(key);
        return key.split("-")[0];
    }

    private static String module(String key) {
        Objects.requireNonNull(key);
        return key.split("-")[1];
    }
}
```

上述的初始库初始化操作，需要在应用上下文注入的时候申明：

```java
package cn.nothinghere.dynamic.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.servlet.annotation.WebListener;

/**
 * @author amos
 * @date 2021/1/9
 */
@Service
@WebListener
public class DatabaseListener implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        DataSourceManager manager = applicationContext.getBean(DataSourceManager.class);
        manager.init();
    }
}
```

然后在启动项目的时候，便会获取到数据库里面的配置信息，以及讲其存储在一个哈希表中，然后在切片的时候获取到。启动后的日志信息部分如下：

>2021-01-11 15:00:27.690  INFO 3583 --- [           main] com.alibaba.druid.pool.DruidDataSource   : {dataSource-1} inited
2021-01-11 15:00:27.997 DEBUG 3583 --- [           main] c.n.d.d.s.m.DatabaseSetMapper.listAll    : ==>  Preparing: select id, env, module, url, driver, username, password, enable from database_set where enable = true 
2021-01-11 15:00:28.019 DEBUG 3583 --- [           main] c.n.d.d.s.m.DatabaseSetMapper.listAll    : ==> Parameters: 
2021-01-11 15:00:28.041 DEBUG 3583 --- [           main] c.n.d.d.s.m.DatabaseSetMapper.listAll    : <==      Total: 4
2021-01-11 15:00:28.043 DEBUG 3583 --- [           main] c.n.dynamic.config.DataSourceManager     : 获取配置: alpha环境/trade子系统数据库连接成功.
2021-01-11 15:00:28.043 DEBUG 3583 --- [           main] c.n.dynamic.config.DataSourceManager     : 获取配置: beta环境/trade子系统数据库连接成功.
2021-01-11 15:00:28.043 DEBUG 3583 --- [           main] c.n.dynamic.config.DataSourceManager     : 获取配置: alpha环境/logistics子系统数据库连接成功.
2021-01-11 15:00:28.043 DEBUG 3583 --- [           main] c.n.dynamic.config.DataSourceManager     : 获取配置: beta环境/logistics子系统数据库连接成功.

从日志可以看出，第一个 dataSource-1 初始化成功也就是我们前面提到的 system。然后我们重新实现了`ApplicationContextAware`接口，设置运行此对象的 `ApplicationContext`。通常，此调用将用于初始化对象。然后便是 mybatis 的查询操作，获取到了 2 个子系统 2 个环境对应的数据库配置信息。

然后我们调用接口，分别传入不同的env环境参数，便会有对应 DataSource 的初始化过程：

>2021-01-11 15:08:51.705  INFO 3583 --- [nio-8080-exec-3] c.n.d.config.DynamicDataSourceContext    : 切换为环境beta成功.
2021-01-11 15:08:51.705  INFO 3583 --- [nio-8080-exec-3] c.n.dynamic.controller.HelloController   : BaseRequest(env=beta, id=1)
2021-01-11 15:08:51.756 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.config.DynamicDataSourceContext    : [orderMapper]适配[beta]数据源成功.
2021-01-11 15:08:51.831  INFO 3583 --- [nio-8080-exec-3] com.alibaba.druid.pool.DruidDataSource   : {dataSource-2} inited
2021-01-11 15:08:51.918 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.t.m.O.selectByPrimaryKey         : ==>  Preparing: select id, user_id, status, amount from `order` where id = ? 
2021-01-11 15:08:51.923 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.t.m.O.selectByPrimaryKey         : ==> Parameters: 1(Integer)
2021-01-11 15:08:51.939 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.t.m.O.selectByPrimaryKey         : <==      Total: 1
2021-01-11 15:08:51.979 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.config.DynamicDataSourceContext    : [recordMapper]适配[beta]数据源成功.
2021-01-11 15:08:52.026  INFO 3583 --- [nio-8080-exec-3] com.alibaba.druid.pool.DruidDataSource   : {dataSource-3} inited
2021-01-11 15:08:52.101 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.l.m.R.selectByPrimaryKey         : ==>  Preparing: select id, order_id, location, create_time from record where id = ? 
2021-01-11 15:08:52.102 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.l.m.R.selectByPrimaryKey         : ==> Parameters: 1(Integer)
2021-01-11 15:08:52.114 DEBUG 3583 --- [nio-8080-exec-3] c.n.d.d.l.m.R.selectByPrimaryKey         : <==      Total: 1


>2021-01-11 15:09:08.016  INFO 3583 --- [nio-8080-exec-5] c.n.d.config.DynamicDataSourceContext    : 切换为环境alpha成功.
2021-01-11 15:09:08.017  INFO 3583 --- [nio-8080-exec-5] c.n.dynamic.controller.HelloController   : BaseRequest(env=alpha, id=1)
2021-01-11 15:09:08.061 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.config.DynamicDataSourceContext    : [orderMapper]适配[alpha]数据源成功.
2021-01-11 15:09:08.075  INFO 3583 --- [nio-8080-exec-5] com.alibaba.druid.pool.DruidDataSource   : {dataSource-4} inited
2021-01-11 15:09:08.684 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.t.m.O.selectByPrimaryKey         : ==>  Preparing: select id, user_id, status, amount from `order` where id = ? 
2021-01-11 15:09:08.684 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.t.m.O.selectByPrimaryKey         : ==> Parameters: 1(Integer)
2021-01-11 15:09:08.695 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.t.m.O.selectByPrimaryKey         : <==      Total: 1
2021-01-11 15:09:08.719 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.config.DynamicDataSourceContext    : [recordMapper]适配[alpha]数据源成功.
2021-01-11 15:09:08.726  INFO 3583 --- [nio-8080-exec-5] com.alibaba.druid.pool.DruidDataSource   : {dataSource-5} inited
2021-01-11 15:09:08.919 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.l.m.R.selectByPrimaryKey         : ==>  Preparing: select id, order_id, location, create_time from record where id = ? 
2021-01-11 15:09:08.919 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.l.m.R.selectByPrimaryKey         : ==> Parameters: 1(Integer)
2021-01-11 15:09:08.929 DEBUG 3583 --- [nio-8080-exec-5] c.n.d.d.l.m.R.selectByPrimaryKey         : <==      Total: 1

完整的代码已经上传到 github，欢迎查看提出其他意见，感谢。