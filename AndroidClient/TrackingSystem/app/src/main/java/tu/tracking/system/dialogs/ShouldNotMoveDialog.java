package tu.tracking.system.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.ShouldNotMoveDialogListener;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TargetModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DateManager;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.FeedbackManager;
import tu.tracking.system.utilities.ProgressBarManager;

public class ShouldNotMoveDialog implements TrackingSystemHttpResponse{
    private final String TAG = "ShouldNotMoveDialog";
    private final Dialog dialog;
    private Context context;
    private ShouldNotMoveDialogListener delegate;
    private DatePicker.OnDateChangedListener listener;
    private boolean shouldNotMove;
    private GregorianCalendar cal;
    private int mYear = 0;
    private int mMonth = 0;
    private int mDay = 0;

    public ShouldNotMoveDialog(Context context, ShouldNotMoveDialogListener delegate){
        this.context = context;
        this.delegate = delegate;
        dialog = new Dialog(context);

        listener = new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        };
    }

    public void show(String title, final TargetModel target){
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_should_not_move);
        dialog.setTitle(title);
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.mipmap.radar);

        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePickerShouldNotMove);
        datePicker.init(mYear, mMonth, mDay, listener);
        final TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePickerShouldNotMove);
        Button buttonDone = (Button) dialog.findViewById(R.id.buttonSNMDone);
        Button buttonUpdateCancel = (Button) dialog.findViewById(R.id.buttonSNMCancel);
        final CheckBox checkBoxShouldNotMove = (CheckBox) dialog.findViewById(R.id.checkBoxShouldNotMove);
        checkBoxShouldNotMove.setChecked(target.getShouldNotMove());
        datePicker.setCalendarViewShown(false);
        timePicker.setIs24HourView(true);

        int[] dateNumbers = DateManager.getDateInNumbersFromGregorianCalendar(target.getShouldNotMoveUntil());
        datePicker.updateDate(dateNumbers[2], dateNumbers[1] - 1, dateNumbers[0]);
        GregorianCalendar now = new GregorianCalendar();
        datePicker.setMinDate(DateManager.getTimeInMilisFromGregorianCalendar(now));
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        buttonUpdateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final TrackingSystemHttpResponse delegate = this;
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GregorianCalendar now = new GregorianCalendar();
                datePicker.clearFocus();
                timePicker.clearFocus();
                if (DateManager.isDateValid(now, datePicker, timePicker)) {
                    shouldNotMove = checkBoxShouldNotMove.isChecked();
                    cal = DateManager.getGregorianCalendarFromNumbers(mDay,
                            mMonth + 1, mYear,
                            timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                    String debug = DateManager.getBGDateTimeStringFromCalendar(cal);
                    ProgressBarManager.showProgressBar((Activity)context);
                    (new TrackingSystemHttpRequester(delegate)).setShouldTargetMove(target.getId(), shouldNotMove, cal);
                } else {
                    DialogManager.makeAlert(context, Constants.TITLE_INVALID_TIME, Constants.MESSAGE_INVALID_TIME);
                }
            }
        });

        dialog.show();
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        ProgressBarManager.hideProgressBar();
        if(result != null){
            if(result.getSuccess()) {
                delegate.onShouldNotMoveDialogDone(shouldNotMove, cal);
                dialog.dismiss();
            } else {
                FeedbackManager.makeToast(context, "Sorry, something went wrong", Toast.LENGTH_SHORT);
            }
        } else {
            FeedbackManager.makeToast(context, "No internet or service!", Toast.LENGTH_SHORT);
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
        }
    }
}
