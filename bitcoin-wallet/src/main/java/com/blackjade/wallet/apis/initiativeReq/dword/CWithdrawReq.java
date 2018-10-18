package com.blackjade.wallet.apis.initiativeReq.dword;

import java.util.UUID;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawAccStatus;

//C	CRMC/GW/CNET/APM	
//cWithdraw	
//0x4005	{requestid, clientid, pnsid, pnsgid, toaddress, quant, fees, toquant}	HTTP
public class CWithdrawReq {

	private UUID requestid;
	private String messageid;
	private int clientid;
	private UUID oid;
	private int pnsid;
	private int pnsgid;
	private String toaddress;
	private long quant;
	private long fees;
	private long toquant;

	public WithdrawAccStatus reviewData() {

		if (!"4005".equals(this.messageid))
			return ComStatus.WithdrawAccStatus.WRONG_MSGID;

		if ((null == this.toaddress) || (this.toaddress == ""))
			return ComStatus.WithdrawAccStatus.IN_MSG_ERR;

		if ((this.quant < 0) || (this.fees < 0) || (this.toquant < 0))
			return ComStatus.WithdrawAccStatus.IN_MSG_ERR;

		if (this.quant != this.fees + this.toquant)
			return ComStatus.WithdrawAccStatus.WRONG_ORD_QUANT;

		return ComStatus.WithdrawAccStatus.SUCCESS;
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

	public UUID getOid() {
		return oid;
	}

	public void setOid(UUID oid) {
		this.oid = oid;
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

	public String getToaddress() {
		return toaddress;
	}

	public void setToaddress(String toaddress) {
		this.toaddress = toaddress;
	}

	public long getQuant() {
		return quant;
	}

	public void setQuant(long quant) {
		this.quant = quant;
	}

	public long getFees() {
		return fees;
	}

	public void setFees(long fees) {
		this.fees = fees;
	}

	public long getToquant() {
		return toquant;
	}

	public void setToquant(long toquant) {
		this.toquant = toquant;
	}
}
