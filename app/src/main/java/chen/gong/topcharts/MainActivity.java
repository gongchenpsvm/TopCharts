package chen.gong.topcharts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCachedUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = (ListView)findViewById(R.id.xmlListView);
//        Log.d(TAG,"onCreate:starting Asynctask");
//        DownloadData downloadData = new DownloadData();
//        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml");
//        //Pass to doInBackground() and downloadXML()
//        Log.d(TAG,"onCreate:done");//NOTE print before downloadData above finished
        if (savedInstanceState != null){//If opened before, retrieve
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);
        }
        downloadUrl(String.format(feedUrl,feedLimit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (feedLimit == 10){
            menu.findItem(R.id.menu10).setChecked(true);
        } else{
            menu.findItem(R.id.menu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        String feedUrl;
        switch (id){
            case R.id.menuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;
            case R.id.menuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.menuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.menu10:
            case R.id.menu25:
                if (!item.isChecked()){
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting feedLimit to " + feedLimit);
                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged ");
                }
                break;
            case R.id.menuRefresh:
                feedCachedUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedUrl,feedLimit));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String feedUrl){
        if (!feedUrl.equalsIgnoreCase(feedCachedUrl)) {//If different, download
            Log.d(TAG, "downloadUrl:starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            //Pass to doInBackground() and downloadXML()
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl:done");//NOTE print before downloadData above finished
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String>{
        private static final String TAG = "DownloadData";
        @Override
        protected void onPostExecute(String s) {//s here is rssFeed
            super.onPostExecute(s);
//            Log.d(TAG,"onPostExecute: parameter is " + s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);
//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>//adapter puts data into list_item
//                    (MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);
            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record,
                    parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
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
