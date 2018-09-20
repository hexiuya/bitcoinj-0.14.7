package com.blackjade.wallet.controller;

import com.blackjade.wallet.BitcoinWalletApplication;
import com.blackjade.wallet.utils.BitcoinModel;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.blackjade.wallet.BitcoinWalletApplication.bitcoin;

/**
 * Created by Administrator on 2018/8/13.
 */
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(BitcoinWalletApplication.class);

    private BitcoinModel model = new BitcoinModel();

    public void onBitcoinSetup() {
        // chaolumen bitcoin.wallet() 返回一个Wallet对象，该对象里有新生成的address
        // chaolumen model.setWallet(bitcoin.wallet()); model里的address被赋值，用于初始化时展示address
        model.setWallet(bitcoin.wallet());
        // 给address绑定事件 复制，展示二维码等等
//        addressControl.addressProperty().bind(model.addressProperty());
        // 格式化balance
        Coin balanceCoin = model.balanceProperty();
        String balanceStr = MonetaryFormat.BTC.noCode().format(balanceCoin).toString();

        // Don't let the user click send money when the wallet is empty.

        // 展示初始化网络的程度
        showBitcoinSyncMessage();

        // 监听网络初始化进度，若进度<1.0,展示进度；否则停止展示进度
        model.syncProgressProperty().addListener(x -> {
            if (model.syncProgressProperty().get() <= 1.0) {
                showBitcoinSyncMessage();
            }
        });

    }

    private void showBitcoinSyncMessage() {
        // 初始化网络，并展示进度
//        syncItem = Main.instance.notificationBar.pushItem("Synchronising with the Bitcoin network", model.syncProgressProperty());

        log.info("Synchronising with the Bitcoin network" + model.syncProgressProperty());
    }

    public DownloadProgressTracker progressBarUpdater() {
        return model.getDownloadProgressTracker();
    }
}
