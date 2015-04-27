package hypnoric.tp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    public static SharedPreferences prefs;
    boolean firstTime;
    static final int PREF_FINISHED = 1;
    final static private String APP_KEY = "xzo6z6zc0s3tf3p";
    final static private String APP_SECRET = "4y4dtrm7zzlo049";
    final static private String AUTH_TOKEN = "GJI0AgLtbpAAAAAAAAAABo7e4UYwBTu71C1ZAA4yKGrz2YGPVlqtM6SST2NukECB";
    public static DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getPreferences(Context.MODE_PRIVATE);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, AUTH_TOKEN);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        //UploadFileToDropbox upload = new UploadFileToDropbox(this, mDBApi, "/tp3/");
        //upload.execute();
        //mDBApi.getSession().startOAuth2Authentication(this);

        firstTime = prefs.getBoolean("firstTime", true);
        nextStep();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    public void nextStep(){
        if(firstTime) {
            Intent intent = new Intent(this, PreferenceActivity.class);
            startActivity(intent);
        }
    }

    public void buttonQuitterOnClick(View v){
        finish();
        System.exit(0);
    }

    public void openMap(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void createMeeting(View v) {
        Intent intent = new Intent(this, MeetingActivity.class);
        startActivity(intent);
    }

    public void preferenceBtnOnClick(View v){
        Intent intent = new Intent(this, PreferenceActivity.class);
        String photoPath = prefs.getString("photoPath", "");
        String courriel = prefs.getString("courriel", "");
        String groupe = prefs.getString("groupe", "");
        boolean restaurant = prefs.getBoolean("restaurant", false);
        boolean parc = prefs.getBoolean("parc", false);
        boolean cinema = prefs.getBoolean("cinema", false);
        intent.putExtra("photoPath", photoPath);
        intent.putExtra("courriel", courriel);
        intent.putExtra("groupe", groupe);
        intent.putExtra("restaurant", restaurant);
        intent.putExtra("parc", parc);
        intent.putExtra("cinema", cinema);
        startActivity(intent);
    }

    public static void updatePosition(double latitude, double longitude)
    {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("latitude", (float)latitude);
        editor.putFloat("longitude", (float)longitude);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
