package chen.gong.topcharts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"onCreate:starting Asynctask");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml");
        //Pass to doInBackground() and downloadXML()
        Log.d(TAG,"onCreate:done");//NOTE print before downloadData above finished
    }
    private class DownloadData extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadData";
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG,"onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground:starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null){
                Log.e(TAG,"doInBackground: Error downloading");
            }
            return rssFeed;//Return to onPostExecute
        }
        private String downloadXML(String urlPath){
            StringBuilder xmlResult = new StringBuilder();
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();//Where missing-Internet-Permission error really occurs
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The repsonse code was " + response);
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                //BufferReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                int charsRead;
                char [] inputBuffer = new char [500];
                while (true){
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0 ) {//If end of input stream reached, charsRead = -1
                        break;
                    }
                    if (charsRead > 0 ) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0 , charsRead));
                    }
                }
                reader.close();//Close both InputStreamReader and InputStream
                return xmlResult.toString();
            } catch(MalformedURLException e){
                Log.e(TAG,"downloadXML: Invalid URL " + e.getMessage());
            } catch(IOException e){
                Log.e(TAG, "downloadURL: IO Exception reading data: " + e.getMessage());
            } catch(SecurityException e){
                Log.e(TAG,"downloadXML: Security Exception. Needs permission?" + e.getMessage());
                e.printStackTrace();
            }
            return null;//If null return, prompt error.
        }
    }
}
