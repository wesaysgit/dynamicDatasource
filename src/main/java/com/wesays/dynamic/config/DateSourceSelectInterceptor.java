package com.wesays.dynamic.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Locale;
import java.util.Properties;

/**
 * @author wesays
 * @date 2017/12/6.
 * @description 拦截数据库操作，根据sql判断是读还是写，选择不同的数据源
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
@Component
@Slf4j
public class DateSourceSelectInterceptor implements Interceptor {

    /**
     * 正则匹配 insert、delete、update操作
     */
    private static final String REGEX = ".*insert\\\\u0020.*|.*delete\\\\u0020.*|.*update\\\\u0020.*";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        //判断当前操作是否有事务
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        //获取执行参数
        Object[] objects = invocation.getArgs();
        MappedStatement ms = (MappedStatement) objects[0];
        //默认设置使用主库
        String lookupKey = DynamicDataSourceHolder.DB_MASTER;
        if (!synchronizationActive) {
            //读方法
            if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                //selectKey为自增主键（SELECT LAST_INSERT_ID()）方法,使用主库
                if (ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                    lookupKey = DynamicDataSourceHolder.DB_MASTER;
                } else {
                    BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
                    String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replace("[\\t\\n\\r]", " ");
                    //如果是insert、delete、update操作 使用主库
                    if (sql.matches(REGEX)) {
                        lookupKey = DynamicDataSourceHolder.DB_MASTER;
                    } else {
                        //查詢操作，使用从库
                        lookupKey = DynamicDataSourceHolder.DB_SLAVE;
                    }
                }
            }
        } else {
            //使用事务的，使用主库
            lookupKey = DynamicDataSourceHolder.DB_MASTER;
        }

        //设置数据源
        log.info("当前数据源为 {}", lookupKey);
        DynamicDataSourceHolder.setDataSourceType(lookupKey);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            //如果是Executor（执行增删改查操作），则拦截下来
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
