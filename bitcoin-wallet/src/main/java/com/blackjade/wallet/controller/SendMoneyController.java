package com.blackjade.wallet.controller;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.apis.CSendBitcoin;
import com.blackjade.wallet.utils.BitcoinModel;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.bitcoinj.core.*;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.params.KeyParameter;

import javax.annotation.Nullable;

/**
 * Created by Administrator on 2018/8/14.
 */
public class SendMoneyController {
    private static final Logger log = LoggerFactory.getLogger(SendMoneyController.class);

    private Wallet.SendResult sendResult;
    private KeyParameter aesKey;

    private static SendMoneyController sendMoneyController = new SendMoneyController();

/*    public static synchronized String sendBitcoin(String amountStr ,String address){
//        SendMoneyController sendMoneyController = new SendMoneyController();
        String result = sendMoneyController.send(amountStr,address);
        return result;
    }*/

    public String send(CSendBitcoin cSendBitcoin) throws Exception {

        String amountStr = cSendBitcoin.getAmount();
        String address = cSendBitcoin.getAddress();
        String transactionId = "";

        // Address exception cannot happen as we validated it beforehand.
        try {
            Coin amount = Coin.parseCoin(amountStr);
            Address destination = Address.fromBase58(BitcoinWalletApplication.params, address);
            SendRequest req;
            if (amount.equals(BitcoinWalletApplication.bitcoin.wallet().getBalance()))
                req = SendRequest.emptyWallet(destination);
            else
                req = SendRequest.to(destination, amount);
            req.aesKey = aesKey;

            // chaolumen start
            req.setOrderId(cSendBitcoin.getOrderid());
            req.setOperateType(cSendBitcoin.getOperateType());
            req.setClientId(cSendBitcoin.getClientid());
            req.setPlatformFee(cSendBitcoin.getPlatformFee());
            req.setPnsid(cSendBitcoin.getPnsid());
            req.setPnsgid(cSendBitcoin.getPnsgid());
            req.setParams(BitcoinWalletApplication.params);
            req.setAmount(amount.getValue());
            req.setReceiveAddress(cSendBitcoin.getAddress());
            // chaolumen end

            sendResult = BitcoinWalletApplication.bitcoin.wallet().sendCoins(req);

            // 加入待确认map
            String txid = sendResult.tx.getHashAsString();
            BitcoinModel.addVerifyingTx(txid,1);

            Futures.addCallback(sendResult.broadcastComplete, new FutureCallback<Transaction>() {
                @Override
                public void onSuccess(@Nullable Transaction result) {
                    // 检查线程，是否有这个必要呢
//                    checkGuiThread();
                    log.info("pay successfully");
                }

                @Override
                public void onFailure(Throwable t) {
                    // We died trying to empty the wallet.
                    log.info(t.getMessage(), t);
                }
            });
            sendResult.tx.getConfidence().addEventListener((tx, reason) -> {
                if (reason == TransactionConfidence.Listener.ChangeReason.SEEN_PEERS)
                    updateTitleForBroadcast();
            });

            updateTitleForBroadcast();

            return txid;

        } catch (InsufficientMoneyException e) {
            log.error("Could not empty the wallet",
                    "You may have too little money left in the wallet to make a transaction.");
        } catch (ECKey.KeyIsEncryptedException e) {
            // 需要密码才能进行下一步操作
            askForPasswordAndRetry();
        } catch (Exception e){
            log.error(e.getMessage(),e);
            throw new Exception(e.getMessage());
        }

        return "failed";
    }

    private void updateTitleForBroadcast() {
        final int peers = sendResult.tx.getConfidence().numBroadcastPeers();
        String broadCasting = String.format("Broadcasting ... seen by %d peers", peers);
        log.info(broadCasting);
    }

    private void askForPasswordAndRetry() {
        log.info("need password .........");
    }
}
