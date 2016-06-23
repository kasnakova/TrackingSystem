package tu.tracking.system.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class PositionModel implements ClusterItem{
    private LatLng coordinates;
    private String label;

    public PositionModel(double latitude, double longitude, String targetName){
        coordinates = new LatLng(latitude, longitude);
        this.label = targetName;
    }

    public double getLatitude() {
        return coordinates.latitude;
    }

    public double getLongitude() {
        return coordinates.longitude;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public LatLng getPosition() {
        return coordinates;
    }
}
