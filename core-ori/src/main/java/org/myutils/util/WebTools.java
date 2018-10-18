package org.myutils.util;

import org.myutils.apis.CCheckEmailUnique;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/8/22.
 */

public class WebTools {

    public static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext applicationContextParam){
        applicationContext = applicationContextParam;
    }

    public static void setRestTemplate(RestTemplate restTemplateParam){
        restTemplate = restTemplateParam;
    }

    public static RestTemplate getRestTemplate(){
        return restTemplate;
    }

    public static RestTemplate restTemplate;

    public String checkEmailUnique(){

        String result = "";
        String url = "http://crm-c/cCheckEmailUnique";
        CCheckEmailUnique cCheckEmailUnique = new CCheckEmailUnique();
        try {
            result = restTemplate.postForObject(url, cCheckEmailUnique, String.class);
            System.out.println("result:"+result);
        } catch (RestClientException e) {
            e.printStackTrace();
            // TODO 把数据记录在数据库
//                throw new RestClientException(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();

        }
        return result;
    }


}
