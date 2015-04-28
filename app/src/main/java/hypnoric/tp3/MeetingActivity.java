package hypnoric.tp3;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.provider.CalendarContract.Calendars;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;


public class MeetingActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private AsyncEventRetriever eventRetriever;
    private HashSet<String> calendarIDs = new HashSet<String>();
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<CalendarEvent> events=new ArrayList<CalendarEvent>();

    private LatLng meetingPosition;
    private ArrayList<GooglePlace> meetingPlaces;
    private int locatingFrequency = 4;
    private Location mLastKnownLocation;
    private int placeSearchAttempt = 1;

    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

        buildGoogleApiClient();

        listView = (ListView) findViewById(R.id.list);

        // Defined Array values to show in ListView

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItems);


        // Assign adapter to ListView
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
                GooglePlace chosenPlace = meetingPlaces.get((int)id);
                requestMeeting(chosenPlace);
            }
        });

        eventRetriever = new AsyncEventRetriever(getContentResolver());
        eventRetriever.Meeting = this;
        Preferences user = MainActivity.user;
        if(user != null && user.GetCourriel() != null){
            // Run query
            Uri uri = Calendars.CONTENT_URI;
            String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                    + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                    + Calendars.OWNER_ACCOUNT + " = ?))";
            String[] selectionArgs = new String[] {user.GetCourriel(), "com.google",
                    user.GetCourriel()};
            // Submit the query and get a Cursor object back.
            eventRetriever.startQuery(AsyncEventRetriever.CALENDAR_QUERY, null, uri, AsyncEventRetriever.EVENT_PROJECTION, selection, selectionArgs, null);
        }
    }

    private void requestMeeting(GooglePlace place){

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
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
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void AddEvent(CalendarEvent event){
        //listItems.add(event.Title);
        //adapter.notifyDataSetChanged();
        events.add(event);
    }

    public void CalendarQueryCompleted(){
        // We now have the events from current user, now we need to get every member's location
        ArrayList<Preferences> friends = MainActivity.getUsersSameGroup("");
        ArrayList<LatLng> locations = new ArrayList<LatLng>();
        for(Preferences pref : friends){
            locations.add(new LatLng(pref.GetLatitude(), pref.GetLongitude()));
        }
        meetingPosition = FindCenterLocation(locations);


    }

    public void PlaceSearchCompleted(ArrayList<GooglePlace> places){

        if(places != null && places.size() > 0){
            for(int i = 0; i < places.size(); i++){
                GooglePlace place = places.get(i);
                listItems.add(place.getName());
            }
            meetingPlaces = places;
            adapter.notifyDataSetChanged();
            placeSearchAttempt = 1;
        }
        else if(placeSearchAttempt < 10){
            AsyncPlaceSearch asyncPlaceSearch = new AsyncPlaceSearch();
            asyncPlaceSearch.latitude = "" + mLastKnownLocation.getLatitude();//meetingPosition.latitude;
            asyncPlaceSearch.longitude = "" + mLastKnownLocation.getLongitude();
            asyncPlaceSearch.Meeting = this;
            asyncPlaceSearch.radius = asyncPlaceSearch.radius + placeSearchAttempt * 20;
            asyncPlaceSearch.execute();
            placeSearchAttempt = placeSearchAttempt + 1;
        }
        else{
            Toast.makeText(this, "Unable to find a meeting place", Toast.LENGTH_SHORT);
        }
    }

    private LatLng FindCenterLocation(ArrayList<LatLng> locations){
        LatLng finalPos;
        double totalLat = 0;
        double totalLng = 0;
        int totalLocations = 0;
        for(LatLng location : locations){
            totalLat += location.latitude;
            totalLng += location.longitude;
            totalLocations += 1;
        }
        totalLat = totalLat/totalLocations;
        totalLng = totalLng/totalLocations;
        finalPos = new LatLng(totalLat, totalLng);
        return finalPos;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //locationEnabled = true;

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {}


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if(mLastKnownLocation == null){
                mLastKnownLocation = location;
                AsyncPlaceSearch asyncPlaceSearch = new AsyncPlaceSearch();
                asyncPlaceSearch.latitude = "" + mLastKnownLocation.getLatitude();//meetingPosition.latitude;
                asyncPlaceSearch.longitude = "" + mLastKnownLocation.getLongitude();
                asyncPlaceSearch.Meeting = this;
                asyncPlaceSearch.execute();
            }
            mLastKnownLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_meeting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
