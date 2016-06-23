package tu.tracking.system.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import tu.tracking.system.R;
import tu.tracking.system.models.PositionModel;

public class PositionRenderer extends DefaultClusterRenderer<PositionModel> {
    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
    private final ImageView mImageView;
    private final RelativeLayout mClusterImageView;
    private final int mDimension;
    private final Context context;
    private final Bitmap icon;

    public PositionRenderer(Context context, GoogleMap map, ClusterManager<PositionModel> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        mIconGenerator = new IconGenerator(context);
        mClusterIconGenerator = new IconGenerator(context);
        mClusterImageView = (RelativeLayout) ((Activity) context).findViewById(R.id.clusterLayout);
        ((ViewGroup) mClusterImageView.getParent()).removeView(mClusterImageView);
        mClusterImageView.setVisibility(View.VISIBLE);
        int dimen = (int) context.getResources().getDimension(R.dimen.custom_cluster_label);
        mClusterImageView.setLayoutParams(new ViewGroup.LayoutParams(dimen, dimen));
        mClusterIconGenerator.setContentView(mClusterImageView);

        mImageView = new ImageView(context.getApplicationContext());
        mDimension = (int) context.getResources().getDimension(R.dimen.custom_label);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        mImageView.setBackgroundResource(android.R.color.white);
        mIconGenerator.setContentView(mImageView);
        mImageView.setImageResource(R.mipmap.radar);
        icon = mIconGenerator.makeIcon();
    }

    @Override
    protected void onBeforeClusterItemRendered(PositionModel PositionModel, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(PositionModel.getLabel());
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<PositionModel> cluster, MarkerOptions markerOptions) {
        TextView tv = (TextView) mClusterImageView.findViewById(R.id.textViewClusterCount);
        String count = String.valueOf(cluster.getSize());
        tv.setText(count);
        Bitmap icon = mClusterIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }
}