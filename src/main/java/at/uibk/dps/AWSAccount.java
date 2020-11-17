package at.uibk.dps;

/**
 * Class for AWS Account credentials
 */
public class AWSAccount {
	private String awsAccessKey;
	private String awsSecretKey;
	private String awsSecctionToken;

	public AWSAccount(String awsAccessKey, String awsSecretKey, String awsSecctionToken) {
		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;
		this.awsSecctionToken = awsSecctionToken;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getAwsSecctionToken() { return awsSecctionToken; }

	public void setAwsSecctionToken(String awsSecctionToken) { this.awsSecctionToken = awsSecctionToken; }
}
