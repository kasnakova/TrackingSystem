package tu.tracking.system.utilities;

public class Constants {
    public static final int MAX_LOG_FILES = 10;
    public static final long INTERVAL_LOCATION_REQUEST = 30000;
    public static final long FASTEST_INTERVAL_LOCATION_REQUEST = INTERVAL_LOCATION_REQUEST - (long)(0.2 * INTERVAL_LOCATION_REQUEST);
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_ADD_TARGET_FIELDS_LENGTH = 2;
    public static final int MAX_ADD_TARGET_FIELDS_LENGTH = 50;
    public static final int MIN_IDENTIFIER_LENGTH = 10;
    public static final int MAX_IDENTIFIER_LENGTH = 255;
    public static final int DARK_BLUE = 0xFF1C2F40;

    public static final String CHANGE_LOCATION_INTERVAL = "changeLocationInterval";
    public static final String LOCATION_INTERVAL = "locationInterval";
    public static final String LOCATION_FASTEST_INTERVAL = "locationFastestInterval";

    public static final String EMPTY_STRING = "";
    public static final String LOG_PATH = "/TrackingSystem/Logs";
    public static final String LOG_FILE_EXTENSION = ".log";

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String IS_DEVICE_ACTIVE = "isDeviceActive";
    public static final String IS_SPECIAL_SOFTWARE_SERVICE_STARTED = "isSpecialSoftwareServiceStarted";
    public static final String TOKEN = "token";
    public static final String EMAIL = "email";

    public static final String TITLE_LOGOUT = "Logout";
    public static final String TITLE_PROBLEM_WITH_LOGIN = "Problem with login";
    public static final String TITLE_PROBLEM_OCCURRED = "Problem occurred";
    public static final String MESSAGE_PROBLEM_OCCURRED = "There is no internet connection or the server is not running";
   public static final String MESSAGE_EMAIL_CANNOT_BE_EMPTY = "Email can't be empty!";
    public static final String MESSAGE_PASSWORD_CANNOT_BE_EMPTY= "Password can't be empty!";
    public static final String MESSAGE_INVALID_EMAIL= "Enter a valid email address!!";
    public static final String MESSAGE_PROBLEM_DELETING_TARGET = "Sorry, but we couldn't delete your target!";
    public static final String MESSAGE_PASSWORD_CONFIRM_NOT_MATCH = "The password confirmation doesn't match!";
    public static final String MESSAGE_PASSWORD_LENGTH = "The password must be at least %d characters long!";
    public static final String MESSAGE_LOGOUT = "Are you sure you want to log out?";
    public static final String TITLE_INVALID_TIME = "Invalid time";
    public static final String MESSAGE_INVALID_TIME = "Your reminder must be after the current time!";

    public static final String JSON_ACCESS_TOKEN = "access_token";
    public static final String JSON_EMAIL = "userName";
    public static final String JSON_ERROR_DESCRIPTION = "error_description";
    public static final String JSON_MESSAGE = "Message";
}
