package com.test.getdevicelocation2;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.*;

import javax.net.ssl.HttpsURLConnection;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity {


    private long UPDATE_INTERVAL = 60 * 1000;  /* 60 secs */
    private long FASTEST_INTERVAL = 40*1000; /* 40 sec */


    private FusedLocationProviderClient locationClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private double mElevation;
    private double mRMR;
    private ArrayList<String> locationUpdatesForTransition;
    private Boolean mRequestingLocationUpdates;
    private LocationCallback mLocationCallback;

    public SharedPreferences sharedPreferences;
    public static final String localUpdatesForTransitionKey="localUpdatesForTransitionKey";
    public static final String mActivityTransitionEventLastActivityIdKey="lastActivityId";
    public static final String mActivityTransitionEventLastActivityTransitionTypeKey="lastActivityTransType";
    public static final String mActivityTransitionEventElapsedTimeKey="lastActivityElapsedTime";
    public static final String mRMRKey="mRMRkey";

    String contentText = null;

    private PendingIntent mPendingIntent;
    private myTransitionReceiver mTransitionsReceiver;
    private  ActivityTransitionEvent mActivityTransitionEvent;

    private Button maleButton;
    private Button femaleButton;

    private final String TRANSITION_ACTION_RECEIVER =
            BuildConfig.APPLICATION_ID + "TRANSITION_ACTION_RECEIVER";


    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();
        locationClient = getFusedLocationProviderClient(this);
        isLocationServicesThere();

        Intent intent = new Intent(TRANSITION_ACTION_RECEIVER);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        mTransitionsReceiver = new myTransitionReceiver();
        registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITION_ACTION_RECEIVER));


        locationUpdatesForTransition=new ArrayList<String>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        getCurrentLocation();


        new Thread(new Runnable() {
            @Override
            public void run() {
                database = AppDatabase.getDatabaseInstance(getApplicationContext());
            }
        }).start();


        createLocationCallback();
        startLocationUpdates();
        mRequestingLocationUpdates=true;


        //mTransitionsReceiver.onReceive(MainActivity.this,intent);

        maleButton = findViewById(R.id.countRMRforMale);

        femaleButton = findViewById(R.id.countRMRforFemale);

        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), MaleRMRActivity.class);
                startActivity(i);
            }
        });

        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), FemaleRMRActivity.class);
                startActivity(i);
            }
        });


        Button button = findViewById(R.id.getLocation);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(v.getContext(), HeadActivity.class);
                startActivity(i);

                TextView textView1 = findViewById(R.id.location);
                TextView textView2 = findViewById(R.id.elevation);
                TextView textView4 = findViewById(R.id.RMR);



                getCurrentLocation();

                double Lon = mLastLocation.getLongitude();
                double Lat = mLastLocation.getLatitude();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String mRMRStr=sharedPreferences.getString(mRMRKey,"1500");
                mRMR=Double.parseDouble(mRMRStr);
                //textView4.setText("RMR"+ String.valueOf(mRMR));

                String time = getCurrentTime();
                //textView1.setText("Latitude: " + String.valueOf(Lat) + "   Longitude: " + String.valueOf(Lon) + "   Time:" + time
                //+"RMR"+ String.valueOf(mRMR));



                double elevation=getElevation(Lat, Lon);


                //textView2.setText("Elevation: " + String.valueOf(elevation));

                //For Checking Database Latest Activities
                /*Date timeMinus1Hour=new Date();
                timeMinus1Hour.setHours(timeMinus1Hour.getHours()-2);

                List<DetectedActivities> activities = database.activityDao().getActivitiesBetweenDates( timeMinus1Hour, new Date());*/

                //}
                //else textView1.setText("Location is unknown");


                /*List<ValuesMET> valuesMET = database.valueDao().getAllValues();
                List<DetectedActivities> activities = database.activityDao().getAll();

                Date activityTime = new Date(119, 4, 18, 14, 46, 6);
                Date fromTime = new Date(119, 4, 18, 13, 46, 6);
                // https://docs.oracle.com/javase/7/docs/api/java/util/Date.html
                DetectedActivities testActivity1 =
                        new DetectedActivities
                                (new String("RUNNING"), 8, 0, Lat, Lon, elevation, activityTime);
                DetectedActivities testActivity2 =
                        new DetectedActivities
                                (new String("RUNNING"), 8, 1, Lat, Lon, elevation, new Date());

                //database.activityDao().insertActivity(testActivity1);
                //database.activityDao().insertActivity(testActivity2);

                List<DetectedActivities> resultActivities = database.activityDao().getAll();
                List<DetectedActivities> last24HoursActivity =
                        database.activityDao().getActivitiesBetweenDates(fromTime, new Date());

                double resultCaloriesConsumption = getCaloriesConsumptionBetweenDates(fromTime, new Date());

                textView4.setText(String.valueOf(resultCaloriesConsumption));*/
            }
        });
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }
        locationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        }, Looper.myLooper());
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
            };
    }
    private void isLocationServicesThere() {


        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        boolean gps_enabled = false;
        boolean wifi_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
        }

        try {

            wifi_enabled =  wifi.isWifiEnabled();

        } catch (Exception ex) {
        }

        if (!gps_enabled ) {
            Toast.makeText(this, "Turn on GPS!", Toast.LENGTH_SHORT).show();
        }
        /*if(!network_enabled){
            Toast.makeText(this, "Turn on Wi-Fi!", Toast.LENGTH_SHORT).show();
        }*/

        if (!wifi_enabled){
            Toast.makeText(this, "Turn on Wi-Fi!", Toast.LENGTH_SHORT).show();
        }
    }
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        double latitude=location.getLatitude();
        double longitude=location.getLongitude();

        //contentText=null;
        double elevation=getElevation(latitude,longitude);

        if (elevation ==0.0)
        {elevation=mElevation;}

        //Date date = new Date(location.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        String time = dateFormat.format(getRealTimeinDateTypeFromElapsedTimeNanos(location.getElapsedRealtimeNanos()));

        locationUpdatesForTransition.add(String.valueOf(latitude));
        locationUpdatesForTransition.add(String.valueOf(longitude));
        locationUpdatesForTransition.add(String.valueOf(elevation));
        locationUpdatesForTransition.add(String.valueOf(location.getElapsedRealtimeNanos()));


        //EVERY TIME SAVE WHOLE ARRAY TO SHARED PREFERENCE , DONT KNOW HOW TO FIX
        saveArrayListToSharedPreferences(locationUpdatesForTransition,localUpdatesForTransitionKey);

        // New location has now been determined
        /*String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude() )+ " "+ time;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            //Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        locationClient.removeLocationUpdates(mLocationCallback);


    }


   /* private void getRMRUsingUsersEditParameters(int sex) {
        EditText ageEdit=findViewById(R.id.ageEdit);
        EditText heightEdit=findViewById(R.id.heightEdit);
        EditText weightEdit=findViewById(R.id.weightEdit);
        TextView resultRMRText=findViewById(R.id.resultRMR);

        int age = Integer.parseInt(ageEdit.getText().toString());
        double weight=Double.parseDouble(weightEdit.getText().toString());
        double height=Double.parseDouble(heightEdit.getText().toString());

        double resultRMR=countRMRUsingMifflinJeorEquation(sex,weight,height,age);
        resultRMRText.setText(String.valueOf(resultRMR));
    }*/

    public double getCaloriesConsumptionBetweenDates(Date from,Date to){
        double caloriesConsumption=0;
        List<DetectedActivities> listActivitiesBetweenDates=database.activityDao().getActivitiesBetweenDates(from,to);
        for(int i=0; i<listActivitiesBetweenDates.size()-1;i++){

            if(listActivitiesBetweenDates.get(i).getDetectedActivityId()
                    ==listActivitiesBetweenDates.get(i+1).getDetectedActivityId()) {
                int transTypeOld=listActivitiesBetweenDates.get(i).getTransitionType();
                int transTypeNext=listActivitiesBetweenDates.get(i+1).getTransitionType();
                if      ((transTypeOld == 0 && transTypeNext == 1) ||
                        (transTypeOld == 0 && transTypeNext == 2) ||
                        (transTypeOld == 2 && transTypeNext == 2) ||
                        (transTypeOld == 2 && transTypeNext == 1)) {
                    caloriesConsumption=caloriesConsumption+
                            getCaloriesForTransitionActivity(listActivitiesBetweenDates.get(i),listActivitiesBetweenDates.get(i+1));

                }
            }
        }
        return caloriesConsumption;
    }

    public double getCaloriesForTransitionActivity(DetectedActivities activityEnter,DetectedActivities activityExit){

        double height=activityEnter.getElevation()-activityExit.getElevation();
        double flatDistance=getFlatDistance
                (activityEnter.getLatitude(),activityEnter.getLongitude(),activityExit.getLatitude(),activityExit.getLongitude());
        double distance=getDistance
                (activityEnter.getLatitude(),activityEnter.getLongitude(),activityExit.getLatitude(),activityExit.getLongitude(),
                        activityEnter.getElevation(),activityExit.getElevation());

        double slope=getDegreeSlope(flatDistance,height);



         long durationOfTransitionInMinutes=TimeUnit.MILLISECONDS.toMinutes(activityExit.getTime().getTime()
                 -activityEnter.getTime().getTime());

         double speed=getSpeed(distance,durationOfTransitionInMinutes);

         //double valueMETofTransition= database.valueDao().getValueMETById(activityEnter.getDetectedActivityId()).getValueMET();
         //double correctedValueMETofTransition=correctValueMETBasedOnSlopeAndSpeed(valueMETofTransition,slope,speed);

        double VO2max=3.5 + speed*0.17+slope*speed*0.79;

        if(activityEnter.getDetectedActivityId()==1 ){
         VO2max=3.5 + speed*0.2+slope*speed*0.9;}
        if(activityEnter.getDetectedActivityId()==3){
          VO2max=3.5 ;}



         double MET=VO2max/3.5;

       // sharedPreferences=getSharedPreferences(mPreferences,getApplicationContext().MODE_PRIVATE);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String mRMRStr=sharedPreferences.getString(mRMRKey,"1500");
        mRMR=Double.parseDouble(mRMRStr);

        if(mRMR==0.0){
             String msg = "Insert your personal data for counting RMR!";
             Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            mRMR=1500;}

         double RMRinMinute= mRMR/1440;



         double caloriesForTransition=durationOfTransitionInMinutes * MET * RMRinMinute ;

        return caloriesForTransition;
    }

    public double correctValueMETBasedOnSlopeAndSpeed(double valueMETofTransition,double slope,double speed){
        double correctValueMET=valueMETofTransition;
        if( slope>0 && slope<0.1){
            correctValueMET=5;
        }
        if( slope> 0.1 && slope<0.2){
            correctValueMET=8.5;
        }

        return correctValueMET;
    }

    public double getDistance(double lat1, double lat2, double lon1,
                                  double lon2, double elev1, double elev2) {

        double distance = getFlatDistance(lat1, lat2, lon1, lon2);
         if(distance !=0){
        double height = elev1 - elev2;

        double distanceFull = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distanceFull);}
        return 0.0;
    }

