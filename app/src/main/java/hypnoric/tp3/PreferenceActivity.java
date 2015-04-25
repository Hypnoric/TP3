package hypnoric.tp3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;


public class PreferenceActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        String photoPath = "";
        String courriel = "";
        String groupe = "";
        boolean restaurant = false;
        boolean parc = false;
        boolean cinema = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            photoPath = extras.getString("photoPath");
            courriel = extras.getString("courriel");
            groupe = extras.getString("groupe");
            restaurant = extras.getBoolean("restaurant");
            parc = extras.getBoolean("parc");
            cinema = extras.getBoolean("cinema");
        }
        TextView textPhoto = (TextView)findViewById(R.id.photoText);
        textPhoto.setText(photoPath);
        TextView textCourriel = (TextView)findViewById(R.id.courrierText);
        textCourriel.setText(courriel);
        TextView textGroupe = (TextView)findViewById(R.id.groupNameText);
        textGroupe.setText(groupe);
        CheckBox restaurantChk = (CheckBox)findViewById(R.id.checkBoxRestaurant);
        restaurantChk.setChecked(restaurant);
        CheckBox parcChk = (CheckBox)findViewById(R.id.checkBoxParc);
        parcChk.setChecked(parc);
        CheckBox cinemaChk = (CheckBox)findViewById(R.id.checkBoxCinema);
        cinemaChk.setChecked(cinema);
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

    public void onBackPressed() {
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                .setTitle("Quitter")
                .setMessage("Voulez vous quitter sans sauvegarder les changements ?")

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

    public void saveBtnOnClick(View v){

        TextView textPhoto = (TextView)findViewById(R.id.photoText);
        TextView textCourriel = (TextView)findViewById(R.id.courrierText);
        TextView textGroupe = (TextView)findViewById(R.id.groupNameText);
        CheckBox restaurantChk = (CheckBox)findViewById(R.id.checkBoxRestaurant);
        CheckBox parcChk = (CheckBox)findViewById(R.id.checkBoxParc);
        CheckBox cinemaChk = (CheckBox)findViewById(R.id.checkBoxCinema);

        String photoPath = textPhoto.getText().toString();
        String courriel = textCourriel.getText().toString();
        String groupe = textGroupe.getText().toString();
        boolean restaurant = restaurantChk.isChecked();
        boolean parc = parcChk.isChecked();
        boolean cinema = cinemaChk.isChecked();

        savePreferences(photoPath, courriel, groupe, restaurant, parc, cinema);

        BackToMenu(MainActivity.PREF_FINISHED);
    }

    public void savePreferences(String photoPath, String courriel, String groupe, boolean restaurant, boolean parc, boolean cinema){
        SharedPreferences.Editor editor = MainActivity.prefs.edit();
        editor.putBoolean("firstTime", false);
        editor.putString("photoPath", photoPath);
        editor.putString("courriel", courriel);
        editor.putString("groupe", groupe);
        editor.putBoolean("restaurant", restaurant);
        editor.putBoolean("parc", parc);
        editor.putBoolean("cinema", cinema);
        editor.commit();

        double latitude = MainActivity.prefs.getFloat("latitude", 0);
        double longitude = MainActivity.prefs.getFloat("longitude", 0);

        Preferences preferencesUser = new Preferences(photoPath, courriel, groupe, restaurant, parc, cinema, latitude, longitude);
        final String androidId = Settings.Secure.getString(
                this.getContentResolver(), Settings.Secure.ANDROID_ID);
        File xmlFile = new File(getFilesDir().getPath() + "/" + androidId + ".xml");

        try
        {
            Serializer serializer = new Persister();
            serializer.write(preferencesUser, xmlFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        UploadFileToDropbox upload = new UploadFileToDropbox(this, MainActivity.mDBApi, "/tp3/", xmlFile);
        upload.execute();
    }

    private void BackToMenu(int activityResult) {
        Intent returnIntent = new Intent();

        // SAVE INFO AND SHIT

        /* returnIntent.putExtra("nomJoueur1",player1Name);
         returnIntent.putExtra("scoreJoueur1",ptsP1);
         if(!isOnePlayer){
             returnIntent.putExtra("nomJoueur2",player2Name);
             returnIntent.putExtra("scoreJoueur2",ptsP2);
         }*/
        setResult(activityResult, returnIntent);

        finish();
    }
}
