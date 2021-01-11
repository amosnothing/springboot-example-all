package cn.nothinghere.multiple.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

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