//https://www.e-education.psu.edu/natureofgeoinfo/book/export/html/1837
    public double getDegreeSlope(double distance,double height){
        if(distance!=0){
        double slopeDegree=Math.tanh(height/distance);
        return slopeDegree;}
        return 0.0;
    }

    public double getPercentSlope(double distance,double height){
        if(distance!=0){
        double slopePersent=(height/distance);
        return slopePersent;}
        return 0.0;
    }

    public double getSpeed(double distance,double duration){
        if(duration!=0) {
            return distance / duration; //meters/sec
        }
            return 0.0;
    }

    //Harvesine method
    public double getFlatDistance(double lat1, double lon1, double lat2, double lon2) {
       float[] result = new float[1];
        Location.distanceBetween(lat1,lon1,lat2,lon2,result);
        return (double) result[0];
        /*final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double flatDistance=R * c * 1000;
        return flatDistance;*/
    }


    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

        }

        /*LocationManager mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double longitude = mLastLocation.getLongitude();
        double latitude = mLastLocation.getLatitude();
        //double[] result={latitude,longitude};
        */

        locationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
             if(location !=null) {


                 double Lat=location.getLatitude();
                 double Lon=location.getLongitude();
                 mLastLocation=location;

                 Date date = new Date(location.getTime());
                 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                 String time = dateFormat.format(date);

                 }
            }
        });

    }

    public String getCurrentTime(){
        return new SimpleDateFormat("HH:mm:ss", Locale.UK)
                .format(new Date());
        /*Date date = new Date(location.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String time = dateFormat.format(date);*/
    }
    public double getElevation(double latitude, double longitude) {
        double elevation;
        String ApiKey="AIzaSyCUsenQyvXXFMHTEcANoDDQhAUoSJo-dNI";
        String url = "https://maps.googleapis.com/maps/api/elevation/json?locations=" + latitude + "," + longitude + "&key="+ApiKey;
        //String url="https://developer.android.com/index.html";
        //if(contentText==null){
            // new ProgressTask().execute(url);
            try {
          contentText = new ProgressTask().execute(url).get();
               }
    catch (java.util.concurrent.ExecutionException | InterruptedException ei) {
     ei.printStackTrace();
        }

            //textView2.setText(contentText); //VIEW ALL REQUEST
            try {
   JSONObject jsonObj = new JSONObject(contentText);
   JSONArray resultEl = jsonObj.getJSONArray("results");
   JSONObject current = resultEl.getJSONObject(0);
   elevation = Double.parseDouble(current.getString("elevation"));
   mElevation=elevation;
   return elevation;

} catch (JSONException e) {
   e.printStackTrace();
   return 0;
}

        //}
        //return 0;
        }

    private void requestPermission(){
       ActivityCompat.requestPermissions(this,new String[] {ACCESS_FINE_LOCATION},1);
    }


