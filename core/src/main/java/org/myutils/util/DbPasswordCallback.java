package org.myutils.util;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.util.DruidPasswordCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by Administrator on 2018/8/21.
 */
public class DbPasswordCallback extends DruidPasswordCallback {
    private static final Logger logger = LoggerFactory.getLogger(DbPasswordCallback.class);

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
        String password = (String) properties.get("password");
        String publickey = (String) properties.get("publickey");
        try {
            String dbpassword = ConfigTools.decrypt(publickey, password);
            setPassword(dbpassword.toCharArray());
        } catch (Exception e) {
            logger.error("Druid ConfigTools.decrypt", e);
        }
    }
}
