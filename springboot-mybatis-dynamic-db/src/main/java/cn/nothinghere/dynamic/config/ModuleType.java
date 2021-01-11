package cn.nothinghere.dynamic.config;

import cn.nothinghere.dynamic.dao.Mapper;
import cn.nothinghere.dynamic.dao.logistics.LogisticsMapper;
import cn.nothinghere.dynamic.dao.trade.TradeMapper;

/**
 * @author amos
 * @date 2021/1/8
 */
public enum ModuleType {
    /**
     * 子系统 trade
     */
    TRADE(TradeMapper.class, "trade"),
    /**
     * 子系统 logistics
     */
    LOGISTICS(LogisticsMapper.class, "logistics");

    private Class<? extends Mapper> aClass;
    private String moduleName;

    ModuleType(Class<? extends Mapper> aClass, String moduleName) {
        this.aClass = aClass;
        this.moduleName = moduleName;
    }

    public static String ofName(Class aClass) {
        ModuleType[] types = ModuleType.values();
        for (ModuleType type : types) {
            if (type.aClass.isAssignableFrom(aClass)) {
                return type.moduleName;
            }
        }
        return null;
    }

}
