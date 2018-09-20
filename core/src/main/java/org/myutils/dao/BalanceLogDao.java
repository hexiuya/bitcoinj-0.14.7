package org.myutils.dao;

import org.apache.ibatis.annotations.Param;
import org.myutils.model.BalanceLog;

/**
 * Created by Administrator on 2018/8/20.
 */
public interface BalanceLogDao {
    public String getTxid();

    public int updateRecord(BalanceLog balanceLog);

    public BalanceLog getBalanceLog(@Param(value = "orderId") String orderId);
}
