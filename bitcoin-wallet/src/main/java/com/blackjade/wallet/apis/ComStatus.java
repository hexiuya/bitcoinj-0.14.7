package com.blackjade.wallet.apis;

/**
 * Created by Administrator on 2018/8/29.
 */
public class ComStatus {
    public static enum SendBitcoinEnum {
        REQUEST_ID_IS_EMPTY,
        REQUEST_ID_LENGTH_MORE_THAN_36,
        MESSAGE_ID_IS_INVALID,
        ADDRESS_IS_EMPTY,
        RECEIVE_ADDRESS_IS_INVALID,
        SERVER_BUSY,
        SUCCESS
    }

    public static enum GetFreshAddressEnum {
        REQUEST_ID_IS_EMPTY,
        REQUEST_ID_LENGTH_MORE_THAN_36,
        MESSAGE_ID_IS_INVALID,
        FAILED,
        SUCCESS
    }
}
