package hypnoric.tp3;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.text.format.DateUtils;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by ysevigny on 4/23/2015.
 */
public class AsyncEventRetriever extends AsyncQueryHandler {

    // Projection array. Creating indices for this array instead of doing
// dynamic lookups improves performance.
    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // Indices for async queries
    public static final int CALENDAR_QUERY = 0;
    public static final int EVENT_QUERY = 1;

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    public MeetingActivity Meeting;

    public AsyncEventRetriever(ContentResolver cr) {
        super(cr);
    }

    @Override
    protected Handler createHandler(Looper looper) {
        return super.createHandler(looper);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);

        if(token == CALENDAR_QUERY){
            if(cursor.getCount()>0) {
                if (cursor.moveToFirst()) {
                    do {
                        long calID = 0;

                        // Get the field values
                        calID = cursor.getLong(PROJECTION_ID_INDEX);

                        Uri.Builder builder = Uri.parse("content://com.android.calendar/instances/when").buildUpon();
                        //Uri.Builder builder = Uri.parse("content://com.android.calendar/calendars").buildUpon();
                        long now = new Date().getTime();

                        ContentUris.appendId(builder, now - DateUtils.DAY_IN_MILLIS * 10000);
                        ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS * 10000);

                        this.startQuery(EVENT_QUERY,null, CalendarContract.Events.CONTENT_URI,new String[]  {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY}, CalendarContract.Events.CALENDAR_ID + "=" + calID,null,null);

                    } while (cursor.moveToNext());
                }
            }
        }
        else if(token == EVENT_QUERY){
            if(cursor != null && cursor.getCount()>0)
            {
                if(cursor.moveToFirst())
                {
                    do
                    {
                        Object mbeg_date,beg_date,beg_time,end_date,end_time;

                        final String title = cursor.getString(0);
                        final Date begin = new Date(cursor.getLong(1));
                        final Date end = new Date(cursor.getLong(2));
                        final Boolean allDay = !cursor.getString(3).equals("0");

                        CalendarEvent event = new CalendarEvent();
                        event.Title = cursor.getString(0);
                        event.StartTime = new Date(cursor.getLong(1));
                        event.EndTime = new Date(cursor.getLong(2));

                        Meeting.AddEvent(event);
                    }
                    while(cursor.moveToNext());
                }
            }
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        super.onInsertComplete(token, cookie, uri);
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        super.onUpdateComplete(token, cookie, result);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);
    }
}
