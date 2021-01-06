package com.wesays.dynamic.config;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mysql.cj.jdbc.Driver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源配置
 **/
@Configuration
@Order(-100)
@MapperScan(basePackages = DataSourceFactory.BASE_PACKAGES, sqlSessionTemplateRef = "sqlSessionTemplate")
public class DataSourceFactory {

    static final String BASE_PACKAGES = "com.wesays.*.mapper";

    private static final String MAPPER_LOCATION = "classpath:com/wesays/*/xml/*.xml";

    private static final String CONFIG_LOCATION = "classpath:mybatis.xml";

    @Bean
    public DataSource master() throws SQLException {
        DruidXADataSource dataSource = new DruidXADataSource();
        dataSource.setUrl("jdbc:mysql://192.168.17.129:3306/dynamic_wesays?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&autoReconnect=true");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setFilters("log4j,wall,mergeStat");
        dataSource.setDriver(new Driver());
        return dataSource;
    }

    @Bean
    public DataSource slave() throws SQLException {
        DruidXADataSource dataSource = new DruidXADataSource();
        dataSource.setUrl("jdbc:mysql://192.168.17.130:3306/dynamic_wesays?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8&autoReconnect=true");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setFilters("log4j,wall,mergeStat");
        dataSource.setDriver(new Driver());
        return dataSource;
    }

    /**
     * 创建支持 XA 事务的 Atomikos 数据源 master
     */
    @Bean
    public DataSource xaMaster(DataSource master) {
        AtomikosDataSourceBean sourceBean = new AtomikosDataSourceBean();
        sourceBean.setXaDataSource((XADataSource) master);
        // 必须为数据源指定唯一标识
        sourceBean.setPoolSize(5);
        sourceBean.setTestQuery("SELECT 1");
        sourceBean.setUniqueResourceName("xaMaster");
        return sourceBean;
    }

    /**
     * 创建支持 XA 事务的 Atomikos 数据源 slave
     */
    @Bean
    public DataSource xaSlave(DataSource slave) {
        AtomikosDataSourceBean sourceBean = new AtomikosDataSourceBean();
        sourceBean.setXaDataSource((XADataSource) slave);
        sourceBean.setPoolSize(5);
        sourceBean.setTestQuery("SELECT 1");
        sourceBean.setUniqueResourceName("xaSlave");
        return sourceBean;
    }


    /**
     * @param xaMaster 数据源 xaMaster
     * @return 数据源 xaMaster 的会话工厂
     */
    @Bean
    public SqlSessionFactory sqlSessionFactoryMaster(DataSource xaMaster)
            throws Exception {
        return createSqlSessionFactory(xaMaster);
    }


    /**
     * @param xaSlave 数据源 xaSlave
     * @return 数据源 xaSlave 的会话工厂
     */
    @Bean
    public SqlSessionFactory sqlSessionFactorySlave(DataSource xaSlave)
            throws Exception {
        return createSqlSessionFactory(xaSlave);
    }


    /***
     * sqlSessionTemplate 与 Spring 事务管理一起使用，以确保使用的实际 SqlSession 是与当前 Spring 事务关联的,
     * 此外它还管理会话生命周期，包括根据 Spring 事务配置根据需要关闭，提交或回滚会话
     * @param sqlSessionFactoryMaster 数据源 ivdp
     * @param sqlSessionFactorySlave 数据源 cockpit
     */
    @Bean
    public CustomSqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactoryMaster, SqlSessionFactory sqlSessionFactorySlave) {
        Map<Object, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();
        sqlSessionFactoryMap.put("master", sqlSessionFactoryMaster);
        sqlSessionFactoryMap.put("slave", sqlSessionFactorySlave);
        CustomSqlSessionTemplate customSqlSessionTemplate = new CustomSqlSessionTemplate(sqlSessionFactoryMaster);
        customSqlSessionTemplate.setTargetSqlSessionFactories(sqlSessionFactoryMap);
        return customSqlSessionTemplate;
    }

    /***
     * 自定义会话工厂
     * @param dataSource 数据源
     * @return :自定义的会话工厂
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        GlobalConfig globalConfig = new GlobalConfig();
        factoryBean.setGlobalConfig(globalConfig);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        //配置读取mapper.xml路径
        factoryBean.setMapperLocations(resolver.getResources(MAPPER_LOCATION));
        //配置读取mybatis.xml文件
        factoryBean.setConfigLocation(resolver.getResource(CONFIG_LOCATION));
        return factoryBean.getObject();
    }

}
