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
