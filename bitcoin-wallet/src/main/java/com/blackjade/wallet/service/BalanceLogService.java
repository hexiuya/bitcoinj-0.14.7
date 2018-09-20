package com.blackjade.wallet.service;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.CSendBitcoin;
import com.blackjade.wallet.dao.BalanceDao;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
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

    public int saveDepositRecord(Transaction tx){
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
            if (size == 1){//只有收款地址，没有找零地址
                receiveAddress = outputs.get(0).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                receiveAmount = outputs.get(0).getValue().getValue();
            } else if (size == 2){
                receiveAddress = outputs.get(0).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                receiveAmount = outputs.get(0).getValue().getValue();
                changeAddress = outputs.get(1).getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                changeAmount = outputs.get(1).getValue().getValue();
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
            balanceLog.setPlatformFee(platformFee);
            balanceLog.setCreateTime(createTime);
            balanceLog.setNotifyStatus(notifyStatus);
            balanceLog.setTradeStatus(tradeStatus);
            balanceLog.setPnsid(1);
            balanceLog.setPnsgid(8);

            result = balanceDao.saveDepositRecord(balanceLog);
            RestTemplate restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean("initRestTemplate");
//                String url = "http://crm-c/cCheckEmailUnique";
//                CCheckEmailUnique cCheckEmailUnique = new CCheckEmailUnique();
            log.info("call apm to increase money temporarily ...");
//            RestTemplate restTemplate = new RestTemplate();
//            String url = "http://192.168.1.9:8116/deposit";
            String url = "http://otc-apm/deposit";
            long quant = receiveAmount - platformFee;//收款时index=1的为接收金额
            CDepositAcc cDepositAcc = new CDepositAcc();
            cDepositAcc.setMessageid("7103");
            cDepositAcc.setRequestid(UUID.randomUUID());
            cDepositAcc.setClientid(customerId);
            cDepositAcc.setOid(orderId);
            cDepositAcc.setPnsid(1);
            cDepositAcc.setPnsgid(8);
            cDepositAcc.setQuant(quant);
            cDepositAcc.setTranid(txid);
            cDepositAcc.setConlvl(ComStatus.DepositOrdStatus.PROCEEDING);

            CDepositAccAns cDepositAccAns = restTemplate.postForObject(url, cDepositAcc, CDepositAccAns.class);
            System.out.println("result:"+cDepositAccAns.getStatus());
            if (cDepositAccAns.getStatus() == ComStatus.DepositAccStatus.SUCCESS){
                log.info("call apm to increase money temporarily successfully ...");
            } else {
                log.info(cDepositAccAns.toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }

        return result;
    }
}
