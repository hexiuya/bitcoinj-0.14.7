package org.myutils.apis;

import org.myutils.util.CommonUtil;

/**
 * Created by Administrator on 2018/8/20.
 */
public class LockMoney {
    private String requestid = CommonUtil.getUUID();
    private String messageid = "9090";

    public String getRequestid() {
        return requestid;
    }

    public void setRequestid(String requestid) {
        this.requestid = requestid;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }
}
