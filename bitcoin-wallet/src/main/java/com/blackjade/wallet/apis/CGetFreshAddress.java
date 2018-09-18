package com.blackjade.wallet.apis;

import java.util.UUID;

/**
 * Created by Administrator on 2018/8/30.
 */
public class CGetFreshAddress {
    private UUID requestid;
    private String messageid;
    private int customerId;

    public ComStatus.GetFreshAddressEnum reviewData(){
        if (this.requestid == null || "".equals(this.requestid)){
            return ComStatus.GetFreshAddressEnum.REQUEST_ID_IS_EMPTY;
        }

        if (this.requestid.toString().length() > 36){
            return ComStatus.GetFreshAddressEnum.REQUEST_ID_LENGTH_MORE_THAN_36;
        }

        if (!"0x1003".equals(this.messageid)){
            return ComStatus.GetFreshAddressEnum.MESSAGE_ID_IS_INVALID;
        }

        return ComStatus.GetFreshAddressEnum.SUCCESS;
    }

    public UUID getRequestid() {
        return requestid;
    }

    public void setRequestid(UUID requestid) {
        this.requestid = requestid;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
}
