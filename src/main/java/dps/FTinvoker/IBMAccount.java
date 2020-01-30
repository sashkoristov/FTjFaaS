package dps.FTinvoker;
/**
 * Class for IBM Account Credentials
 */
public class IBMAccount {
	private String IBMKey;

	public IBMAccount(String IBMKey) {
		this.IBMKey = IBMKey;
	}

	public String getIBMKey() {
		return IBMKey;
	}

	public void setIBMKey(String iBMKey) {
		IBMKey = iBMKey;
	}

}
