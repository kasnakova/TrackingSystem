package tu.tracking.system.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tu.tracking.system.R;
import tu.tracking.system.utilities.AlarmManager;
import tu.tracking.system.utilities.FeedbackManager;

public class StopAlarmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_alarm);
        Bundle extras = getIntent().getExtras();
        setTitle(extras.getString("userName"));
        Button btnStopAlarm = (Button) findViewById(R.id.buttonStopAlarm);
        final Activity context = this;
        btnStopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmManager.stop();
                FeedbackManager.makeToast(context, "Alarm stopped.", Toast.LENGTH_LONG);
                finish();
            }
        });
    }
}
