package com.blackjade.wallet.utils;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.initiativeReq.dword.*;
import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus;
import com.blackjade.wallet.service.BalanceLogService;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.bitcoinj.core.*;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.myutils.apis.*;
import org.myutils.model.BalanceLog;
import org.myutils.util.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.DepositOrdStatus;

/**
 * Created by Administrator on 2018/8/13.
 */
@Service
public class BitcoinModel {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    BalanceLogService balanceLogService;

    private final static int DEPTH = 6;

    private SimpleObjectProperty<Address> address = new SimpleObjectProperty<>();
//    private SimpleObjectProperty<Coin> balance = new SimpleObjectProperty<>(Coin.ZERO);

    private SimpleDoubleProperty syncProgress = new SimpleDoubleProperty(-1);
    private ProgressBarUpdater syncProgressUpdater = new ProgressBarUpdater();

    private Coin balance = Coin.ZERO;

    private static Map<String,Integer> verifyingTx = new HashMap<String,Integer>();

    public static void initVerifyingTx(){
        BalanceLogService balanceLogService = (BalanceLogService) BitcoinWalletApplication.applicationContext.getBean("balanceLogService");
        List<BalanceLog> balanceLogs = balanceLogService.getVerifyingTxid();
        for (int i=0;i<balanceLogs.size();i++){
            BalanceLog balanceLog = balanceLogs.get(i);
            addVerifyingTx(balanceLog.getTransactionId(),balanceLog.getOperateType());
        }
    }

    public static void addVerifyingTx(String txid , int operateType){
        Integer count = verifyingTx.get(txid);
        if (count == null){
            verifyingTx.put(txid,new Integer(operateType));
        }
    }

    public static void removeVerifyingTx(String txid){
        verifyingTx.remove(txid);
    }

    public BitcoinModel() {
    }

    public BitcoinModel(Wallet wallet) {
        setWallet(wallet);
    }

