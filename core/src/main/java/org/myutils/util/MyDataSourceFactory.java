package org.myutils.util;


import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.ibatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Administrator on 2018/8/21.
 */
public class MyDataSourceFactory extends DruidDataSourceFactory implements DataSourceFactory {

    protected Properties properties;

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
        System.out.println("properties:"+properties);
    }

    @Override
    public DataSource getDataSource() {
        DataSource dataSource = null;
        if (this.properties == null){
            return dataSource;
        }
        try {

            String publickey = (String) this.properties.get("publickey");
            String password = (String) this.properties.get("password");

            String dbpassword = ConfigTools.decrypt(publickey, password);

            this.properties.put("password",dbpassword);

            dataSource = createDataSource(this.properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }


}
