package tu.tracking.system.interfaces;

import tu.tracking.system.http.HttpResult;

public interface AsyncResponse {
    void processFinish(HttpResult data);
}
