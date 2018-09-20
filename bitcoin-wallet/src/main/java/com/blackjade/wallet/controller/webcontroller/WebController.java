package com.blackjade.wallet.controller.webcontroller;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.*;
import com.blackjade.wallet.controller.SendMoneyController;
import com.blackjade.wallet.service.BalanceLogService;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.myutils.apis.CCheckEmailUnique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Administrator on 2018/8/14.
 */
@RestController
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);

//    @Autowired
    RestTemplate restTemplate;

    @Autowired
    BalanceLogService balanceLogService;

    @RequestMapping(value = "/getBalance")
    public String getBalance(){
        Coin coin = BitcoinWalletApplication.bitcoin.wallet().getBalance();
        long balance = coin.getValue();

        String str = coin.toPlainString();
        System.out.println("balance:" + str);

        String str2 = coin.toFriendlyString();
        System.out.println("balance2:" + str2);

        String url = "http://crm-c/cCheckEmailUnique";
        CCheckEmailUnique cCheckEmailUnique = new CCheckEmailUnique();
        restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean(RestTemplate.class);
        try {
            String result = restTemplate.postForObject(url, cCheckEmailUnique, String.class);
            System.out.println("result:"+result);
        } catch (RestClientException e) {
            e.printStackTrace();
            // TODO 把数据记录在数据库
//                throw new RestClientException(e.getMessage());
        } catch (Exception e){
            e.printStackTrace();

        }

        return str;
    }

    @RequestMapping(value = "/cSendBitcoin")
    public CSendBitcoinAns sendBitcoin(@RequestBody CSendBitcoin cSendBitcoin){
        CSendBitcoinAns cSendBitcoinAns = new CSendBitcoinAns();
        cSendBitcoinAns.setRequestid(cSendBitcoin.getRequestid());
        cSendBitcoinAns.setAmount(cSendBitcoin.getAmount());
        cSendBitcoinAns.setAddress(cSendBitcoin.getAddress());
        cSendBitcoinAns.setOrderid(cSendBitcoin.getOrderid());
        cSendBitcoinAns.setClientid(cSendBitcoin.getClientid());
        cSendBitcoinAns.setPnsid(cSendBitcoin.getPnsid());
        cSendBitcoinAns.setPnsgid(cSendBitcoin.getPnsgid());
        cSendBitcoinAns.setOperateType(cSendBitcoin.getOperateType());
        cSendBitcoinAns.setPlatformFee(cSendBitcoin.getPlatformFee());

        ComStatus.SendBitcoinEnum sendBitcoinEnum = cSendBitcoin.reviewData();
        if (ComStatus.SendBitcoinEnum.SUCCESS != sendBitcoinEnum){
            cSendBitcoinAns.setStatus(sendBitcoinEnum);
            return cSendBitcoinAns;
        }

        balanceLogService.saveBalanceLog(cSendBitcoin);

        String address = cSendBitcoin.getAddress();
        // TODO 要做異常處理
        Address destination = Address.fromBase58(BitcoinWalletApplication.params, address);
        final NetworkParameters parameters = destination.getParameters();
        if (parameters == null){
            log.info("Address is for an unknown network");
            cSendBitcoinAns.setStatus(ComStatus.SendBitcoinEnum.RECEIVE_ADDRESS_IS_INVALID);
            return  cSendBitcoinAns;
        }

        SendMoneyController sendMoneyController = new SendMoneyController();
        String transactionId = null;
        try {
            transactionId = sendMoneyController.send(cSendBitcoin);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            cSendBitcoinAns.setStatus(ComStatus.SendBitcoinEnum.SERVER_BUSY);
            return  cSendBitcoinAns;
        }

        cSendBitcoinAns.setTransactionId(transactionId);
        cSendBitcoinAns.setStatus(ComStatus.SendBitcoinEnum.SUCCESS);

        return cSendBitcoinAns ;
    }

    @RequestMapping(value = "/cGetFreshAddress")
    public CGetFreshAddressAns getFreshAddress(@RequestBody CGetFreshAddress cGetFreshAddress){

        CGetFreshAddressAns getFreshAddressAns = new CGetFreshAddressAns();
        getFreshAddressAns.setRequestid(cGetFreshAddress.getRequestid());
        getFreshAddressAns.setCustomerId(cGetFreshAddress.getCustomerId());
        getFreshAddressAns.setRequestid(cGetFreshAddress.getRequestid());
        // TODO 检查用户是否有收款地址，如果没有，则新生成一个收款地址
        Address address = BitcoinWalletApplication.bitcoin.wallet().freshReceiveAddress();

        String receiveAddress = address.toString();

        int result = 0;
        result = balanceLogService.addCustomerAddressMap(cGetFreshAddress.getCustomerId(),receiveAddress);
        if (result == 0){
            getFreshAddressAns.setStatus(ComStatus.GetFreshAddressEnum.FAILED);
            return getFreshAddressAns;
        }

        getFreshAddressAns.setReceiveAddress(receiveAddress);
        getFreshAddressAns.setStatus(ComStatus.GetFreshAddressEnum.SUCCESS);
        return getFreshAddressAns;
    }

    @RequestMapping(value = "/getCurrentAddress")
    public String getCurrentAddress(){
        // TODO 检查用户是否有收款地址，如果没有，则新生成一个收款地址
        Address address = BitcoinWalletApplication.bitcoin.wallet().currentReceiveAddress();

        return address.toString();
    }

}
