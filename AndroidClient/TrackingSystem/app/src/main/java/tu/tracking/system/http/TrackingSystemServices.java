package tu.tracking.system.http;

public class TrackingSystemServices {
    public static final String URL_BASE = "http://37.143.216.196:50264/";//"http://192.168.0.128:50264/";
    public static final String URL_LOGIN = URL_BASE + "Token";
    public static final String URL_REGISTER = URL_BASE + "api/Account/Register";
    public static final String URL_LOGOUT = URL_BASE + "api/Account/Logout";
    public static final String URL_REGISTER_TARGET_IDENTITY = URL_BASE + "api/SpecialSoftware/RegisterIdentifier";
    public static final String URL_SEND_COORDINATES = URL_BASE + "api/SpecialSoftware/ReceiveCoordinates";
    public static final String URL_ADD_TARGET = URL_BASE + "api/Target/AddTarget";
    public static final String URL_GET_TARGETS = URL_BASE + "api/Target/GetTargets";
    public static final String URL_DELETE_TARGET = URL_BASE + "api/Target/DeleteTarget";
    public static final String URL_GET_HISTORY_OF_POSITIONS= URL_BASE + "api/Target/GetHistoryOfPositions";
    public static final String URL_SET_IS_TARGET_ACTIVE = URL_BASE + "api/Target/SetIsTargetActive";
    public static final String URL_SET_SHOULD_TARGET_MOVE = URL_BASE + "api/Target/SetShouldTargetMove";
    public static final String URL_TURN_ALARM_ON = URL_BASE + "api/Target/TurnAlarmOn";
    public static final String URL_GET_TARGETS_POSITION = URL_BASE + "api/Target/GetTargetsPosition";
}
