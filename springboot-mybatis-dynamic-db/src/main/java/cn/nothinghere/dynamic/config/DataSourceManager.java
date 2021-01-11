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
