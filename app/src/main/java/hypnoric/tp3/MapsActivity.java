package hypnoric.tp3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
    private String adresseDepart;
    private String adresseArrivee;
    private PointDeMarquage depart;
    private PointDeMarquage arrivee;
    private PointDeMarquage dernierPointDeMarquage;
    private boolean destinationReached = false;
    private Marker arriveeMarker = null;
    //private ArrayList<PointDeMarquage> points = new ArrayList<PointDeMarquage>();
    private HashMap<Marker, PointDeMarquage> pointMarkerMap = new HashMap<Marker, PointDeMarquage>();
    private String provider;
    private boolean trackingEnabled = false;
    private boolean locationEnabled = false;
    private float zoomFactor = 0;
    private int locatingFrequency = 0;
    private Intent batteryStatus;
    private WifiManager wifi;
    private boolean playbackMode = false;
    private int numeroTrajet;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        connectionResult = connectionResult;
    }

    public class PointDeMarquage {
        public int index;
        public double latitude;
        public double longitude;
        public double altitude;
        public boolean depart = false;
        public boolean arrivee = false;
        public double distanceRelative = 0;
        public double distanceTotale = 0;
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
            adresseDepart = extras.getString("depart");
            adresseArrivee = extras.getString("arrivee");
            zoomFactor = extras.getFloat("zoomFactor");
            locatingFrequency = extras.getInt("locatingFrequency");
            playbackMode = extras.getBoolean("playbackMode");
            numeroTrajet = extras.getInt("numeroTrajet");
        }

        /*depart = getLatLongFromAddress(adresseDepart);
        depart.depart = true;
        arrivee = getLatLongFromAddress(adresseArrivee);
        arrivee.arrivee = true;
        arrivee.latitude = 45.6060755;
        arrivee.longitude = -73.530734;*/

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

    private List<PointDeMarquage> getHistory(){
        List<PointDeMarquage> ptsList = new ArrayList<PointDeMarquage>();
        String selectQuery = "SELECT  * FROM " + "Trajet" + Integer.toString(numeroTrajet) ;
        /*Cursor cursor = MainActivity.trajets.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
           // cursor.moveToNext();

            PointDeMarquage pt1 = new PointDeMarquage();
            pt1.index = Integer.parseInt(cursor.getString(2));
            pt1.latitude = Double.parseDouble(cursor.getString(3));
            pt1.longitude = Double.parseDouble(cursor.getString(4));
            pt1.altitude = Double.parseDouble(cursor.getString(5));
            if(Integer.parseInt(cursor.getString(6)) == 0)
                pt1.depart = false;
            else
                pt1.depart = true;
            if(Integer.parseInt(cursor.getString(7)) == 0)
                pt1.arrivee = false;
            else
                pt1.arrivee = true;
            pt1.distanceRelative = Double.parseDouble(cursor.getString(8));
            pt1.distanceTotale = Double.parseDouble(cursor.getString(9));
            pt1.batterieLevel = Integer.parseInt(cursor.getString(10));
            pt1.direction = cursor.getString(11);
            pt1.ssid = cursor.getString(12);
            pt1.bssid = cursor.getString(13);
            pt1.signal = Integer.parseInt(cursor.getString(14));
            // Adding contact to list
            ptsList.add(pt1);
            cursor.moveToNext();

            PointDeMarquage pt2 = new PointDeMarquage();
            pt2.latitude = Double.parseDouble(cursor.getString(2));
            pt2.longitude = Double.parseDouble(cursor.getString(3));
            pt2.altitude = Double.parseDouble(cursor.getString(4));
            if(Integer.parseInt(cursor.getString(5)) == 0)
                pt2.depart = false;
            else
                pt2.depart = true;
            if(Integer.parseInt(cursor.getString(6)) == 0)
                pt2.arrivee = false;
            else
                pt2.arrivee = true;
            pt2.distanceRelative = Double.parseDouble(cursor.getString(7));
            pt2.distanceTotale = Double.parseDouble(cursor.getString(8));
            pt2.batterieLevel = Integer.parseInt(cursor.getString(9));
            pt2.direction = cursor.getString(10);
            pt2.ssid = cursor.getString(11);
            pt2.bssid = cursor.getString(12);
            pt2.signal = Integer.parseInt(cursor.getString(13));
            // Adding contact to list
            ptsList.add(pt2);
            cursor.moveToNext();*/
/*
            do {
                PointDeMarquage pt = new PointDeMarquage();
                pt.index = Integer.parseInt(cursor.getString(2));
                pt.latitude = Double.parseDouble(cursor.getString(3));
                pt.longitude = Double.parseDouble(cursor.getString(4));
                pt.altitude = Double.parseDouble(cursor.getString(5));
                if(Integer.parseInt(cursor.getString(6)) == 0)
                    pt.depart = false;
                else
                    pt.depart = true;
                if(Integer.parseInt(cursor.getString(7)) == 0)
                    pt.arrivee = false;
                else
                    pt.arrivee = true;
                pt.distanceRelative = Double.parseDouble(cursor.getString(8));
                pt.distanceTotale = Double.parseDouble(cursor.getString(9));
                pt.batterieLevel = Integer.parseInt(cursor.getString(10));
                pt.direction = cursor.getString(11);
                pt.ssid = cursor.getString(12);
                pt.bssid = cursor.getString(13);
                pt.signal = Integer.parseInt(cursor.getString(14));
                // Adding contact to list
                ptsList.add(pt);
            } while (cursor.moveToNext());
        }*/

        // return contact list
        return ptsList;
    }

    public class PointDeMarquageIndexComparator implements Comparator<PointDeMarquage> {
        @Override
        public int compare(PointDeMarquage point1, PointDeMarquage point2) {
            return ((Integer)point1.index).compareTo(((Integer) point2.index));
        }
    }

    private void RetrievePositionInformation() {
        PointDeMarquage point = new PointDeMarquage();
        //Location location = locationManager.getLastKnownLocation(provider);
        Location location = mLastKnownLocation;
        if (location != null) {
            point.latitude = location.getLatitude();
            point.longitude = location.getLongitude();
            point.altitude = location.getAltitude();
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(point.latitude, point.longitude))
                            //.title(markerInfo)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            //points.add(point);
            point.distanceRelative = SphericalUtil.computeDistanceBetween(new LatLng(point.latitude, point.longitude), new LatLng(dernierPointDeMarquage.latitude, dernierPointDeMarquage.longitude));
            point.distanceTotale = SphericalUtil.computeDistanceBetween(new LatLng(point.latitude, point.longitude), new LatLng(depart.latitude, depart.longitude));

            double distanceToDestination = SphericalUtil.computeDistanceBetween(new LatLng(point.latitude, point.longitude), new LatLng(arrivee.latitude, arrivee.longitude));
            if(distanceToDestination <= 10) {
                //Less than 10 meters from destination ... considered reached
                destinationReached = true;
                Toast.makeText(getApplicationContext(), "You have reached your destination", Toast.LENGTH_LONG).show();
                stopLocationUpdates();
            }

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

            point.index = dernierPointDeMarquage.index + 1;

            pointMarkerMap.put(marker, point);

            dernierPointDeMarquage = point;

            if(destinationReached){
                if (arrivee != null) {
                    arrivee.index = pointMarkerMap.size() + 1;
                    pointMarkerMap.put(arriveeMarker, arrivee);
                    dernierPointDeMarquage = arrivee;
                }
                List list = sortHashMapByIndex(pointMarkerMap);
                //MainActivity.addToDataBase(adresseDepart, adresseArrivee, (PointDeMarquage[])list.toArray(new PointDeMarquage[list.size()]));
            }

            wifi.startScan();
            List<ScanResult> results = wifi.getScanResults();
            if(results.size() > 0)
            {
                Toast.makeText(getApplicationContext(), "Point d'acces wifi detecte", Toast.LENGTH_SHORT).show();
                for(int i = 0; i < results.size(); ++i)
                {
                    if(point.signal < results.get(i).level)
                    {
                        point.signal = results.get(i).level;
                        point.ssid = results.get(i).SSID;
                        point.bssid = results.get(i).BSSID;
                    }
                }
            }
        }
    }

    public List<PointDeMarquage> sortHashMapByIndex(HashMap passedMap) {
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues, new PointDeMarquageIndexComparator());

        List<PointDeMarquage> sortedList = new ArrayList();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();

            sortedList.add((PointDeMarquage)val);
            /*Object key = keyIt.next();
            int index1 = ((PointDeMarquage)passedMap.get(key)).index;
            int index2 = ((PointDeMarquage)val).index;

            if (index1 < index2){
                passedMap.remove(key);
                mapKeys.remove(key);
                sortedList.put((Marker)key, (PointDeMarquage)val);
                break;
            }*/

        }
        return sortedList;
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

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        if(!playbackMode){
            mMap.setMyLocationEnabled(true);

            // Get the location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            // Define the criteria how to select the locatioin provider -> use
            // default
            Criteria criteria = new Criteria();
            provider = locationManager.getBestProvider(criteria, false);

            if (depart != null) {
                Marker depMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(depart.latitude, depart.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                pointMarkerMap.put(depMarker, depart);
                dernierPointDeMarquage = depart;
            }
            if (arrivee != null) {
                arriveeMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(arrivee.latitude, arrivee.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                //pointMarkerMap.put(arrMarker, arrivee);
            }
        }
        else{
            List<PointDeMarquage> playbackPoints = getHistory();
            depart = playbackPoints.get(0);
            if (depart != null) {
                Marker depMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(depart.latitude, depart.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                pointMarkerMap.put(depMarker, depart);
                dernierPointDeMarquage = depart;
            }
            for(int i = 1; i < playbackPoints.size()-1; i++){
                PointDeMarquage point = playbackPoints.get(i);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(point.latitude, point.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                pointMarkerMap.put(marker, point);
            }
            arrivee = playbackPoints.get(playbackPoints.size()-1);
            if (arrivee != null) {
                Marker arrMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(arrivee.latitude, arrivee.longitude))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                pointMarkerMap.put(arrMarker, arrivee);
            }
        }

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(depart.latitude, depart.longitude), zoomFactor));

        /*mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                PointDeMarquage point = pointMarkerMap.get(marker);
                if(point == null)
                    return null;
                DecimalFormat df = new DecimalFormat("#.##");

                // Getting view from the layout file
                View v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                TextView title = (TextView) v.findViewById(R.id.infowindow_title);
                if(point.depart)
                    title.setText("Depart");
                else if(point.arrivee)
                    title.setText("Arrivee");
                else
                    title.setText("Point " + point.index);


                TextView lat = (TextView) v.findViewById(R.id.infowindow_latitude);
                String formattedLat = df.format(point.latitude);
                lat.setText("Latitude : "+formattedLat);

                TextView lng = (TextView) v.findViewById(R.id.infowindow_longitude);
                String formattedLng = df.format(point.longitude);
                lng.setText("Longitude : "+formattedLng);

                TextView alt = (TextView) v.findViewById(R.id.infowindow_altitude);
                String formattedAlt = df.format(point.altitude);
                alt.setText("Altitude : "+formattedAlt);

                TextView dir = (TextView) v.findViewById(R.id.infowindow_direction);
                dir.setText("Direction : " + point.direction);

                TextView distRel = (TextView) v.findViewById(R.id.infowindow_distanceRelative);
                String formattedDistRel = df.format(point.distanceRelative);
                distRel.setText("Distance Relative : "+formattedDistRel + "m");

                TextView distTot = (TextView) v.findViewById(R.id.infowindow_distanceTotale);
                String formattedDistTot = df.format(point.distanceTotale);
                distTot.setText("Distance Totale : "+formattedDistTot + "m");

                TextView mod = (TextView) v.findViewById(R.id.infowindow_modeLocalistion);
                mod.setText("GPS");

                TextView bat = (TextView) v.findViewById(R.id.infowindow_niveauBatterie);
                bat.setText("Batterie : " + point.batterieLevel + "%");

                TextView sig = (TextView) v.findViewById(R.id.infowindow_signal);
                sig.setText("Signal : " + point.signal + " dBm");

                TextView ssid = (TextView) v.findViewById(R.id.infowindow_ssid);
                ssid.setText("SSID : " + point.ssid);

                TextView bssid = (TextView) v.findViewById(R.id.infowindow_bssid);
                bssid.setText("BSSID : " + point.bssid);

                return v;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                // TODO Auto-generated method stub
                return null;
            }
        });*/

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

    /*@Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/

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
                        PointDeMarquage points[] = new PointDeMarquage[pointMarkerMap.size()];
                        points = pointMarkerMap.values().toArray(points);
                        //MainActivity.addToDataBase(adresseDepart, adresseArrivee, points);
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


    /*public PointDeMarquage getLatLongFromAddress(String youraddress) {
        youraddress.replace(" ", "%20");
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                youraddress + "&sensor=false";
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());

            double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            PointDeMarquage point = new PointDeMarquage();
            point.latitude = lat;
            point.longitude = lng;
            return point;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}