package at.uibk.dps;


/**
 * Class for Google Service Account Key
 * */
public class GoogleFunctionAccount {
    private String serviceAccountKey;

    public GoogleFunctionAccount(){

    }

    public GoogleFunctionAccount(String serviceAccountKey){
        this.serviceAccountKey = serviceAccountKey;

    }


    public void setServiceAccountKey(String serviceAccountKey){
        this.serviceAccountKey = serviceAccountKey;

    }

    public String getServiceAccountKey(){
        return this.serviceAccountKey;
    }

}


