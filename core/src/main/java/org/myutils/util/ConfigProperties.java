package org.myutils.util;

import org.apache.ibatis.io.Resources;

import javax.naming.event.ObjectChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Administrator on 2018/8/31.
 */
public class ConfigProperties {

    private static Properties prop = initProperty();

    public static Object getValue(String key){
        Object obj = prop.get(key);
        return obj;
    }

    public static Properties initProperty(){
        Properties prop = new Properties();
        String resource = "config.properties";
        try {
            InputStream inputStream = Resources.getResourceAsStream(resource);
            prop.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
