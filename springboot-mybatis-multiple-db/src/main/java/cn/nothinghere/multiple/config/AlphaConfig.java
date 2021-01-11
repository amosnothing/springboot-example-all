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
