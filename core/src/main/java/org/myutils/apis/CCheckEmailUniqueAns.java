package org.myutils.apis;

public class CCheckEmailUniqueAns {
	private String requestid;
	private String messageid;
	private String email;
	
	private Enum<?> status;

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

	public Enum<?> getStatus() {
		return status;
	}

	public void setStatus(Enum<?> status) {
		this.status = status;
	}
}