    public final void setWallet(Wallet wallet) {

        // 变动事件监听
        wallet.addChangeEventListener(Platform::runLater, new WalletChangeEventListener() {
            @Override
            public void onWalletChanged(Wallet wallet) {
                update(wallet);
            }
        });
        update(wallet);

        // 事务深度变动事件监听
        wallet.addTransactionConfidenceEventListener(Platform::runLater, new TransactionConfidenceEventListener(){

            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                int depth = tx.getConfidence().getDepthInBlocks();

                // 情况2：收到比特币，confidence=1
                // TODO 记录比特币数据库

                // 情况3：收到比特币，confidence=6
                // TODO 记录比特币数据库
                String txid = tx.getHashAsString();
                Integer count = verifyingTx.get(txid);
                if (count == null){
                    return;
                }

//                if (depth < DEPTH){
//                    return;
//                }

                BalanceLogService balanceLogService = (BalanceLogService) BitcoinWalletApplication.applicationContext.getBean("balanceLogService");
                BalanceLog balanceLog = balanceLogService.getBalanceLogByTxid(txid);

                // TODO 向apm发送请求，给用户账户增加钱
                if (count.intValue() == 2){
                    RestTemplate restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean("initRestTemplate");
                    String url = "http://crm-c/cDepositUpdate";

                    long quant = balanceLog.getReceiveAmount();//总金额
                    long fees = balanceLog.getPlatformFee();//平台费用
                    long rcvquant = balanceLog.getActualAmount();//实际应缴金额
                    CDepositUpdate cDepositUpdate = new CDepositUpdate();
                    cDepositUpdate.setRequestid(UUID.randomUUID());
                    cDepositUpdate.setMessageid("4003");
                    cDepositUpdate.setClientid(balanceLog.getCustomerId());
                    cDepositUpdate.setOid(UUID.fromString(balanceLog.getOrderId()));
                    cDepositUpdate.setPnsid(balanceLog.getPnsid());
                    cDepositUpdate.setPnsgid(balanceLog.getPnsgid());
                    cDepositUpdate.setQuant(quant);
                    cDepositUpdate.setFees(fees);
                    cDepositUpdate.setRcvquant(rcvquant);
                    cDepositUpdate.setToaddress(balanceLog.getReceiveAddress());
                    cDepositUpdate.setTransactionid(txid);
                    cDepositUpdate.setConlvl(DepositOrdStatus.SUCCESS);
                    try {
                        CDepositUpdateAns cDepositUpdateAns = restTemplate.postForObject(url, cDepositUpdate, CDepositUpdateAns.class);
                        System.out.println("result:"+cDepositUpdateAns.getStatus());
                    } catch (RestClientException e) {
                        e.printStackTrace();
                        // TODO 把数据记录在数据库
//                throw new RestClientException(e.getMessage());
                    } catch (Exception e){
                        e.printStackTrace();

                    }

                    // TODO 更新数据库
                    balanceLogService.updateStatus("2", StatusEnum.TradeStatus.DEPOSIT_SUCCESS.toString(),txid);

                    // 从MAP中删除
                    removeVerifyingTx(txid);
                }
                // TODO 向apm发送请求，给用户账户减少钱
                if (count.intValue() == 1){

                    long receiveAmount = balanceLog.getReceiveAmount();
                    long fee = balanceLog.getFee();
                    long platformFee = balanceLog.getPlatformFee();
                    long quant = receiveAmount + fee + platformFee;
                    RestTemplate restTemplate = (RestTemplate) BitcoinWalletApplication.applicationContext.getBean("initRestTemplate");
                    String url = "http://crm-c/cWithdrawUpdate";

                    CWithdrawUpdate cWithdrawUpdate = new CWithdrawUpdate();
                    cWithdrawUpdate.setRequestid(UUID.randomUUID());
                    cWithdrawUpdate.setMessageid("4007");
                    cWithdrawUpdate.setClientid(balanceLog.getCustomerId());
                    cWithdrawUpdate.setOid(UUID.fromString(balanceLog.getOrderId()));
                    cWithdrawUpdate.setPnsid(balanceLog.getPnsid());
                    cWithdrawUpdate.setPnsgid(balanceLog.getPnsgid());
                    cWithdrawUpdate.setToaddress(balanceLog.getReceiveAddress());
                    cWithdrawUpdate.setQuant(balanceLog.getReceiveAmount());
                    cWithdrawUpdate.setFees(balanceLog.getPlatformFee());
                    cWithdrawUpdate.setToquant(balanceLog.getActualAmount());
                    cWithdrawUpdate.setTransactionid(balanceLog.getTransactionId());
                    cWithdrawUpdate.setConlvl(ComStatus.WithdrawOrdStatus.SUCCESS);
                    try {
                        CWithdrawUpdateAns cWithdrawUpdateAns = restTemplate.postForObject(url, cWithdrawUpdate, CWithdrawUpdateAns.class);
                        System.out.println("result : " + cWithdrawUpdateAns.getStatus());

                    } catch (RestClientException e) {
                        e.printStackTrace();
                        // TODO 把数据记录在数据库
//                throw new RestClientException(e.getMessage());
                    } catch (Exception e){
                        e.printStackTrace();

                    }

                    // TODO 更新数据库
                    balanceLogService.updateStatus("2", StatusEnum.TradeStatus.WITHDRAW_SUCCESS.toString(),txid);

                    // 从MAP中删除
                    removeVerifyingTx(txid);
                }


                // TODO 向crm-c发送请求，订单已确认
            }


        });

