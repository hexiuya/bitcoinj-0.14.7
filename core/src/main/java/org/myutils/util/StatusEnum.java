package org.myutils.util;

/**
 * Created by Administrator on 2018/8/29.
 */
public class StatusEnum {

    public static enum TradeStatus {
        DEALING,
        START_BLOCKED_FUNDS,
        BLOCKED_FUNDS_FAIDED,
        BLOCKED_FUNDS_SUCCESS,
        SENDING_BTC_NETWORK,
        SEND_BTC_NETWORK_FAILED,
        SEND_BTC_NETWORK_SUCCESS,
        WITHDRAW_SUCCESS,


        DEPOSIT_VERIFYING,
        DEPOSIT_SUCCESS,
        DEPOSIT_FAILED
    }
}
