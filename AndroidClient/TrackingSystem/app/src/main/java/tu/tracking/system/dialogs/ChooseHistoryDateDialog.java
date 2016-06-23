package tu.tracking.system.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.GregorianCalendar;

import tu.tracking.system.R;
import tu.tracking.system.interfaces.ChangeHistoryDateDialogListener;
import tu.tracking.system.utilities.DateManager;

public class ChooseHistoryDateDialog {
    private Context context;
    private ChangeHistoryDateDialogListener delegate;
    private DatePicker.OnDateChangedListener listener;
    int mYear = 0;
    int mMonth = 0;
    int mDay = 0;

    public ChooseHistoryDateDialog(Context context, ChangeHistoryDateDialogListener delegate){
        this.context = context;
        this.delegate = delegate;

        listener = new DatePicker.OnDateChangedListener() {

            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
            }
        };
    }

    public void show(String title, GregorianCalendar currDate){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        dialog.setContentView(R.layout.dialog_choose_date);
        dialog.setTitle(title);
        dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.mipmap.radar);

        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePickerHistory);
        datePicker.init(mYear, mMonth, mDay, listener);
        datePicker.setCalendarViewShown(false);
        int[] dateNumbers = DateManager.getDateInNumbersFromGregorianCalendar(currDate);
        datePicker.updateDate(dateNumbers[2], dateNumbers[1], dateNumbers[0]);
        datePicker.setMaxDate(DateManager.getTimeInMilisFromGregorianCalendar(new GregorianCalendar()));
        Button buttonDone = (Button) dialog.findViewById(R.id.buttonChooseDateDone);
        Button buttonUpdateCancel = (Button) dialog.findViewById(R.id.buttonChooseDateCancel);
        buttonUpdateCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    dialog.dismiss();
                GregorianCalendar date = DateManager.getGregorianCalendarFromNumbers(mDay,
                        mMonth + 1, mYear, 0, 0);
                    delegate.onChangeHistoryDateDialogDone(date);
            }
        });

        dialog.show();
    }
}
