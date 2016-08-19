package example.tacademy.com.samplelbs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

import example.tacademy.com.samplelbs.data.POI;

/**
 * Created by Tacademy on 2016-08-19.
 */
public class MyInfoWindow implements GoogleMap.InfoWindowAdapter {
    View infoView;
    TextView nameView, addressView, descView;

    Map<Marker,POI> poiResolver;

    public MyInfoWindow(Context context, Map<Marker,POI> poiResolver) {
        infoView = LayoutInflater.from(context).inflate(R.layout.view_info_window, null);
        nameView = (TextView)infoView.findViewById(R.id.text_name);
        addressView = (TextView)infoView.findViewById(R.id.text_address);
        descView = (TextView)infoView.findViewById(R.id.text_description);
        this.poiResolver = poiResolver;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        POI poi = poiResolver.get(marker);
        nameView.setText(poi.getName());
        addressView.setText(poi.getMiddleAddrName() + " " + poi.getLowerAddrName());
        descView.setText(poi.getDesc());
        return infoView;
    }
}
