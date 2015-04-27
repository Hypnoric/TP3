package hypnoric.tp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    public static SharedPreferences prefs;
    boolean firstTime;
    static final int PREF_FINISHED = 1;
    final static private String APP_KEY = "xzo6z6zc0s3tf3p";
    final static private String APP_SECRET = "4y4dtrm7zzlo049";
    final static private String AUTH_TOKEN = "GJI0AgLtbpAAAAAAAAAABo7e4UYwBTu71C1ZAA4yKGrz2YGPVlqtM6SST2NukECB";
    public static DropboxAPI<AndroidAuthSession> mDBApi;
    public static ArrayList<Preferences> usersInGroup;
    //public static String file;
    //public static Preferences user;
    public static DropboxAPI.Entry dirent;
    //public static DropboxAPI.DropboxFileInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getPreferences(Context.MODE_PRIVATE);

        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, AUTH_TOKEN);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        //users = new ArrayList<Preferences>();
        final String androidId = Settings.Secure.getString(
                this.getContentResolver(), Settings.Secure.ANDROID_ID);
        usersInGroup = getUsersSameGroup(androidId);
        firstTime = prefs.getBoolean("firstTime", true);
        nextStep();
    }

    static public ArrayList<Preferences> getUsersSameGroup(String androidId)
    {
        final ArrayList<Preferences> users = new ArrayList<Preferences>();
        androidId = androidId + ".xml";
        dirent = null;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MainActivity.dirent = mDBApi.metadata("/tp3/", 1000, null, true, null);
                } catch (DropboxException e) {
                    e.printStackTrace();
                }
            }});
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(dirent == null)
            return users;

        ArrayList<String> files = new ArrayList<String>();
        for (DropboxAPI.Entry ent: dirent.contents)
        {
            files.add(ent.fileName());// Add it to the list of thumbs we can choose from
        }

        for (int i = 0; i < files.size(); ++i){
            if(!files.get(i).equals(androidId)) {
                final String parameter = files.get(i); // the final is important
                Thread t2 = new Thread(new Runnable() {
                    String p = parameter;

                    public void run() {
                        try {
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                            DropboxAPI.DropboxFileInfo info = mDBApi.getFile("/tp3/" + p, null, outputStream, null);
                            String file = new String(outputStream.toByteArray(),"UTF-8");
                            //System.out.println("Metadata: " + file);
                            //Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);

                            Preferences user;
                            Serializer serializer = new Persister();
                            user = serializer.read(Preferences.class, file);

                            String groupe = MainActivity.prefs.getString("groupe", "");

                            if (user.GetGroupe().equals(groupe)) {
                                users.add(user);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                t2.start();
                try {
                    t2.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return  users;
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
