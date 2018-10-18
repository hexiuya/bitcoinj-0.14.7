package org.myutils.model;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Administrator on 2018/8/28.
 */
public class BalanceLog {
    private long id;
    private String orderId;
    private int operateType;
    private int customerId;
    private String transactionId;
    private String receiveAddress;
    private long receiveAmount;
    private String changeAddress;
    private String sendAddress;
    private long fee;
    private int confirmations;
    private long platformFee;
    private long actualAmount;
    private Timestamp createTime;
    private String notifyStatus;
    private String tradeStatus;
    private Timestamp updateTime;
    private int pnsid;
    private int pnsgid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getOperateType() {
        return operateType;
    }

    public void setOperateType(int operateType) {
        this.operateType = operateType;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public long getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(long receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public String getChangeAddress() {
        return changeAddress;
    }

    public void setChangeAddress(String changeAddress) {
        this.changeAddress = changeAddress;
    }

    public String getSendAddress() {
        return sendAddress;
    }

    public void setSendAddress(String sendAddress) {
        this.sendAddress = sendAddress;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public long getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(long platformFee) {
        this.platformFee = platformFee;
    }



    public String getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(String notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    public String getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(String tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public int getPnsid() {
        return pnsid;
    }

    public void setPnsid(int pnsid) {
        this.pnsid = pnsid;
    }

    public int getPnsgid() {
        return pnsgid;
    }

    public void setPnsgid(int pnsgid) {
        this.pnsgid = pnsgid;
    }

    public long getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(long actualAmount) {
        this.actualAmount = actualAmount;
    }
}
