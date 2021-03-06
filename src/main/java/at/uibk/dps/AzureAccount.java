package at.uibk.dps;


/**
 * Class for Microsoft Azure Functions access key
 * */
public class AzureAccount {
    private String azureKey;

    public AzureAccount(){ }

    public AzureAccount(String azureKey){
        this.azureKey = azureKey;

    }

    public void setAzureKey(String azureKey){
        this.azureKey = azureKey;

    }

    public String getAzureKey(){
        return this.azureKey;

    }

}
