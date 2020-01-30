package dps.FTinvoker;

/**
 * Class for AWS Account credentials
 */
public class AWSAccount {
	private String awsAccessKey;
	private String awsSecretKey;

	public AWSAccount(String awsAccessKey, String awsSecretKey) {
		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;
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
}
