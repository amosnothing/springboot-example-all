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
