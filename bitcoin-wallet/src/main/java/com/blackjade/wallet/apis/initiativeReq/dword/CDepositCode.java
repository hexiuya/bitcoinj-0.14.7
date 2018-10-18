package com.blackjade.wallet.apis.initiativeReq.dword;

import java.util.UUID;
import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.DepositCodeStatus;

//CRMC/GW	cDepositCode	0x4001	{requestid, clientid, pnsid, pnsgid}	HTTP

public class CDepositCode {
	private UUID requestid;
	private String messageid;
	private int clientid;
	private int pnsid;
	private int pnsgid;

	public DepositCodeStatus reviewData() {

		if (!"4001".equals(this.messageid))
			return ComStatus.DepositCodeStatus.WRONGMID;

		// if()
		// return ComStatus.

		return ComStatus.DepositCodeStatus.SUCCESS;
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

	@Override
	public String toString() {
		return "CDepositCode [requestid=" + requestid.toString() + ", messageid=" + messageid + ", clientid=" + clientid
				+ ", pnsid=" + pnsid + ", pnsgid=" + pnsgid + "]";
	}

}
