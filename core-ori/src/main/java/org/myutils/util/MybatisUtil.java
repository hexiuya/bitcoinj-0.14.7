package org.myutils.util;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.myutils.dao.BalanceLogDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2018/8/20.
 */
public class MybatisUtil {

    private static Logger logger = LoggerFactory.getLogger(MybatisUtil.class);

    private static SqlSessionFactory sqlSessionFactory  ;

    private static DruidDataSource druidDataSource;

    public static void setDataSource(DruidDataSource druidDataSourceParam){
        druidDataSource = druidDataSourceParam;
    }

    public static DruidDataSource getDataSource(){
        return druidDataSource;
    }

    public static SqlSession getSqlSession() throws Exception {

        sqlSessionFactory = sqlSessionFactoryBean();
        SqlSession session = sqlSessionFactory.openSession();
        return session;
    }

    private static SqlSessionFactory getSqlSessionFactory() {
        String resource = "mybatis-config.xml";
        SqlSessionFactory sqlSessionFactory = null;
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }
        return sqlSessionFactory;
    }

    public static SqlSessionFactory sqlSessionFactoryBean() {

        SqlSessionFactory sqlSessionFactory = null;
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(getDataSource());

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            // scan mybatis package which under classpath
            sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:BalanceLog.xml"));

            sqlSessionFactory =  sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return sqlSessionFactory;
    }
}
