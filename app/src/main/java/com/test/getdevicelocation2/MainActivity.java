package com.test.getdevicelocation2;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.*;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient client;
    String contentText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(this);

        Button button = findViewById(R.id.getLocation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

                    return;
                }
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                     if(location !=null) {
                         //TextView textView = findViewById(R.id.location);
                         //textView.setText(location.toString());

                         double elevation;
                         double Lat=location.getLatitude();
                         double Lon=location.getLongitude();
                          String ApiKey="";
                         String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + Lat + "," + Lon + "&key="+ApiKey;
                         //String url="https://developer.android.com/index.html";
                         if(contentText==null){
                             // new ProgressTask().execute(url);
                             try {
                           contentText = new ProgressTask().execute(url).get();
                                }
                     catch (java.util.concurrent.ExecutionException | InterruptedException ei) {
                      ei.printStackTrace();
                         }
                            TextView textView = findViewById(R.id.location);
                             //textView.setText(contentText); //VIEW ALL REQUEST
                             try {
                    JSONObject jsonObj = new JSONObject(contentText);
                    JSONArray resultEl = jsonObj.getJSONArray("results");
                    JSONObject current = resultEl.getJSONObject(0);
                    elevation = Double.parseDouble(current.getString("elevation"));
                    textView.setText(String.valueOf(elevation));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                         }
                     }
                    }
                });
            }
        });
    }
    private void requestPermission(){
       ActivityCompat.requestPermissions(this,new String[] {ACCESS_FINE_LOCATION},1);
    }



    private class ProgressTask extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... path) {
            String content;
            try{
                content = getContent(path[0]);
            }
            catch (IOException ex){
                content = ex.getMessage();
            }

            return content;
        }

        @Override
        protected void onPostExecute(String content) {
          super.onPostExecute(contentText);
            //contentText=content;
            /*contentView.setText(content);
            webView.loadData(content, "text/html; charset=utf-8", "utf-8");
            Toast.makeText(getActivity(), "Данные загружены", Toast.LENGTH_SHORT)
                    .show();*/
        }


        private String getContent(String path) throws IOException {
            BufferedReader reader = null;
            try {
                URL url = new URL(path);
                HttpsURLConnection c = (HttpsURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setReadTimeout(10000);
                c.connect();
                reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder buf = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    buf.append(line + "\n");
                }
                return (buf.toString());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

    }
}
