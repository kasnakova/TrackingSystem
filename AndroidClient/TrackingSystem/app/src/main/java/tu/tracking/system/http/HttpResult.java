package tu.tracking.system.http;

/**
 * Created by Liza on 18.6.2016 г..
 */
public class HttpResult {
    private boolean success;
    private String service;
    private String data;

    public HttpResult(boolean success, String service, String data){
        this.success = success;
        this.service = service;
        this.data = data;
    }

    public boolean getSuccess(){
        return this.success;
    }

    public String getService(){
        return this.service;
    }

    public String getData(){
        return this.data;
    }
}
