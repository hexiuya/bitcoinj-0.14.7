package org.myutils.apis;

import org.myutils.util.CommonUtil;

public class CCheckEmailUnique {
	private String requestid = CommonUtil.getUUID();
	private String messageid = "0x0029";
	
	private String email = "312764087@qq.com";
	

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
