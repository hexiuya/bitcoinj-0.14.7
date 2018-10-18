package com.blackjade.wallet.controller;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.CSendBitcoin;
import com.blackjade.wallet.apis.initiativeReq.dword.CWithdrawReq;
import com.blackjade.wallet.apis.initiativeReq.dword.CWithdrawUpdate;
import com.blackjade.wallet.apis.initiativeReq.dword.CWithdrawUpdateAns;
import com.blackjade.wallet.controller.webcontroller.WebController;
import org.myutils.apis.CWithdrawAccAns;
import org.myutils.apis.ComStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawOrdStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Administrator on 2018/9/28.
 */
public class SendBtcThread extends Thread{

    private static final Logger log = LoggerFactory.getLogger(SendBtcThread.class);

    private static LinkedBlockingDeque<CWithdrawReq> queue = new LinkedBlockingDeque<CWithdrawReq>();

    public static boolean addToQueue(CWithdrawReq cWithdrawReq){
        return queue.offer(cWithdrawReq);
    }

    @Override
    public void run() {

        SendMoneyController sendMoneyController = new SendMoneyController();

        try {
            while (true){
                System.out.println("启动线程.............");
                CWithdrawReq cWithdrawReq = queue.take();
                try {
                    System.out.println("接收到订单："+cWithdrawReq.getOid());
                    String transactionId = sendMoneyController.send(cWithdrawReq);
                    log.info("订单"+cWithdrawReq.getOid()+",已发送到BTC-NET,txid:"+transactionId);

                    CWithdrawUpdate cWithdrawUpdate = new CWithdrawUpdate();
                    cWithdrawUpdate.setRequestid(UUID.randomUUID());
                    cWithdrawUpdate.setMessageid("4007");
                    cWithdrawUpdate.setClientid(cWithdrawReq.getClientid());
                    cWithdrawUpdate.setOid(cWithdrawReq.getOid());
                    cWithdrawUpdate.setPnsid(cWithdrawReq.getPnsid());
                    cWithdrawUpdate.setPnsgid(cWithdrawReq.getPnsgid());
                    cWithdrawUpdate.setToaddress(cWithdrawReq.getToaddress());
                    cWithdrawUpdate.setQuant(cWithdrawReq.getQuant());
                    cWithdrawUpdate.setFees(cWithdrawReq.getFees());
                    cWithdrawUpdate.setToquant(cWithdrawReq.getToquant());
                    if (transactionId != null && transactionId.length() == 64){
                        cWithdrawUpdate.setTransactionid(transactionId);
                        cWithdrawUpdate.setConlvl(WithdrawOrdStatus.PROCEEDING);
                    }else {
                        cWithdrawUpdate.setTransactionid(transactionId);
                        cWithdrawUpdate.setConlvl(WithdrawOrdStatus.FAILED);
                    }

                    RestTemplate restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean("initRestTemplate");
                    String url = "http://crm-c/cWithdrawUpdate";

                    try {
                        CWithdrawUpdateAns cWithdrawUpdateAns = restTemplate.postForObject(url, cWithdrawUpdate, CWithdrawUpdateAns.class);
                        System.out.println("result : " + cWithdrawUpdateAns.getStatus());

                    } catch (Exception e){
                        e.printStackTrace();
                        System.out.println(e.getMessage());

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        while (true){
//            CSendBitcoin cSendBitcoin = queue.peek();
//            System.out.println("线程启动了.............");
//            if(cSendBitcoin != null){
//
//                try {
//                    String transactionId = sendMoneyController.send(cSendBitcoin);
//                    log.info("订单"+cSendBitcoin.getOrderid()+",已发送到BTC-NET,txid:"+transactionId);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

}
