package org.myutils.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/8/20.
 */
public class CommonUtil {
    public static String getUUID(){
        String uuid = UUID.randomUUID().toString();   //转化为String对象

        System.out.println(uuid);  //打印UUID

        uuid = uuid.replace("-", "");//因为UUID本身为32位只是生成时多了“-”，所以将它们去点就可

        System.out.println(uuid);

        return uuid;
    }

    public static String formatterUUID(UUID uuid){
        String uid = uuid.toString().replace("-", "");//因为UUID本身为32位只是生成时多了“-”，所以将它们去点就可
        return uid;
    }

}
