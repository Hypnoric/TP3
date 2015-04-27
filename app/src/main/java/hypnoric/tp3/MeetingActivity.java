package hypnoric.tp3;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;


public class MeetingActivity extends ActionBarActivity {

    private AsyncEventRetriever eventRetriever;
    private HashSet<String> calendarIDs = new HashSet<String>();
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<CalendarEvent> events=new ArrayList<CalendarEvent>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);

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

        eventRetriever = new AsyncEventRetriever(getContentResolver());
        eventRetriever.Meeting = this;

        // Run query
        Uri uri = Calendars.CONTENT_URI;
        String selection = "((" + Calendars.ACCOUNT_NAME + " = ?) AND ("
                + Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {"sevigny.yanick@gmail.com", "com.google",
                "sevigny.yanick@gmail.com"};
        // Submit the query and get a Cursor object back.
        eventRetriever.startQuery(AsyncEventRetriever.CALENDAR_QUERY, null, uri, AsyncEventRetriever.EVENT_PROJECTION, selection, selectionArgs, null);
    }

    public void AddEvent(CalendarEvent event){
        listItems.add(event.Title);
        adapter.notifyDataSetChanged();
        events.add(event);
    }

    public void CalendarQueryCompleted(){
        // We now have the events from current user, now we need to get every member's location
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
