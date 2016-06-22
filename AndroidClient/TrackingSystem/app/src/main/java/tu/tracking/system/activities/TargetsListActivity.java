/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tu.tracking.system.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import tu.tracking.system.R;
import tu.tracking.system.adapters.TargetAdapter;
import tu.tracking.system.dialogs.ShouldNotMoveDialog;
import tu.tracking.system.gestures.OnTargetTouchListener;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.http.TrackingSystemServices;
import tu.tracking.system.interfaces.ShouldNotMoveDialogListener;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TargetModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.Constants;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.JsonManager;
import tu.tracking.system.utilities.ProgressBarManager;

public class TargetsListActivity extends AppCompatActivity implements TrackingSystemHttpResponse, ShouldNotMoveDialogListener {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "TheMainActivity";

    private ListView listViewTargets;
    private TargetsListActivity context = this;
    private TargetAdapter adapter;
    private TrackingSystemHttpRequester httpRequester;
    private List<TargetModel> targets;
    private TargetModel targetToDelete;
    private TargetModel targetForAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets_list);
        listViewTargets = (ListView) findViewById(R.id.listViewTargets);
        httpRequester = new TrackingSystemHttpRequester(this);
        httpRequester.getTargets();
        ProgressBarManager.showProgressBar(this);
    }

    private void deleteTarget(){
        final int id = targetToDelete.getId();
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage(String.format("Do you want to delete '%s' from your list of targets?", targetToDelete.getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBarManager.showProgressBar(context);
                        httpRequester.deleteTarget(id);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
    }

    private void turnAlarmOn(){
        final int id = targetForAlarm.getId();
        new AlertDialog.Builder(context)
                .setTitle("Alarm")
                .setMessage(String.format("Do you want to sound an alarm on %s?", targetForAlarm.getName()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressBarManager.showProgressBar(context);
                        httpRequester.turnAlarmOn(id);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.radar)
                .show();
    }

    private void populateNoteListView(){
        adapter = new TargetAdapter(context,
                R.layout.listview_target_cell, targets);
        final ShouldNotMoveDialogListener listener = this;
        listViewTargets.setAdapter(adapter);

        listViewTargets.setOnTouchListener(new OnTargetTouchListener(this){
            public void onSwipeRight(MotionEvent e) {
                targetToDelete = getTargetFromList(e);
                if(targetToDelete != null) {
                    deleteTarget();
                }
            }

            @Override
            public void onClick(MotionEvent e) {
                //TODO opraviav servica da e false shouldnotmove ako e izteklo vremeto
                Intent intent = new Intent(context, MainActivity.class);
                TargetModel target = getTargetFromList(e);
                if(target != null) {
                    intent.putExtra("id", target.getId());
                    intent.putExtra("name", target.getName());
                    startActivity(intent);
                }
            }

            @Override
            public void onDoubleTapEvent(MotionEvent e) {
                targetForAlarm = getTargetFromList(e);
                if(targetForAlarm != null){
                    turnAlarmOn();
                }
            }

            @Override
            public void onLongPressEvent(MotionEvent e) {
                TargetModel target = getTargetFromList(e);
                //UI not updates - image not hidden
                if(target != null) {
                    ShouldNotMoveDialog dialog = new ShouldNotMoveDialog(context, listener);
                    dialog.show("Tracking", target);
                }
            }

            @Override
            public void onSwipeLeft(MotionEvent e) {
                targetToDelete = getTargetFromList(e);
                if(targetToDelete != null) {
                    deleteTarget();
                }
            }

            @Override
            public void onSwipeTop() {

            }

            @Override
            public void onSwipeBottom() {

            }
        });

//        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                targetForAlarm = getTargetFromList(e);
//                turnAlarmOn();
//                return true;
//            }
//        });
    }

    private TargetModel getTargetFromList(MotionEvent e){
        ListAdapter listAdapter = listViewTargets.getAdapter();
        float x = e.getRawX();
        float y = e.getRawY();
        int index = listViewTargets.pointToPosition(Math.round(x), Math.round(y));
        if(index < 0 || index >= targets.size()){
            return null;
        }

        return (TargetModel) listAdapter.getItem(index);
    }

    @Override
    public void onShouldNotMoveDialogDone(TargetModel target) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        ProgressBarManager.hideProgressBar();
        if(result != null){
            switch(result.getService()){
                case TrackingSystemServices.URL_GET_TARGETS:
                    if(result.getSuccess()){
                        targets = JsonManager.makeTargetsFromJson(result.getData());
                        if(targets.size() == 0){
                            Toast.makeText(this, "You have no targets.", Toast.LENGTH_SHORT).show();
                        }
                        //TODO show message if there are no targets
                        populateNoteListView();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, "Sorry, but we couldn't retrieve your targets");
                    }
                    break;
                case TrackingSystemServices.URL_DELETE_TARGET:
                    if(result.getSuccess()){
                        targets.remove(targetToDelete);
                        adapter.notifyDataSetChanged();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, Constants.MESSAGE_PROBLEM_DELETING_TARGET);
                    }
                    break;
                case TrackingSystemServices.URL_TURN_ALARM_ON:
                    if(result.getSuccess()){
                        Toast.makeText(this, String.format("Alarm on %s turned on", targetForAlarm.getName()), Toast.LENGTH_SHORT).show();
                    } else {
                        DialogManager.makeAlert(context, Constants.TITLE_PROBLEM_OCCURRED, String.format("Sorry, we couldn't turn the alarm on %s", targetForAlarm.getName()));
                    }
                    break;
                default:
                    break;
            }
        } else {
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
            DialogManager.NoInternetOrServerAlert(context);
        }
    }
}
