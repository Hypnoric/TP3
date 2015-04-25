package hypnoric.tp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

public class UploadFileToDropbox extends AsyncTask<Void, Void, Boolean> {

    private DropboxAPI<?> dropbox;
    private String path;
    private Context context;
    private File fileToUpload;

    public UploadFileToDropbox(Context context, DropboxAPI<?> dropbox,
                               String path, File uploadFile) {
        this.context = context.getApplicationContext();
        this.dropbox = dropbox;
        this.path = path;
        this.fileToUpload = uploadFile;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        final File tempDir = context.getCacheDir();
        //File tempFile;
        FileWriter fr;
        try {
            //tempFile = File.createTempFile("file", ".txt", tempDir);
            //fr = new FileWriter(tempFile);
            //fr.write("Sample text file created for demo purpose. You may use some other file format for your app ");
            //fr.close();

            FileInputStream fileInputStream = new FileInputStream(fileToUpload);
            dropbox.putFileOverwrite(path + fileToUpload.getName(), fileInputStream,
                    fileToUpload.length(), null);
            fileToUpload.delete();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DropboxException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "File Uploaded Sucesfully!",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to upload file", Toast.LENGTH_LONG)
                    .show();
        }
    }
}