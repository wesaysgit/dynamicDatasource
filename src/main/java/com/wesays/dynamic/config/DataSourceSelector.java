//package com.wesays.dynamic.config;
//
//import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
//
///**
// * @author wesays
// * @date 2017/12/6.
// * @description 继承了AbstractRoutingDataSource，动态选择数据源
// */
//public class DataSourceSelector extends AbstractRoutingDataSource {
//
//    @Override
//    protected Object determineCurrentLookupKey() {
//        return DynamicDataSourceHolder.getDataSourceType();
//    }
//}
