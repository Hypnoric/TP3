package hypnoric.tp3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

//import android.location.LocationListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSource {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationManager locationManager;
    private OnLocationChangedListener mListener;
    private Location mLastKnownLocation;
    private PointDeMarquage dernierPointDeMarquage;
    private boolean destinationReached = false;
    private HashMap<Marker, Preferences> pointMarkerMap = new HashMap<Marker, Preferences>();
    private boolean trackingEnabled = false;
    private boolean locationEnabled = false;
    private float zoomFactor = 0;
    private int locatingFrequency = 0;
    private Intent batteryStatus;
    private WifiManager wifi;
    private boolean playbackMode = false;
    private int numeroTrajet;
    private Dialog meetingDialog;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionResult = connectionResult;
    }

    public class PointDeMarquage {
        public int index;
        public double latitude;
        public double longitude;
        public double altitude;
        public int batterieLevel = 0;
        public String direction = "";
        public String ssid = "";
        public String bssid = "";
        public int signal = -100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //On doit recevoir les points de depart et d'arrivee en parametre (string)
        //On utilisera geocoder pour obtenir la latitude et logitude de ces points de depart pour creer les points de marquages

        super.onCreate(savedInstanceState);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_SHORT).show();
            wifi.setWifiEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            zoomFactor = extras.getFloat("zoomFactor");
            locatingFrequency = extras.getInt("locatingFrequency");
            playbackMode = extras.getBoolean("playbackMode");
            numeroTrajet = extras.getInt("numeroTrajet");
        }

        setContentView(R.layout.activity_maps);
        buildGoogleApiClient();
        setUpMap();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, ifilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void RetrievePositionInformation() {
        PointDeMarquage point = new PointDeMarquage();
        //Location location = locationManager.getLastKnownLocation(provider);
        Location location = mLastKnownLocation;
        if (location != null) {
            point.latitude = location.getLatitude();
            point.longitude = location.getLongitude();
            point.altitude = location.getAltitude();
            /*Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                            //.title(markerInfo)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));*/


            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float)scale;

            point.batterieLevel = (int)(batteryPct * 100.0f);

            if(location.hasBearing()){
                float bearing = location.getBearing();
                if(bearing < 22.5 || bearing > 337.5)
                    point.direction = "Est";
                else if(bearing >= 22.5 && bearing < 67.5)
                    point.direction = "Nord-Est";
                else if(bearing >= 67.5 && bearing < 112.5 )
                    point.direction = "Nord";
                else if(bearing >= 112.5 && bearing < 157.5)
                    point.direction = "Nord-Ouest";
                else if(bearing >= 157.5 && bearing < 202.5)
                    point.direction = "Ouest";
                else if(bearing >= 202.5 && bearing < 247.5)
                    point.direction = "Sud-Ouest";
                else if(bearing >= 247.5 && bearing < 292.5)
                    point.direction = "Sud";
                else if(bearing >= 292.5 && bearing < 337.5)
                    point.direction = "Sud-Est";
            }
            else{
                point.direction = "Inconnue";
            }

            dernierPointDeMarquage = point;
            MainActivity.updatePosition(point.latitude, point.longitude, getFilesDir().getPath(),this);

            wifi.startScan();
            List<ScanResult> results = wifi.getScanResults();
            if(results.size() > 0)
            {
                /*Toast.makeText(getApplicationContext(), "Point d'acces wifi detecte", Toast.LENGTH_SHORT).show();
                for(int i = 0; i < results.size(); ++i)
                {
                    if(point.signal < results.get(i).level)
                    {
                        point.signal = results.get(i).level;
                        point.ssid = results.get(i).SSID;
                        point.bssid = results.get(i).BSSID;
                    }
                }*/
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();

        if (mGoogleApiClient.isConnected() && !destinationReached) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(locatingFrequency * 1000);
        locationRequest.setFastestInterval(locatingFrequency * 1000);
        locationRequest.setSmallestDisplacement(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    public void refresh(View v){
        MainActivity.usersInGroup = MainActivity.getUsersSameGroup(MainActivity.androidId);
        updateMarkers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() && !destinationReached)
            stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setUpMap() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    private void updateMarkers(){
        // Verify if a new meeting as been posted
        if(MainActivity.isMeetingAccepted().equals("Unknown") || MainActivity.isMeetingAccepted().equals("unknown")){
            if(meetingDialog != null && meetingDialog.isShowing()){
                meetingDialog.hide();
            }
            // Create custom dialog object
            meetingDialog = new Dialog(MapsActivity.this);
            // Include dialog.xml file
            meetingDialog.setContentView(R.layout.dialog_meeting);
            // Set dialog title
            meetingDialog.setTitle("");
            meetingDialog.setCancelable(false);

            Meeting meeting = MainActivity.GetMeetingFromDropbox();
            TextView text = (TextView)meetingDialog.findViewById(R.id.meeting_place);
            text.setText(meeting != null?meeting.GetPlace():"Unknown place");
            Button acceptButton = (Button)meetingDialog.findViewById(R.id.acceptButton);
            acceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.accepterMeeting("True", getFilesDir().getPath(), MapsActivity.this);
                    meetingDialog.hide();
                }
            });
            Button refuseButton = (Button)meetingDialog.findViewById(R.id.refuseButton);
            refuseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.accepterMeeting("False", getFilesDir().getPath(), MapsActivity.this);
                    meetingDialog.hide();
                }
            });
            meetingDialog.show();
        }

        mMap.clear();
        ArrayList<Preferences> users = MainActivity.usersInGroup;
        if(users != null && users.size() > 1){
            for(Preferences user : users){
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(user.GetLatitude(), user.GetLongitude()))
                                //.title(markerInfo)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                pointMarkerMap.put(marker, user);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setMyLocationEnabled(true);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        updateMarkers();

        Preferences user = MainActivity.user;
        if(user != null && user.GetLatitude() != 0 && user.GetLongitude() != 0)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(user.GetLatitude(), user.GetLongitude()), zoomFactor));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Preferences user = pointMarkerMap.get(marker);
                if(user == null)
                    return null;

                // Getting view from the layout file
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                TextView title = (TextView) v.findViewById(R.id.infowindow_title);
                title.setText(user.GetCourriel());


                TextView lat = (TextView) v.findViewById(R.id.infowindow_meetingRequest);
                if(user.GetMeetingAccepte().equals("Unknown"))
                    lat.setText("Meeting not answered");
                else if(user.GetMeetingAccepte().equals("True"))
                    lat.setText("Meeting Accepted");
                else if(user.GetMeetingAccepte().equals("False"))
                    lat.setText("Meeting Refused");

                /*if(!user.GetPhotoPath().equals("")){
                    Bitmap selectedImage = BitmapFactory.decodeFile(user.GetPhotoPath());
                    if(selectedImage != null){
                        ImageView picture = (ImageView) v.findViewById(R.id.profile_pic);
                        picture.setImageBitmap(selectedImage);
                    }
                }*/

                return v;
            }
        });

        trackingEnabled = true;
        final Handler handler = new Handler();
        handler.post(new Runnable() {

            @Override
            public void run() {
                if (trackingEnabled) {
                    //RetrievePositionInformation();
                    handler.postDelayed(this, 5000); // Change this time in function of sampling frequency
                }
            }
        });
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //locationEnabled = true;

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationEnabled = false;
    }

    @Override
    public void onLocationChanged(Location location) {

        /*if( mListener != null ) {
            mListener.onLocationChanged(location);
        }*/

        if (location != null) {
            mLastKnownLocation = location;
            RetrievePositionInformation();
        }
    }

    public void onBackPressed() {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                .setTitle("Menu")
                .setMessage("Voulez vous revenir au menu?")

                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private PointDeMarquage getLatLongFromAddress(String address) {
        double lat = 0.0, lng = 0.0;

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                PointDeMarquage point = new PointDeMarquage();
                point.latitude = addresses.get(0).getLatitude();
                point.longitude = addresses.get(0).getLongitude();
                return point;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}