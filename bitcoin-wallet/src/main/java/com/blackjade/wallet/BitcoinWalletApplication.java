package com.blackjade.wallet;

import com.alibaba.druid.pool.DruidDataSource;
import com.blackjade.wallet.controller.MainController;
import com.blackjade.wallet.controller.SendBtcThread;
import com.blackjade.wallet.controller.WalletSetPasswordController;
import com.blackjade.wallet.utils.BitcoinModel;
import com.google.common.util.concurrent.Service;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.utils.BriefLogFormatter;
import org.bitcoinj.wallet.DeterministicSeed;
import org.mybatis.spring.annotation.MapperScan;
import org.myutils.util.MybatisUtil;
import org.myutils.util.WebTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;

@SpringBootApplication
@MapperScan("com.blackjade.wallet.dao")
@EnableDiscoveryClient
public class BitcoinWalletApplication extends Application {

	private static final Logger log = LoggerFactory.getLogger(BitcoinWalletApplication.class);

//    public static NetworkParameters params = MainNetParams.get();
	public static NetworkParameters params = TestNet3Params.get();
//    public static NetworkParameters params = RegTestParams.get();

	public static final String APP_NAME = "SimpleWallet";
	private static final String WALLET_FILE_NAME = APP_NAME.replaceAll("[^a-zA-Z0-9.-]", "_") + "-"
			+ params.getPaymentProtocolId();

	public static WalletAppKit bitcoin;

	public MainController controller = new MainController();

	public static BitcoinWalletApplication instance;

	public static ApplicationContext applicationContext;

	@Value("${btc-environment}")
	public String btcEnvironment;

	public static void main(String[] args) {
		// 启动web
		ApplicationContext ctx = SpringApplication.run(BitcoinWalletApplication.class, args);
		applicationContext = ctx;

		//启动线程
		SendBtcThread sendBtcThread = new SendBtcThread();
		sendBtcThread.start();
		// 传输applicationContext到调用的包
//		WebTools.setApplicationContext(applicationContext);
		// 初始化restTemplate到调用的包
		RestTemplate restTemplateParam = (RestTemplate) applicationContext.getBean("initRestTemplate");
		WebTools.setRestTemplate(restTemplateParam);

		// 初始化druidDataSource到调用的包
		DruidDataSource druidDataSource = (DruidDataSource) applicationContext.getBean("dataSource");
		MybatisUtil.setDataSource(druidDataSource);

		// 启动wallet
		launch(args);

	}

	private void realStart() throws IOException {
		// 初始化待确认map
		BitcoinModel.initVerifyingTx();

		instance = this;

		// Make log output concise.
		BriefLogFormatter.init();

		// Create the app kit. It won't do any heavyweight initialization until after we realStart it.
		setupWalletKit(null);

		if (bitcoin.isChainFileLocked()) {
			log.info("Already running", "This application is already running and cannot be started twice.");
			Platform.exit();
			return;
		}

		WalletSetPasswordController.estimateKeyDerivationTimeMsec();

		bitcoin.addListener(new Service.Listener() {
			@Override
			public void failed(Service.State from, Throwable failure) {
				log.error(failure.getMessage(),failure);
			}
		}, Platform::runLater);
		bitcoin.startAsync();

		// 触发"Shortcut+F"时，停止下载节点
//		scene.getAccelerators().put(KeyCombination.valueOf("Shortcut+F"), () -> bitcoin.peerGroup().getDownloadPeer().close());
	}

	public void setupWalletKit(@Nullable DeterministicSeed seed) {
		// If seed is non-null it means we are restoring from backup.
		bitcoin = new WalletAppKit(params, new File("./data/"), WALLET_FILE_NAME) {
			@Override
			protected void onSetupCompleted() {
				// Don't make the user wait for confirmations for now, as the intention is they're sending it
				// their own money!
				bitcoin.wallet().allowSpendingUnconfirmedTransactions();
				Platform.runLater(controller::onBitcoinSetup);
			}
		};
		// Now configure and realStart the appkit. This will take a second or two - we could show a temporary splash screen
		// or progress widget to keep the user engaged whilst we initialise, but we don't.
		if (params == RegTestParams.get()) {
			bitcoin.connectToLocalHost();   // You should run a regtest mode bitcoind locally.
		} else if (params == TestNet3Params.get()) {
			// As an example!
//            bitcoin.useTor();
			// bitcoin.setDiscovery(new HttpDiscovery(params, URI.create("http://localhost:8080/peers"), ECKey.fromPublicOnly(BaseEncoding.base16().decode("02cba68cfd0679d10b186288b75a59f9132b1b3e222f6332717cb8c4eb2040f940".toUpperCase()))));
		}
		bitcoin.setDownloadListener(controller.progressBarUpdater())
				.setBlockingStartup(false)
				.setUserAgent(APP_NAME, "1.0");
		if (seed != null)
			bitcoin.restoreWalletFromSeed(seed);
	}

	@PostConstruct
	public void init(){
		System.out.println("比特币连接的环境:"+btcEnvironment);
		if ("test".equals(btcEnvironment)){
			params = TestNet3Params.get();
		}else if ("prod".equals(btcEnvironment)){
			params = MainNetParams.get();
		}
		System.out.println("我被初始化了、、、、、我是用的@PostConstruct的方式、、、、、、");
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		realStart();
	}

	@Override
	public void stop() throws Exception {
		bitcoin.stopAsync();
		bitcoin.awaitTerminated();
		// Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
		Runtime.getRuntime().exit(0);
	}

	@PreDestroy
	public void destory() {
		System.out.println("我被销毁了、、、、、我是用的@PreDestory的方式、、、、、、");
	}


	@Bean
	@LoadBalanced
	RestTemplate initRestTemplate() {
		return new RestTemplate();
	}

}
