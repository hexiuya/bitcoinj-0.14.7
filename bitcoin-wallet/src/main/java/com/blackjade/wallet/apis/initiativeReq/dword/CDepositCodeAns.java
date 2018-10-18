package com.blackjade.wallet.apis.initiativeReq.dword;

import java.util.UUID;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.DepositCodeStatus;

//CRMC/GW	cDepositCodeAns	0x4002	{requestid, clientid, pnsid, pnsgid, depositcode, status}	HTTP

public class CDepositCodeAns {

	private UUID requestid;
	private String messageid;
	private int clientid;
	private int pnsid;
	private int pnsgid;
	private String depositcode;
	private DepositCodeStatus status;

	public CDepositCodeAns(UUID requestid) {
		this.messageid = "4002";
		this.requestid = requestid;
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

	public int getClientid() {
		return clientid;
	}

	public void setClientid(int clientid) {
		this.clientid = clientid;
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

	public String getDepositcode() {
		return depositcode;
	}

	public void setDepositcode(String depositcode) {
		this.depositcode = depositcode;
	}

	public DepositCodeStatus getStatus() {
		return status;
	}

	public void setStatus(DepositCodeStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "CDepositCodeAns [requestid=" + requestid.toString() + ", messageid=" + messageid + ", clientid=" + clientid
				+ ", pnsid=" + pnsid + ", pnsgid=" + pnsgid + ", depositcode=" + depositcode + ", status=" + status
				+ "]";
	}

}
