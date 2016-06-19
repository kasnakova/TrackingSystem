package tu.tracking.system.models;

import tu.tracking.system.utilities.Constants;

/**
 * Created by Liza on 18.6.2016 г..
 */
public class TrackingSystemUserModel {
    private static String token = Constants.EMPTY_STRING;

    public static String getToken(){
        return TrackingSystemUserModel.token;
    }

    public static void setToken(String accessToken){
        TrackingSystemUserModel.token = accessToken;
    }
}
