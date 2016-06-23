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
        int index = service.indexOf('?');
        return index == -1 ? service : service.substring(0, index);
    }

    public String getData(){
        return this.data;
    }
}