//Insert initial table of MET values in Database
    public void prepareValuesMETContent() {
        ValuesMET RUNNING = new ValuesMET(8,8.0);
        database.valueDao().insertMETValue(RUNNING);

        ValuesMET STILL = new ValuesMET(3,1.0);
        database.valueDao().insertMETValue(STILL);

        ValuesMET WALKING = new ValuesMET(7,3.6);
        database.valueDao().insertMETValue(WALKING);

        ValuesMET ON_BICYCLE = new ValuesMET(1,5.0);
        database.valueDao().insertMETValue(ON_BICYCLE);



    }

    /*sex is int: -1 not given,0 male,1 female
    weight in kg
    heilght in cm*/
    public double countRMRUsingMifflinJeorEquation(int sex,double weight,double height,int age){
        mRMR=1;
     if(sex!=-1 && weight>0 && height>0&& age>0 ) {
         if (sex == 0) {
             mRMR = (int) (9.99 * weight + 6.25 * height - 4.92 * age + 5); //male
         } else if (sex == 1) {
             mRMR = (int) (9.99 * weight + 6.25 * height - 4.92 * age - 161); //female
         }
     }
     else{
     throw new InvalidParameterException() ;}
     return mRMR;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpTransitions();
        startLocationUpdates();
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
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
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

                   //TextView textView3 = findViewById(R.id.action);
                        //textView3.setText("Transitions Api was successfully registered.");
                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //Log.e(TAG, "Transitions Api could not be registered: " + e);
                        TextView textView3 = findViewById(R.id.action);
                        textView3.setText("Transitions Api could not be registered: .");
                    }
                });
    }


    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL"; //3
            case DetectedActivity.WALKING:
                return "WALKING"; //7

            case DetectedActivity.RUNNING:
                return "RUNNING"; //8

            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";//1
            default:
                return "UNKNOWN"; //4
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER"; //0
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT"; //1
            default:
                return "UNKNOWN";
        }
    }

    public synchronized ActivityTransitionEvent getmActivityTransitionEvent() {
        return mActivityTransitionEvent;
    }

    public synchronized void setmActivityTransitionEvent(ActivityTransitionEvent mActivityTransitionEvent) {
        this.mActivityTransitionEvent = mActivityTransitionEvent;
    }
    public synchronized void  clearLocationUpdates(){
        this.locationUpdatesForTransition.clear();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(localUpdatesForTransitionKey,"");
        editor.commit();
    }
    public synchronized ActivityTransitionEvent setmActivityTransitionEventWithParams(int activityId,int transType,long elapsedTime) {
        this.mActivityTransitionEvent = new ActivityTransitionEvent(activityId,transType,elapsedTime);
        return this.mActivityTransitionEvent;
    }

    public ActivityTransitionEvent retrieveValueFromSharedPreferencesToUpdatemActivityTransitionEventForFilteringFalseSignals() {
        int lastActivityId, lastActivityTransType;
        long lastActivityElapsedTime;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPreferences.contains(mActivityTransitionEventLastActivityIdKey) &&
                sharedPreferences.contains(mActivityTransitionEventLastActivityTransitionTypeKey) &&
                sharedPreferences.contains(mActivityTransitionEventElapsedTimeKey)) {

            lastActivityId = sharedPreferences.getInt(mActivityTransitionEventLastActivityIdKey, 1);
            lastActivityTransType = sharedPreferences.getInt
                    (mActivityTransitionEventLastActivityTransitionTypeKey, 0);
            lastActivityElapsedTime = sharedPreferences.getLong(mActivityTransitionEventElapsedTimeKey,
                    SystemClock.elapsedRealtime() - 1000000);
        } else {
            lastActivityId = 1;
            lastActivityTransType = 0;
            lastActivityElapsedTime = SystemClock.elapsedRealtime() - 1000000;
        }

        return setmActivityTransitionEventWithParams(lastActivityId, lastActivityTransType, lastActivityElapsedTime);

    }

    public void putNewValueActivityTransitionEventToSharedPreferences(ActivityTransitionEvent event){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(mActivityTransitionEventLastActivityIdKey,event.getActivityType());
        editor.putInt(mActivityTransitionEventLastActivityTransitionTypeKey,event.getTransitionType());
        editor.putLong(mActivityTransitionEventElapsedTimeKey,event.getElapsedRealTimeNanos());
        editor.commit();

    }

    /**
     *     Save and get ArrayList in SharedPreference
     */

    public void saveArrayListToSharedPreferences(ArrayList<String> list, String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<String> getArrayListFromSharedPreferences(String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> result= gson.fromJson(json, type);

        if(result==null){
            result=new ArrayList<String>();
        }
        return result;
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

                    int activityType = event.getActivityType();
                    int transitionType = event.getTransitionType();
                    String theActivity = toActivityString(activityType);
                    String transType = toTransitionType(transitionType);

                    List<DetectedActivities> activitiesBetween = new ArrayList<>();

                    //МОЖНО ТАК ОТСЕКАТЬ:
                      /*DetectedActivities lastActivity=database.activityDao().getLastActivity();
                    if (event.getActivityType() != lastActivity.getDetectedActivityId()
                            || event.getTransitionType() != lastActivity.getTransitionType()) {*/

                    //ВТОРОЙ МЕТОД ФИЛЬТРАЦИИ


                    mActivityTransitionEvent=
                            retrieveValueFromSharedPreferencesToUpdatemActivityTransitionEventForFilteringFalseSignals();


                        if (event.getActivityType() != getmActivityTransitionEvent().getActivityType()
                                || event.getTransitionType() != getmActivityTransitionEvent().getTransitionType()) {
                            //if (((SystemClock.elapsedRealtime() - (event.getElapsedRealTimeNanos() / 1000000)) / 1000) <= 5) {


                            /*if (event.getTransitionType() == 0){
                                if(!mRequestingLocationUpdates){
                                    startLocationUpdates();
                                   mRequestingLocationUpdates=true;
                                }
                            }*/
                        //ЕСЛИ ТИП=1 ТОГДА ОБРАБОТКА ЛОКАЦИЙ ИЗ МАССИВА

                            if (event.getTransitionType() == 1) {

                                /*if(mRequestingLocationUpdates){
                                    stopLocationUpdates();
                                    mRequestingLocationUpdates=false;
                                }*/

                                tempPointsProcessingActivity(event);
                            }

                            // ОБРАБОТКА ЕСЛИ ИСПОЛЬЗОВАТЬ shared

                            putNewValueActivityTransitionEventToSharedPreferences(event);

                            //setmActivityTransitionEvent(event);

                            activityProcessing(event);




                    /*new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DetectedActivities lastActivity =
                                    new DetectedActivities(theActivity,activityType,transitionType,latitude,longitude,elevation,new Date());

                            database.activityDao().insertActivity(lastActivity);
                        }
                    }).start();*/
                            String msg1 = "New activity: " +
                                    theActivity + "   " +
                                    transitionType + " " + new SimpleDateFormat("HH:mm:ss", Locale.UK)
                                    .format(getRealTimeinDateTypeFromElapsedTimeNanos(event.getElapsedRealTimeNanos()));
                            Toast.makeText(getApplicationContext(), msg1, Toast.LENGTH_LONG).show();

                        /*TextView textView3 = findViewById(R.id.action);
                        textView3.setText("Transition: "
                                + theActivity + " (" + transType + ")" + "   "
                                + new SimpleDateFormat("HH:mm:ss", Locale.UK)
                                .format(new Date()));//new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());*/
                            // }
                            //}
                        //}
                    }
                }
            }
        }
        public void activityProcessing(ActivityTransitionEvent event) {

            int activityType =event.getActivityType();
            int transitionType =event.getTransitionType() ;
            String theActivity =toActivityString(activityType) ;

            getCurrentLocation();
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            double elevation=getElevation(latitude, longitude);

            if (elevation == 0) {
                elevation = mElevation;
            }


            Date timeReal= getRealTimeinDateTypeFromElapsedTimeNanos(event.getElapsedRealTimeNanos());

            DetectedActivities lastActivity =
                    new DetectedActivities(theActivity, activityType, transitionType, latitude, longitude, elevation,
                            timeReal);

            database.activityDao().insertActivity(lastActivity);
        }
        public void tempPointsProcessingActivity(ActivityTransitionEvent event) {
            List<DetectedActivities> activitiesBetween = new ArrayList<>();
            double latitudeTemp, longitudeTemp, elevationTemp;
            Date timeTempDate;
            int activityType = event.getActivityType();
            String theActivity = toActivityString(activityType);

            locationUpdatesForTransition=getArrayListFromSharedPreferences(localUpdatesForTransitionKey);

            for (int i = 0; i < locationUpdatesForTransition.size() - 4; i = i + 4) {
                //time less when detected enter activity
                if (Long.parseLong(locationUpdatesForTransition.get(i + 3))
                        >= mActivityTransitionEvent.getElapsedRealTimeNanos()) {

                    //time more than detected exit activity
                    if (Long.parseLong(locationUpdatesForTransition.get(i + 3))
                            > event.getElapsedRealTimeNanos()) {
                        break;
                    }

                    latitudeTemp = Double.parseDouble(locationUpdatesForTransition.get(i));
                    longitudeTemp = Double.parseDouble(locationUpdatesForTransition.get(i + 1));
                    elevationTemp = Double.parseDouble(locationUpdatesForTransition.get(i + 2));
                    String timeTemp = locationUpdatesForTransition.get(i + 3);

                    timeTempDate = getRealTimeinDateTypeFromElapsedTimeNanos(Long.parseLong(timeTemp));
                    //For temporary values
                    int transitionType = 2;

                    activitiesBetween.add(new DetectedActivities(theActivity, activityType, transitionType,
                            latitudeTemp, longitudeTemp, elevationTemp, timeTempDate));
                }

                //locationUpdatesForTransition.clear();
                clearLocationUpdates();
                database.activityDao().insertAllActivities(activitiesBetween);
            }
        }
    }


    public Date getRealTimeinDateTypeFromElapsedTimeNanos(long timeNanos) {
        Date timeReal=new Date();
        timeReal.setTime(System.currentTimeMillis()-
                TimeUnit.NANOSECONDS.toMillis(SystemClock.elapsedRealtimeNanos()-timeNanos));
        return timeReal;
    }

    public double getmRMR() {
        return mRMR;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setRMR(double rmr){
        mRMR=rmr;
    }

    public Location getmLastLocation() {

        return mLastLocation;
    }

    public double getmElevation() {

        return mElevation;
    }

    public AppDatabase getDatabase() {

        return database;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTransitionsReceiver);
        clearLocationUpdates();
        AppDatabase.destroyInstance();
    }

    /*@Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mTransitionsReceiver);
    }*/

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
