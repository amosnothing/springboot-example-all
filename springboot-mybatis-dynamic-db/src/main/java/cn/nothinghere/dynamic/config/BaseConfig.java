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
