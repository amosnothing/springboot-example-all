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