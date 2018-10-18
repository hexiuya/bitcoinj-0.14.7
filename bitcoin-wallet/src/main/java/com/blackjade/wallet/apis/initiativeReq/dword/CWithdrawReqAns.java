package com.blackjade.wallet.apis.initiativeReq.dword;

import java.util.UUID;

import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawAccStatus;
import com.blackjade.wallet.apis.initiativeReq.dword.ComStatus.WithdrawOrdStatus;

//C	CRMC/GW/CNET/APM	cWithdraw	
//cWithdrawAns	
//0x4006	{requestid, clientid, oid, pnsid, pnsgid, toaddress, quant, fees, toquant, transactionid, conlvl, status}	HTTP

public class CWithdrawReqAns {

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
	private WithdrawAccStatus status;


	public CWithdrawReqAns(UUID requestid) {
		this.messageid = "4006";
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

	public WithdrawAccStatus getStatus() {
		return status;
	}

	public void setStatus(WithdrawAccStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "CWithdrawReqAns [requestid=" + requestid.toString() + ", messageid=" + messageid + ", clientid=" + clientid
				+ ", oid=" + oid.toString() + ", pnsid=" + pnsid + ", pnsgid=" + pnsgid + ", toaddress=" + toaddress + ", quant="
				+ quant + ", fees=" + fees + ", toquant=" + toquant + ", transactionid=" + transactionid + ", conlvl="
				+ conlvl + ", status=" + status + "]";
	}



}
