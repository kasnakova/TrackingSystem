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

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import tu.tracking.system.R;
import tu.tracking.system.adapters.TargetAdapter;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.ITrackingSystemHttpResponse;
import tu.tracking.system.models.TargetModel;

public class TargetsListActivity extends AppCompatActivity implements ITrackingSystemHttpResponse {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "TheMainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ProgressBar mRegistrationProgressBar;
    private TextView mInformationTextView;
    private boolean isReceiverRegistered;
    private ListView listViewTargets;
    private TargetsListActivity context = this;
    private TargetAdapter adapter;
    private TrackingSystemHttpRequester myDiaryHttpRequester;
    private List<TargetModel> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets_list);
        listViewTargets = (ListView) findViewById(R.id.listViewTargets);
        myDiaryHttpRequester = new TrackingSystemHttpRequester(this);
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        
    }
}
