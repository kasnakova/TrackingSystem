package tu.tracking.system.utilities;

import android.app.Activity;
import android.widget.ProgressBar;

public class ProgressBarManager {
    private static ProgressBar progressBar;

    public static void showProgressBar(Activity activity){
        //TODO: add progress bar
//        RelativeLayout layout = new RelativeLayout(activity);
//        progressBar = new ProgressBar(activity,null,android.R.attr.progressBarStyleHorizontal);
////        LayoutParams lp = new LayoutParams(
////                550, // Width in pixels
////                LayoutParams.WRAP_CONTENT // Height of progress bar
////        );
////        pb.setLayoutParams(lp);
////        LayoutParams params = (LayoutParams) pb.getLayoutParams();
////       // params.addRule(RelativeLayout.BELOW, tv.getId());
////        pb.setLayoutParams(params);
//        progressBar.getProgressDrawable().setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
////        rl.addView(pb);
//        progressBar.setIndeterminate(true);
//        progressBar.setVisibility(View.VISIBLE);
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
//        params.addRule(RelativeLayout.CENTER_IN_PARENT);
//        layout.addView(progressBar, params);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT , RelativeLayout.LayoutParams.WRAP_CONTENT );
//        activity.setContentView(layout, layoutParams);
    }

    public static void hideProgressBar(){
//        ViewGroup vg = (ViewGroup)(progressBar.getParent());
//        vg.removeView(progressBar);
    }
}
