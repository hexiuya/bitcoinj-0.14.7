package com.blackjade.wallet.apis.initiativeReq.dword;

public class ComStatus {

	public static enum DepositCodeStatus{
		SUCCESS,
		WRONGMID,
		FAILED,
		UNKNOWN
	}

	public static enum WithdrawOrdStatus{
		PROCEEDING,
		SUCCESS,
		FAILED,
		REJECT,
		UNKNOWN
	}

	public static enum DepositOrdStatus{
		PROCEEDING,
		SUCCESS,
		FAILED,
		REJECT,
		UNKNOWN
	}

	public static enum WithdrawAccStatus {
		SUCCESS,
		WRONG_MSGID,
		IN_MSG_ERR,
		MISS_ACC_DB,
		MISS_ACC_DB_EX,
		MISS_ORD_DB,
		WRONG_ORD_STATUS,
		WRONG_ORD_QUANT,
		MISS_DWORD_DB,
		APM_REJECT,
		CNET_REJECTED,
		UNKNOWN
	}

	public static enum DepositAccStatus{
		SUCCESS,
		WRONG_MSGID,
		IN_MSG_ERR,
		MISS_ACC_DB,
		MISS_ACC_DB_EX,
		MISS_ORD_DB,
		WRONG_ORD_STATUS,
		WRONG_ORD_QUANT,
		MISS_DWORD_DB,
		APM_REJECT,
		UNKNOWN
	}
	
}
