package hypnoric.tp3;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class PreferenceActivity extends ActionBarActivity {

    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        String courriel = "";
        String groupe = "";
        boolean restaurant = false;
        boolean parc = false;
        boolean cinema = false;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            selectedImagePath = extras.getString("photoPath");
            courriel = extras.getString("courriel");
            groupe = extras.getString("groupe");
            restaurant = extras.getBoolean("restaurant");
            parc = extras.getBoolean("parc");
            cinema = extras.getBoolean("cinema");
        }
        //TextView textPhoto = (TextView)findViewById(R.id.photoText);
        //textPhoto.setText(photoPath);
        if(!selectedImagePath.equals("")){
            Bitmap selectedImage = BitmapFactory.decodeFile(selectedImagePath);
            ImageView photo = (ImageView) findViewById(R.id.photoImg);
            photo.setImageBitmap(selectedImage);
        }
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

        TextView textCourriel = (TextView)findViewById(R.id.courrierText);
        TextView textGroupe = (TextView)findViewById(R.id.groupNameText);
        CheckBox restaurantChk = (CheckBox)findViewById(R.id.checkBoxRestaurant);
        CheckBox parcChk = (CheckBox)findViewById(R.id.checkBoxParc);
        CheckBox cinemaChk = (CheckBox)findViewById(R.id.checkBoxCinema);

        String courriel = textCourriel.getText().toString();
        String groupe = textGroupe.getText().toString();
        boolean restaurant = restaurantChk.isChecked();
        boolean parc = parcChk.isChecked();
        boolean cinema = cinemaChk.isChecked();

        savePreferences(selectedImagePath, courriel, groupe, restaurant, parc, cinema);

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
        Preferences preferencesUser;
        if (selectedImagePath.equals(""))
            preferencesUser = new Preferences("null", courriel, groupe, restaurant, parc, cinema, latitude, longitude);
        else
            preferencesUser = new Preferences(selectedImagePath, courriel, groupe, restaurant, parc, cinema, latitude, longitude);
        File xmlFile = new File(getFilesDir().getPath() + "/" + MainActivity.androidId + ".xml");

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
        File photo = new File(selectedImagePath);
        UploadFileToDropbox uploadPhoto = new UploadFileToDropbox(this, MainActivity.mDBApi, "/tp3/", photo);
        uploadPhoto.execute();
        MainActivity.updateUser();
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

    public void photoBtnOnClick(View v) {
        // in onCreate or any event where your want the user to
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                InputStream is = null;
                try {
                    is = getContentResolver().openInputStream(selectedImageUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                selectedImagePath = getImagePath(selectedImageUri);

                ImageView photo = (ImageView) findViewById(R.id.photoImg);
                photo.setImageBitmap(bitmap);
            }
        }
    }

    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
}
