package com.blackjade.wallet.utils;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.service.BalanceLogService;
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
                    String url = "http://otc-apm/deposit";

                    long quant = balanceLog.getReceiveAmount() - balanceLog.getPlatformFee();
                    CDepositAcc cDepositAcc = new CDepositAcc();
                    cDepositAcc.setMessageid("7103");
                    cDepositAcc.setRequestid(UUID.randomUUID());
                    cDepositAcc.setClientid(balanceLog.getCustomerId());
                    cDepositAcc.setOid(UUID.fromString(balanceLog.getOrderId()));
                    cDepositAcc.setPnsid(balanceLog.getPnsid());
                    cDepositAcc.setPnsgid(balanceLog.getPnsgid());
                    cDepositAcc.setQuant(quant);
                    cDepositAcc.setTranid(txid);
                    cDepositAcc.setConlvl(ComStatus.DepositOrdStatus.SUCCESS);
                    try {
                        CDepositAccAns cDepositAccAns = restTemplate.postForObject(url, cDepositAcc, CDepositAccAns.class);
                        System.out.println("result:"+cDepositAccAns.getStatus());
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
                    String url = "http://otc-apm/withdraw";

                    CWithdrawAcc cWithdrawAcc = new CWithdrawAcc();
                    cWithdrawAcc.setMessageid("7105");
                    cWithdrawAcc.setRequestid(UUID.randomUUID());
                    cWithdrawAcc.setClientid(balanceLog.getCustomerId());
                    cWithdrawAcc.setOid(UUID.fromString(balanceLog.getOrderId()));
                    cWithdrawAcc.setPnsid(balanceLog.getPnsid());
                    cWithdrawAcc.setPnsgid(balanceLog.getPnsgid());
                    cWithdrawAcc.setQuant(quant);
                    cWithdrawAcc.setTranid(txid);
                    cWithdrawAcc.setConlvl(org.myutils.apis.ComStatus.WithdrawOrdStatus.SUCCESS);
                    try {
                        CWithdrawAccAns cWithdrawAccAns = restTemplate.postForObject(url, cWithdrawAcc, CWithdrawAccAns.class);
                        System.out.println("result : " + cWithdrawAccAns.getStatus());

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
                    // TODO 记录比特币数据库 并向apm发送通知

                    balanceLogService.saveDepositRecord(tx);
                    // TODO 向crm-c发送请求,订单确认中

                    return ;
                }

                if (balanceLog.getOperateType() == 1){//找零事件
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