        // 接收事件监听
        wallet.addCoinsReceivedEventListener(Platform::runLater, new WalletCoinsReceivedEventListener(){

            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("比特币接收事件:");
                System.out.println("txid:"+tx.getHash());
                List<TransactionInput> inputs = tx.getInputs();
                for (TransactionInput input : inputs){
                    System.out.println("sendAddress:" + input.getFromAddress());
                    input.getValue();

                }

//                List<TransactionOutput> outputs = tx.getOutputs();
//                for (TransactionOutput output : outputs){
//                    System.out.println("receiveAddress:" + output.getAddressFromP2SH(BitcoinWalletApplication.params));
//                    System.out.println("receiveAddress2:" + output.getAddressFromP2PKHScript(BitcoinWalletApplication.params));
//                }
                TransactionConfidence transactionConfidence = tx.getConfidence();
                int depth = transactionConfidence.getDepthInBlocks();
                System.out.println("确认交易深度：" + depth);

                tx.getFee();
                tx.getLockTime();
                tx.getUpdateTime();

                // 区分付款找零事件还是收款事件
                BalanceLogService balanceLogService = (BalanceLogService) BitcoinWalletApplication.applicationContext.getBean("balanceLogService");
                String txid = tx.getHashAsString();
                BalanceLog balanceLog = balanceLogService.getBalanceLogByTxid(txid);
                if(balanceLog == null || balanceLog.getOperateType() == 2){//收款事件

                    // 加入待确认tx的Map
                    addVerifyingTx(tx.getHashAsString(),2);

                    // TODO 记录比特币数据库 并向crm-c发送通知
                    balanceLogService.saveDepositRecord(tx,wallet);

                    // TODO 监听事务深度变化
                    Futures.addCallback(tx.getConfidence().getDepthFuture(1), new FutureCallback<TransactionConfidence>() {
                        @Override
                        public void onSuccess(TransactionConfidence transactionConfidence) {
                            // TODO 向crm发送请求
                            System.out.println("txid:"+transactionConfidence.getTransactionHash());
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            // TODO 向crm发送请求
                        }
                    });


                    return ;
                }

                if (balanceLog.getOperateType() == 1){//找零事件,如果transaction里没有找零,那么这个method放到这里就是不对的,要放到其他位置
                    String receiveAddress = null;
                    String changeAddress ;
                    List<TransactionOutput> TransactionOutputs =  tx.getOutputs();
                    for (TransactionOutput output : TransactionOutputs){
                        if (output.isMineOrWatched(wallet)) {
                            System.out.println("找零地址：" + output.getAddressFromP2PKHScript(BitcoinWalletApplication.params));
                            changeAddress = output.getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                        }else {
                            System.out.println("收款地址：" + output.getAddressFromP2PKHScript(BitcoinWalletApplication.params));
                            receiveAddress = output.getAddressFromP2PKHScript(BitcoinWalletApplication.params).toString();
                        }
                    }

                    balanceLogService.updateStatus(null, StatusEnum.TradeStatus.SEND_BTC_NETWORK_SUCCESS.toString(),txid);
                }

            }
        });
        // 发送事件监听
        wallet.addCoinsSentEventListener(Platform::runLater, new WalletCoinsSentEventListener(){

            @Override
            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("txid:"+tx.getHashAsString());
                System.out.println("");
            }
        });
    }

    private void update(Wallet wallet) {
        Coin coin = wallet.getBalance();
        System.out.println("balance:"+coin.getValue());
        Address address = wallet.currentReceiveAddress();
        System.out.println("address:"+address);
    }

    private class ProgressBarUpdater extends DownloadProgressTracker {
        @Override
        protected void progress(double pct, int blocksLeft, Date date) {
            super.progress(pct, blocksLeft, date);
            Platform.runLater(() -> syncProgress.set(pct / 100.0));
        }

        @Override
        protected void doneDownload() {
            super.doneDownload();
            Platform.runLater(() -> syncProgress.set(1.0));
        }
    }
    public DownloadProgressTracker getDownloadProgressTracker() { return syncProgressUpdater; }
    public ReadOnlyDoubleProperty syncProgressProperty() { return syncProgress; }
    public ReadOnlyObjectProperty<Address> addressProperty() {
        return address;
    }

    public Coin balanceProperty() {
        return balance;
    }
}
