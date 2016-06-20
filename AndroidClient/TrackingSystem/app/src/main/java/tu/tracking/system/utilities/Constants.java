package tu.tracking.system.utilities;

public class Constants {
    public static final int MAX_LOG_FILES = 10;
    public static final int INTERVAL_LOCATION_REQUEST = 10000;
    public static final int FASTEST_INTERVAL_LOCATION_REQUEST = 5000;
    public static final int MIN_PASSWORD_LENGTH = 6;

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
    public static final String TITLE_NO_CONNECTION = "No connection to internet or server";
    public static final String MESSAGE_PROBLEM_OCCURRED = "Please check your internet connection or whether the server is running";
    public static final String MESSAGE_DELETE_REMINDER= "Do you want to delete this reminder?";
    public static final String MESSAGE_DELETE_REMINDERS = "Do you want to delete all reminders?";
    public static final String MESSAGE_NO_CONNECTION = "Please check your connection. You will be redirected to the login screen";
    public static final String MESSAGE_EMAIL_CANNOT_BE_EMPTY = "Email can't be empty!";
    public static final String MESSAGE_PASSWORD_CANNOT_BE_EMPTY= "Password can't be empty!";
    public static final String MESSAGE_PROBLEM_DELETING_NOTE= "Sorry, but we couldn't delete your note!";
    public static final String MESSAGE_NAME_CANNOT_BE_EMPTY= "Name can't be empty!";
    public static final String MESSAGE_PASSWORD_CONFIRM_NOT_MATCH = "The password confirmation doesn't match!";
    public static final String MESSAGE_PASSWORD_LENGTH = "The password must be at least %d characters long!";
    public static final String MESSAGE_LOGOUT = "Are you sure you want to log out?";

    public static final String JSON_ACCESS_TOKEN = "access_token";
}
