package com.blackjade.wallet.apis.initiativeReq.dword;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawAccStatus;
import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawOrdStatus;

import java.util.UUID;

//C	CRMC/GW/CNET/APM	
//cWithdrawUpdate
//0x4007	{requestid, clientid, pnsid, pnsgid, toaddress, quant, fees, toquant}	HTTP

public class CWithdrawUpdate {

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
	private String transactionid;
	private WithdrawOrdStatus conlvl;

	public WithdrawAccStatus reviewData() {

		if (!"4007".equals(this.messageid))
			return ComStatus.WithdrawAccStatus.WRONG_MSGID;

		if ((null != this.toaddress) || (this.toaddress != ""))
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

	public String getTransactionid() {
		return transactionid;
	}

	public void setTransactionid(String transactionid) {
		this.transactionid = transactionid;
	}

	public WithdrawOrdStatus getConlvl() {
		return conlvl;
	}

	public void setConlvl(WithdrawOrdStatus conlvl) {
		this.conlvl = conlvl;
	}

	@Override
	public String toString() {
		return "CWithdrawUpdate [requestid=" + requestid + ", messageid=" + messageid + ", clientid=" + clientid
				+ ", oid=" + oid + ", pnsid=" + pnsid + ", pnsgid=" + pnsgid + ", toaddress=" + toaddress + ", quant="
				+ quant + ", fees=" + fees + ", toquant=" + toquant + ", transactionid=" + transactionid + ", conlvl="
				+ conlvl + "]";
	}

}
