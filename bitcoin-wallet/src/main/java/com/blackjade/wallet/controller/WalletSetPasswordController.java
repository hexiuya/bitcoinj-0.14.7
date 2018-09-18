package com.blackjade.wallet.controller;

import com.google.protobuf.ByteString;
import javafx.application.Platform;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.wallet.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Administrator on 2018/8/13.
 */
public class WalletSetPasswordController {
    private static final Logger log = LoggerFactory.getLogger(WalletSetPasswordController.class);

    // These params were determined empirically on a top-range (as of 2014) MacBook Pro with native scrypt support,
    // using the scryptenc command line tool from the original scrypt distribution, given a memory limit of 40mb.
    public static final Protos.ScryptParameters SCRYPT_PARAMETERS = Protos.ScryptParameters.newBuilder()
            .setP(6)
            .setR(8)
            .setN(32768)
            .setSalt(ByteString.copyFrom(KeyCrypterScrypt.randomSalt()))
            .build();

    public static Duration estimatedKeyDerivationTime = null;

    public static CompletableFuture<Duration> estimateKeyDerivationTimeMsec() {
        // This is run in the background after startup. If we haven't recorded it before, do a key derivation to see
        // how long it takes. This helps us produce better progress feedback, as on Windows we don't currently have a
        // native Scrypt impl and the Java version is ~3 times slower, plus it depends a lot on CPU speed.
        CompletableFuture<Duration> future = new CompletableFuture<>();
        new Thread(() -> {
            log.info("Doing background test key derivation");
            KeyCrypterScrypt scrypt = new KeyCrypterScrypt(SCRYPT_PARAMETERS);
            long start = System.currentTimeMillis();
            scrypt.deriveKey("test password");
            long msec = System.currentTimeMillis() - start;
            log.info("Background test key derivation took {}msec", msec);
            Platform.runLater(() -> {
                estimatedKeyDerivationTime = Duration.ofMillis(msec);
                future.complete(estimatedKeyDerivationTime);
            });
        }).start();
        return future;
    }
}
