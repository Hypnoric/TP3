package hypnoric.tp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    private static SharedPreferences prefs;
    boolean firstTime;
    static final int PREF_FINISHED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getPreferences(Context.MODE_PRIVATE);

        firstTime = prefs.getBoolean("firstTime", true);
        nextStep();
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
        /*intent.putExtra("depart", depart);
        intent.putExtra("arrivee", arrivee);
        intent.putExtra("zoomFactor", zoomFactor);
        intent.putExtra("locatingFrequency", locatingFrequency);
        intent.putExtra("replay", false);
        intent.putExtra("numeroTrajet", 0);*/
        startActivity(intent);
    }

    public void preferenceBtnOnClick(View v){
        Intent intent = new Intent(this, PreferenceActivity.class);
        startActivity(intent);
    }

    public static void setFirstTimeFalse(){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstTime", false);
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
