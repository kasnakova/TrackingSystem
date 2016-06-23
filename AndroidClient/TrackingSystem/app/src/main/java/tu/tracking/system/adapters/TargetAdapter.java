package tu.tracking.system.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.GregorianCalendar;
import java.util.List;

import tu.tracking.system.R;
import tu.tracking.system.http.HttpResult;
import tu.tracking.system.http.TrackingSystemHttpRequester;
import tu.tracking.system.interfaces.TrackingSystemHttpResponse;
import tu.tracking.system.models.TargetModel;
import tu.tracking.system.utilities.AndroidLogger;
import tu.tracking.system.utilities.DialogManager;
import tu.tracking.system.utilities.FeedbackManager;
import tu.tracking.system.utilities.ProgressBarManager;

/**
 * Created by Liza on 21.6.2016 г..
 */
public class TargetAdapter extends ArrayAdapter implements TrackingSystemHttpResponse {
    private final String TAG = "TargetAdapter";
    private Context context;
    private int layoutResourceId;
    private List<TargetModel> targets;
    private TrackingSystemHttpRequester httpRequester;
    private Switch changedSwitch;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

    public TargetAdapter(Context context, int layoutResourceId, List<TargetModel> targets){
        super(context, layoutResourceId, targets);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.targets = targets;
        httpRequester = new TrackingSystemHttpRequester(this);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        TargetHolder holder = null;
        final TargetModel target = targets.get(position);

        if(view == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            view = inflater.inflate(layoutResourceId, parent, false);

            holder = new TargetHolder();
            holder.type = (TextView) view.findViewById(R.id.textViewType);
            holder.name = (TextView) view.findViewById(R.id.textViewName);
            holder.isActive = (Switch) view.findViewById(R.id.switchIsActive);
            holder.shouldNotMove = (ImageView) view.findViewById(R.id.imageViewShouldNotMove);
            view.setTag(holder);

        } else {
            holder = (TargetHolder) view.getTag();
        }

        holder.type.setText(target.getType());
        String name = target.getName();
        holder.name.setText(name);
        holder.isActive.setChecked(target.getIsActive());
        boolean shouldNotMove = target.getIsActive() && target.getShouldNotMove() && target.getShouldNotMoveUntil().getTimeInMillis() > new GregorianCalendar().getTimeInMillis();
        holder.shouldNotMove.setVisibility(shouldNotMove ? View.VISIBLE : View.INVISIBLE);
        final int id = target.getId();
        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ProgressBarManager.showProgressBar((Activity)context);
                changedSwitch = (Switch) buttonView;
                httpRequester.setIsTargetActive(id, isChecked);
            }
        };

        holder.isActive.setOnCheckedChangeListener(onCheckedChangeListener);

        return view;
    }

    @Override
    public void trackingSystemProcessFinish(HttpResult result) {
        ProgressBarManager.hideProgressBar();
        if(result != null){
            if(!result.getSuccess()){
                switchBack();
            }
        } else {
            switchBack();
            DialogManager.NoInternetOrServerAlert(context);
            AndroidLogger.getInstance().logMessage(TAG, "The result of the http request was null");
        }
    }

    private void switchBack(){
        boolean isActive = changedSwitch.isChecked();
        FeedbackManager.makeToast(context, String.format("Sorry, we couldn't %s your target.", isActive ? "activate" : "deactivate"), Toast.LENGTH_SHORT);
        //This is to avoid infinite calling of onChecked when the action is not successful
        changedSwitch.setOnCheckedChangeListener(null);
        changedSwitch.setChecked(!isActive);
        changedSwitch.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    static class TargetHolder
    {
        TextView type;
        TextView name;
        Switch isActive;
        ImageView shouldNotMove;
    }
}
