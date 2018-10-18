package com.blackjade.wallet.dao;

import org.apache.ibatis.annotations.Param;
import org.myutils.model.BalanceLog;

import java.util.List;

/**
 * Created by Administrator on 2018/8/29.
 */
public interface BalanceDao {

    public int saveRecord(BalanceLog balanceLog);

    public int saveDepositRecord(BalanceLog balanceLog);

    public int updateDepositRecord(BalanceLog balanceLog);

    public int getCustomerIdByReceiveAddress(@Param(value="receiveAddress") String receiveAddress);

    public int updateStatus(BalanceLog balanceLog);

    public BalanceLog getBalanceLogByTxid(@Param(value="transactionId") String transactionId);

    public int addCustomerAddressMap(@Param(value="customerId") int customerId,@Param(value="receiveAddress") String receiveAddress);

    public List<BalanceLog> getVerifyingTxid();
}
