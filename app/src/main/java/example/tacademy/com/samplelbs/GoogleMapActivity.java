package example.tacademy.com.samplelbs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Map;

import example.tacademy.com.samplelbs.data.CarFeature;
import example.tacademy.com.samplelbs.data.CarRouteInfo;
import example.tacademy.com.samplelbs.data.Geometry;
import example.tacademy.com.samplelbs.data.POI;
import example.tacademy.com.samplelbs.data.POIResult;
import example.tacademy.com.samplelbs.manager.NetworkManager;
import example.tacademy.com.samplelbs.manager.NetworkRequest;
import example.tacademy.com.samplelbs.request.POISearchRequest;
import example.tacademy.com.samplelbs.request.RouteRequest;

public class GoogleMapActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener {

    GoogleMap map;
    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;

    EditText keywordView;
    ListView listView;
    ArrayAdapter<POI> mAdapter;

    Map<POI,Marker> markerResolver = new HashMap<>();
    Map<Marker, POI> poiResolver = new HashMap<>();
    RadioGroup typeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        typeView = (RadioGroup)findViewById(R.id.group_type);
        listView = (ListView)findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);
        keywordView = (EditText)findViewById(R.id.edit_keyword);
        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        fragment.getMapAsync(this);

        Button btn = (Button)findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyword = keywordView.getText().toString();
                if (!TextUtils.isEmpty(keyword)) {
                    POISearchRequest request = new POISearchRequest(GoogleMapActivity.this, keyword);
                    NetworkManager.getInstance().getNetworkData(request, new NetworkManager.OnResultListener<POIResult>() {
                        @Override
                        public void onSuccess(NetworkRequest<POIResult> request, POIResult result) {

                            clear();

                            mAdapter.addAll(result.getSearchPoiInfo().getPois().getPoi());
                            for (POI poi : result.getSearchPoiInfo().getPois().getPoi()) {
                                addMarker(poi);
                            }
                            if (result.getSearchPoiInfo().getPois().getPoi().length > 0) {
                                POI poi = result.getSearchPoiInfo().getPois().getPoi()[0];
                                moveMap(poi.getLatitude(), poi.getLongitude());
                            }
                        }

                        @Override
                        public void onFail(NetworkRequest<POIResult> request, int errorCode, String errorMessage, Throwable e) {

                        }
                    });
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final POI poi = (POI) listView.getItemAtPosition(position);
                animateMap(poi.getLatitude(), poi.getLongitude(), new Runnable() {
                    @Override
                    public void run() {
                        Marker m = markerResolver.get(poi);
                        m.showInfoWindow();
                    }
                });
            }
        });

        btn = (Button)findViewById(R.id.btn_capture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        // save file...
                    }
                });
            }
        });

        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start != null && end != null) {
                    RouteRequest request = new RouteRequest(GoogleMapActivity.this,
                            start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
                    NetworkManager.getInstance().getNetworkData(request, new NetworkManager.OnResultListener<CarRouteInfo>() {
                        @Override
                        public void onSuccess(NetworkRequest<CarRouteInfo> request, CarRouteInfo result) {
                            PolylineOptions options = new PolylineOptions();
                            for (CarFeature feature : result.features) {
                                Geometry geometry = feature.geometry;
                                if (geometry.type.equals("LineString")) {
                                    for (int i = 0 ; i < geometry.coordinates.length; i+=2) {
                                        double lat = geometry.coordinates[i+1];
                                        double lng = geometry.coordinates[i];
                                        options.add(new LatLng(lat, lng));
                                    }
                                }
                            }
                            options.color(Color.RED);
                            options.width(5);
                            map.addPolyline(options);
                            start = end = null;
                        }

                        @Override
                        public void onFail(NetworkRequest<CarRouteInfo> request, int errorCode, String errorMessage, Throwable e) {

                        }
                    });
                }
            }
        });
    }

    private void clear(){
        for (int i = 0; i < mAdapter.getCount(); i++) {
            POI poi = mAdapter.getItem(i);
            Marker m = markerResolver.get(poi);
            m.remove();
        }
        mAdapter.clear();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        map.setIndoorEnabled(true);
//        map.setBuildingsEnabled(true);
//        map.setTrafficEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);

        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnCameraMoveListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerDragListener(this);

        map.setInfoWindowAdapter(new MyInfoWindow(this,poiResolver));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();
        Log.i("GoogleMapActivity", "lat : " + latLng.latitude + ", lng : " + latLng.longitude );
    }

    POI start, end;

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
        POI poi = poiResolver.get(marker);
        Log.i("GoogleMapActivity", "addr : " + poi.getUpperAddrName());
        switch (typeView.getCheckedRadioButtonId()) {
            case R.id.radio_start :
                start = poi;
                break;
            case R.id.radio_end :
                end = poi;
                break;
        }
    }

    Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public boolean onMarkerClick(final Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                marker.showInfoWindow();
            }
        }, 2000);
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarker(latLng.latitude, latLng.longitude, "My Marker");
    }

    private void addMarker(POI poi) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(poi.getLatitude(), poi.getLongitude()));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        options.anchor(0.5f, 1);
        options.title(poi.getName());
        options.snippet(poi.getMiddleAddrName() + " " + poi.getLowerAddrName());

        Marker marker = map.addMarker(options);
        markerResolver.put(poi, marker);
        poiResolver.put(marker, poi);
    }
    Marker marker;
    private void addMarker(double lat, double lng, String title) {
        if (marker != null) {
            marker.remove();
            marker = null;
        }
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(lat, lng));
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        options.anchor(0.5f, 1);
        options.title(title);
        options.snippet("snippet - " + title);
        options.draggable(true);

        marker = map.addMarker(options);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }
        mLM.requestSingleUpdate(mProvider, mListener, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }

    private void animateMap(double lat, double lng, final Runnable callback) {
        if (map != null) {
            CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(lat,lng));
            map.animateCamera(update, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    callback.run();
                }

                @Override
                public void onCancel() {

                }
            });
        }
    }
    private void moveMap(double lat, double lng) {
        if (map != null) {
            LatLng latLng = new LatLng(lat, lng);
            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng)
                    .bearing(30)
                    .tilt(45)
                    .zoom(17)
                    .build();
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 17);
//            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);

            map.moveCamera(update);
//        map.animateCamera(update);
        }
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            moveMap(location.getLatitude(), location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onCameraMove() {
        CameraPosition position = map.getCameraPosition();
        LatLng target = position.target;
        Projection projection = map.getProjection();
        VisibleRegion region = projection.getVisibleRegion();

    }

}
