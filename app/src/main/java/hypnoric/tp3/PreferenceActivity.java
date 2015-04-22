package hypnoric.tp3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class PreferenceActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String photoPath = extras.getString("photoPath");
            String courriel = extras.getString("courriel");
            String groupe = extras.getString("groupe");
            boolean restaurant = extras.getBoolean("restaurant");
            boolean parc = extras.getBoolean("parc");
            boolean cinema = extras.getBoolean("cinema");
        }

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
        MainActivity.setFirstTimeFalse();
        BackToMenu(MainActivity.PREF_FINISHED);
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
