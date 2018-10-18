package com.blackjade.wallet.service;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.CSendBitcoin;
import com.blackjade.wallet.apis.initiativeReq.dword.CDepositUpdate;
import com.blackjade.wallet.apis.initiativeReq.dword.CDepositUpdateAns;
import com.blackjade.wallet.apis.initiativeReq.dword.CWithdrawReq;
import com.blackjade.wallet.dao.BalanceDao;
import com.blackjade.wallet.utils.Constant;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.myutils.apis.CDepositAcc;
import org.myutils.apis.CDepositAccAns;
import org.myutils.apis.ComStatus;
import org.myutils.model.BalanceLog;
import org.myutils.util.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/8/29.
 */
@Service
public class BalanceLogService {
    private static final Logger log = LoggerFactory.getLogger(BalanceLogService.class);

    @Autowired
    BalanceDao balanceDao;

    public int saveBalanceLog(CSendBitcoin sendBitcoin){

        BalanceLog balanceLog = new BalanceLog();
        balanceLog.setOrderId(sendBitcoin.getOrderid().toString());
        balanceLog.setOperateType(sendBitcoin.getOperateType());
        balanceLog.setCustomerId(sendBitcoin.getClientid());
        balanceLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        balanceLog.setNotifyStatus("0");
        balanceLog.setTradeStatus(StatusEnum.TradeStatus.DEALING.toString());
        balanceLog.setPnsid(sendBitcoin.getPnsid());
        balanceLog.setPnsgid(sendBitcoin.getPnsgid());
        balanceLog.setReceiveAddress(sendBitcoin.getAddress());

        int result = balanceDao.saveRecord(balanceLog);

        return result;
    }

    public int saveBalanceLog(CWithdrawReq cWithdrawReq){

        BalanceLog balanceLog = new BalanceLog();
        balanceLog.setOrderId(cWithdrawReq.getOid().toString());
        balanceLog.setOperateType(Constant.OPERATE_TYPE_WITHDRAW);//出金
        balanceLog.setCustomerId(cWithdrawReq.getClientid());
        balanceLog.setCreateTime(new Timestamp(System.currentTimeMillis()));
        balanceLog.setNotifyStatus("0");
        balanceLog.setTradeStatus(StatusEnum.TradeStatus.DEALING.toString());
        balanceLog.setPnsid(cWithdrawReq.getPnsid());
        balanceLog.setPnsgid(cWithdrawReq.getPnsgid());
        balanceLog.setReceiveAddress(cWithdrawReq.getToaddress());
        balanceLog.setPlatformFee(cWithdrawReq.getFees());
        balanceLog.setReceiveAmount(cWithdrawReq.getQuant());
        balanceLog.setActualAmount(cWithdrawReq.getToquant());

        int result = balanceDao.saveRecord(balanceLog);

        return result;
    }



    public int getCustomerIdByReceiveAddress(String receiveAddress){
        int customerId = balanceDao.getCustomerIdByReceiveAddress(receiveAddress);
        return customerId;
    }

    public int updateStatus(String notifyStatus ,String tradeStatus ,String transactionId){
        BalanceLog balanceLog = new BalanceLog();
        balanceLog.setNotifyStatus(notifyStatus);
        balanceLog.setTradeStatus(tradeStatus);
        balanceLog.setTransactionId(transactionId);

        int result = balanceDao.updateStatus(balanceLog);

        return result;
    }

    public int addCustomerAddressMap(int customerId,String receiveAddress){
        int result = balanceDao.addCustomerAddressMap(customerId, receiveAddress);
        return result;
    }

    public BalanceLog getBalanceLogByTxid(String transactionId){
        BalanceLog balanceLog = balanceDao.getBalanceLogByTxid(transactionId);
        return balanceLog;
    }

    public List<BalanceLog> getVerifyingTxid(){
        List<BalanceLog> balanceLogs = balanceDao.getVerifyingTxid();
        return balanceLogs;
    }

