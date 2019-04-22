package com.test.getdevicelocation2;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.google.ads.mediation.MediationServerParameters;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.*;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient locationClient;
    String contentText = null;

    private PendingIntent mPendingIntent;
    private myTransitionReceiver mTransitionsReceiver;

    private final String TRANSITION_ACTION_RECEIVER =
            BuildConfig.APPLICATION_ID + "TRANSITION_ACTION_RECEIVER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        Intent intent = new Intent(TRANSITION_ACTION_RECEIVER);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        mTransitionsReceiver = new myTransitionReceiver();
        registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITION_ACTION_RECEIVER));


        //mTransitionsReceiver.onReceive(MainActivity.this,intent);

        Button button = findViewById(R.id.getLocation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //gimTransitionsReceiver.onReceive(MainActivity.this,new Intent(TRANSITION_ACTION_RECEIVER));

                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

                    return;
                }
                locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                     if(location !=null) {
                         TextView textView1 = findViewById(R.id.location);
                         TextView textView4 = findViewById(R.id.RMR);
                         //textView1.setText("Location: "+location.toString());

                         try {
                            textView4.setText("RMR is: "+ String.valueOf(
                                    countRMRUsingMifflinJeorEquation(1,60,165,21)));
                         } catch (InvalidParameterException e){
                             e.printStackTrace();
                         }

                         double elevation;
                         double Lat=location.getLatitude();
                         double Lon=location.getLongitude();

                         Date date = new Date(location.getTime());
                         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                         String time = dateFormat.format(date);

                         textView1.setText("Latitude: "+String.valueOf(Lat)+"   Longitude: "+String.valueOf(Lon)+ "   Time:"+time);
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
                            TextView textView2 = findViewById(R.id.elevation);
                             //textView2.setText(contentText); //VIEW ALL REQUEST
                             try {
                    JSONObject jsonObj = new JSONObject(contentText);
                    JSONArray resultEl = jsonObj.getJSONArray("results");
                    JSONObject current = resultEl.getJSONObject(0);
                    elevation = Double.parseDouble(current.getString("elevation"));
                    textView2.setText("Elevation: "+String.valueOf(elevation));
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

    /*sex is int: -1 not given,0 male,1 female
    weight in kg
    heilght in cm*/
    public int countRMRUsingMifflinJeorEquation(int sex,double weight,double height,int age){
        int RMR=0;
     if(sex!=-1 && weight>0 && height>0&& age>0 ) {
         if (sex == 0) {
             RMR = (int) (9.99 * weight + 6.25 * height - 4.92 * age + 5); //male
         } else if (sex == 1) {
             RMR = (int) (9.99 * weight + 6.25 * height - 4.92 * age - 161); //female
         }
     }
     else{
     throw new InvalidParameterException() ;}
     return RMR;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpTransitions();
    }


    private void setUpTransitions(){
        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        // Register for Transitions Updates.
        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityTransitionUpdates(request, mPendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        //Log.i(TAG, "Transitions Api was successfully registered.");
                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Transitions Api could not be registered: " + e);
                    }
                });
    }


    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";

            case DetectedActivity.RUNNING:
                return "RUNNING";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    public class myTransitionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(TRANSITION_ACTION_RECEIVER, intent.getAction())) {
                /*mLogFragment.getLogView()
                        .echo("Unsupported action received in myTransitionReceiver class: action="
                                + intent.getAction());*/
                TextView textView3 = findViewById(R.id.action);
                textView3.setText("FAIL");
                return;
            }

            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    String theActivity = toActivityString(event.getActivityType());
                    String transType = toTransitionType(event.getTransitionType());
                    TextView textView3 = findViewById(R.id.action);
                    textView3.setText("Transition: "
                                    + theActivity + " (" + transType + ")" + "   "
                                    + new SimpleDateFormat("HH:mm:ss", Locale.UK)
                                    .format(new Date()));
                }
            }
        }
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
