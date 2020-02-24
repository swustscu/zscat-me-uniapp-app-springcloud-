package com.mallplus.cms.config;

import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.parser.ISqlParserFilter;
import com.baomidou.mybatisplus.core.parser.SqlParserHelper;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PerformanceInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.google.common.collect.Lists;
import com.mallplus.common.vo.ApiContext;
import com.mallplus.db.config.DefaultMybatisPlusConfig;
import com.mallplus.sentinel.config.ConstansValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mall
 * @date 2018/12/10
 */
@Configuration
@MapperScan({"com.mallplus.cms.mapper*"})
public class MybatisPlusConfig  {
    private static final List<String> IGNORE_TENANT_TABLES = ConstansValue.IGNORE_TENANT_TABLES;
    @Autowired
    private ApiContext apiContext;
    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {

        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        paginationInterceptor.setDialectType("mysql");
        /*
         * 【测试多租户】 SQL 解析处理拦截器<br>
         * 这里固定写成住户 1 实际情况你可以从cookie读取，因此数据看不到 【 麻花藤 】 这条记录（ 注意观察 SQL ）<br>
         */
        List<ISqlParser> sqlParserList = new ArrayList<>();
        TenantSqlParser tenantSqlParser = new TenantSqlParser();
        tenantSqlParser.setTenantHandler(new TenantHandler() {

            @Override
            public Expression getTenantId() {
                // 从当前系统上下文中取出当前请求的服务商ID，通过解析器注入到SQL中。
                Long currentProviderId = apiContext.getCurrentProviderId();
                if (null == currentProviderId) {
                    currentProviderId = 1L;
                    System.out.println("#1129 getCurrentProviderId error.");
                    //   throw new RuntimeException("#1129 getCurrentProviderId error.");
                }
                return new LongValue(currentProviderId);
            }

            @Override
            public String getTenantIdColumn() {
                return "store_id";
            }

            @Override
            public boolean doTableFilter(String tableName) {
                if (tableName.startsWith("admin_") || tableName.startsWith("QRTZ_")) {
                    return true;
                }
                return IGNORE_TENANT_TABLES.stream().anyMatch((e) -> e.equalsIgnoreCase(tableName));

            }
        });

        sqlParserList.add(tenantSqlParser);
        paginationInterceptor.setSqlParserList(sqlParserList);
        paginationInterceptor.setSqlParserFilter(new ISqlParserFilter() {
            @Override
            public boolean doFilter(MetaObject metaObject) {
                MappedStatement ms = SqlParserHelper.getMappedStatement(metaObject);
                // 过滤自定义查询此时无租户信息约束【 麻花藤 】出现
                if ("com.zscat.mallplus.sys.mapper.SysUserMapper.selectByUserName".equals(ms.getId())) {
                    return true;
                }
                return false;
            }
        });
        return paginationInterceptor;
    }


    /**
     * 性能分析拦截器，不建议生产使用
     * 用来观察 SQL 执行情况及执行时长
     */
    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }
}