    public int saveDepositRecord(Transaction tx ,Wallet wallet){
        BalanceLog balanceLog = null;
        int result = 0;
        try {
            // 事务ID
            String txid = tx.getHashAsString();

            // 发送地址
            StringBuffer sendAddress = new StringBuffer();
            List<TransactionInput> inputs = tx.getInputs();
            TransactionInput input ;
            for (int i=0;i<inputs.size();i++){
                input = inputs.get(i);
                System.out.println("sendAddress:" + input.getFromAddress());
                sendAddress.append(input.getFromAddress());
                if (i == (inputs.size()-1)){
                    continue;
                }
                sendAddress.append(",");
            }

            // 收款地址 找零地址 收款金额 找零金额
            String receiveAddress = null;
            String changeAddress = null;
            long receiveAmount = 0L;
            long changeAmount = 0L;
            List<TransactionOutput> outputs = tx.getOutputs();
            int size = outputs.size();
//            if (size == 1){//只有收款地址，没有找零地址
//                receiveAddress = outputs.get(0).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
//                receiveAmount = outputs.get(0).getValue().getValue();
//            } else if (size == 2){
//                receiveAddress = outputs.get(0).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
//                receiveAmount = outputs.get(0).getValue().getValue();
//                changeAddress = outputs.get(1).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
//                changeAmount = outputs.get(1).getValue().getValue();
//            }
            for (TransactionOutput output:outputs){//目前仅支持一个收款地址
                if (output.isMineOrWatched(wallet)){
                    receiveAddress = output.getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                    receiveAmount = output.getValue().getValue();
                }
            }

            // 客户ID
            int customerId = getCustomerIdByReceiveAddress(receiveAddress);

            // 矿工费
            long fee = 0L;

            // block深度
            TransactionConfidence transactionConfidence = tx.getConfidence();
            int depth = transactionConfidence.getDepthInBlocks();

            // 平台手续费tx
            long platformFee = 0L;

            // 创建时间
            Timestamp createTime = new Timestamp(System.currentTimeMillis());

            // 通知状态
            String notifyStatus = "0";

            // 交易状态
            String tradeStatus = StatusEnum.TradeStatus.DEPOSIT_VERIFYING.toString();
            UUID orderId = UUID.randomUUID();
            balanceLog = new BalanceLog();
            balanceLog.setOrderId(orderId.toString());
            balanceLog.setOperateType(2);
            balanceLog.setCustomerId(customerId);
            balanceLog.setTransactionId(txid);
            balanceLog.setReceiveAddress(receiveAddress);
            balanceLog.setReceiveAmount(receiveAmount);
            balanceLog.setChangeAddress(changeAddress);
            balanceLog.setSendAddress(sendAddress.toString());
            balanceLog.setFee(fee);
            balanceLog.setConfirmations(depth);
//            balanceLog.setPlatformFee(platformFee); //初始化为Null
//            balanceLog.setActualAmount(0);//初始化为Null
            balanceLog.setCreateTime(createTime);
            balanceLog.setNotifyStatus(notifyStatus);
            balanceLog.setTradeStatus(tradeStatus);
            balanceLog.setPnsid(1);
            balanceLog.setPnsgid(8);

            result = balanceDao.saveDepositRecord(balanceLog);
            RestTemplate restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean("initRestTemplate");
//                String url = "http://crm-c/cCheckEmailUnique";
//                CCheckEmailUnique cCheckEmailUnique = new CCheckEmailUnique();
            log.info("call crm-c to increase money temporarily ...");
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "http://192.168.1.9:8116/deposit";
            String url = "http://crm-c/cDepositUpdate";
            long quant = receiveAmount;//收款时index=1的为接收金额
            long fees = 0L;
            long rcvquant = quant - platformFee;

            CDepositUpdate cDepositUpdate = new CDepositUpdate();
            cDepositUpdate.setRequestid(UUID.randomUUID());
            cDepositUpdate.setMessageid("4003");
            cDepositUpdate.setClientid(balanceLog.getCustomerId());
            cDepositUpdate.setOid(orderId);
            cDepositUpdate.setPnsid(balanceLog.getPnsid());
            cDepositUpdate.setPnsgid(balanceLog.getPnsgid());
            cDepositUpdate.setQuant(quant);
            cDepositUpdate.setFees(0L);
            cDepositUpdate.setRcvquant(rcvquant);
            cDepositUpdate.setToaddress(receiveAddress);
            cDepositUpdate.setTransactionid(txid);
            cDepositUpdate.setConlvl(com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.DepositOrdStatus.PROCEEDING);

            CDepositUpdateAns cDepositUpdateAns = restTemplate.postForObject(url, cDepositUpdate, CDepositUpdateAns.class);
            System.out.println("result:"+cDepositUpdateAns.getStatus());
            if (cDepositUpdateAns.getStatus() == com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.DepositAccStatus.SUCCESS){
                log.info("call apm to increase money temporarily successfully ...");
                balanceLog.setActualAmount(cDepositUpdateAns.getRcvquant());
                balanceLog.setPlatformFee(cDepositUpdateAns.getFees());
                balanceDao.updateDepositRecord(balanceLog);
            } else {
                log.info(cDepositUpdateAns.toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }
}
