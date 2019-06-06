package com.test.getdevicelocation2;

import android.Manifest;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.ads.mediation.MediationServerParameters;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.Timestamp;
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


    private long UPDATE_INTERVAL = 100 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */


    private FusedLocationProviderClient locationClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private double mElevation;
    private double mRMR;
    private List<String> locationUpdatesForTransition;

    public SharedPreferences sharedPreferences;
    public static final String mPreferences="myPreferences";
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

        Intent intent = new Intent(TRANSITION_ACTION_RECEIVER);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        mTransitionsReceiver = new myTransitionReceiver();
        registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITION_ACTION_RECEIVER));

        mActivityTransitionEvent = new ActivityTransitionEvent(1,0,100000);

         locationUpdatesForTransition=new ArrayList<String>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        //database = AppDatabase.getDatabaseInstance(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                database = AppDatabase.getDatabaseInstance(getApplicationContext());
            }
        }).start();



        startLocationUpdates();

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
                //textView1.setText("Location: "+location.toString());

                /*try {
                    textView4.setText("RMR is: "+ String.valueOf(
                            countRMRUsingMifflinJeorEquation(1,60,165,21)));
                } catch (InvalidParameterException e){
                    e.printStackTrace();
                }*/


                getCurrentLocation();
                countRMRUsingMifflinJeorEquation(1, 60, 165, 21);
                //if(mLastLocation!=null) {
                double Lon = mLastLocation.getLongitude();
                double Lat = mLastLocation.getLatitude();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String mRMRStr=sharedPreferences.getString(mRMRKey,"1500");
                mRMR=Double.parseDouble(mRMRStr);
                textView4.setText("RMR"+ String.valueOf(mRMR));

                String time = getCurrentTime();
                textView1.setText("Latitude: " + String.valueOf(Lat) + "   Longitude: " + String.valueOf(Lon) + "   Time:" + time
                +"RMR"+ String.valueOf(mRMR));



                double elevation;

                if (getElevation(Lat, Lon) != 0.0) {
                    elevation = getElevation(Lat, Lon);
                }
                elevation = mElevation;

                textView2.setText("Elevation: " + String.valueOf(elevation));


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
                },
                Looper.myLooper());
    }
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        double latitude=mLastLocation.getLatitude();
        double longitude=mLastLocation.getLongitude();

        double elevation;

        if (getElevation(latitude,longitude)!=0){
            elevation=getElevation(latitude,longitude);}
        elevation=mElevation;

        Date date = new Date(location.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String time = dateFormat.format(date);

        locationUpdatesForTransition.add(String.valueOf(latitude));
        locationUpdatesForTransition.add(String.valueOf(longitude));
        locationUpdatesForTransition.add(String.valueOf(elevation));
        locationUpdatesForTransition.add(time);
        // New location has now been determined
        /*String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude() )+ " "+ time;
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();*/
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
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
                if (listActivitiesBetweenDates.get(i).getTransitionType() == 0
                        && listActivitiesBetweenDates.get(i+1).getTransitionType() == 1) {
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
            countRMRUsingMifflinJeorEquation(1,60,165,21);}

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
        if(contentText==null){
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
}

        }
        return 0;

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

    public synchronized void setmActivityTransitionEventWithParams(int activityId,int transType,long elapsedTime) {
        this.mActivityTransitionEvent = new ActivityTransitionEvent(activityId,transType,elapsedTime);
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


                    /* int lastActivityId,lastActivityTransType;
                     long lastActivityElapsedTime;
                     SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if(sharedPreferences.contains(mActivityTransitionEventLastActivityIdKey) &&
                            sharedPreferences.contains(mActivityTransitionEventLastActivityTransitionTypeKey) &&
                            sharedPreferences.contains(mActivityTransitionEventElapsedTimeKey)) {

                        lastActivityId = sharedPreferences.getInt(mActivityTransitionEventLastActivityIdKey, 1);
                        lastActivityTransType = sharedPreferences.getInt
                                (mActivityTransitionEventLastActivityTransitionTypeKey, 0);
                        lastActivityElapsedTime = sharedPreferences.getLong(mActivityTransitionEventElapsedTimeKey,
                                SystemClock.elapsedRealtime() - 1000000);
                    }
                    else {
                        lastActivityId=1;
                        lastActivityTransType=0;
                        lastActivityElapsedTime=SystemClock.elapsedRealtime() - 1000000;}

                        setmActivityTransitionEventWithParams(lastActivityId,lastActivityTransType,lastActivityElapsedTime); */

                        if (event.getActivityType() != getmActivityTransitionEvent().getActivityType()
                                || event.getTransitionType() != getmActivityTransitionEvent().getTransitionType()) {
                            //if (((SystemClock.elapsedRealtime() - (event.getElapsedRealTimeNanos() / 1000000)) / 1000) <= 5) {


                       // ОБРАБОТКА ЕСЛИ ИСПОЛЬЗОВАТЬ shared

                       /* SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putInt(mActivityTransitionEventLastActivityIdKey,event.getActivityType());
                        editor.putInt(mActivityTransitionEventLastActivityTransitionTypeKey,event.getTransitionType());
                        editor.putLong(mActivityTransitionEventElapsedTimeKey,event.getElapsedRealTimeNanos());
                        editor.commit();*/


                            setmActivityTransitionEvent(event);
                    /*if (transitionType == 1) {
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                        double latitudeTemp, longitudeTemp, elevationTemp;
                        Date timeTempDate;
                        for (int i = 0; i < locationUpdatesForTransition.size() - 4; i = i + 4) {
                            latitudeTemp = Double.parseDouble(locationUpdatesForTransition.get(i));
                            longitudeTemp = Double.parseDouble(locationUpdatesForTransition.get(i + 1));
                            elevationTemp = Double.parseDouble(locationUpdatesForTransition.get(i + 2));
                            String timeTemp = locationUpdatesForTransition.get(i + 3);
                            try {
                                timeTempDate = formatter.parse(timeTemp);
                            } catch (java.text.ParseException e) {
                                timeTempDate = new Date();
                            }
                            activitiesBetween.add(new DetectedActivities(theActivity, activityType, transitionType,
                                    latitudeTemp, longitudeTemp, elevationTemp, timeTempDate));
                        }
                        locationUpdatesForTransition.clear();
                        database.activityDao().insertAllActivities(activitiesBetween);
                    } else {*/

                            activityProcessing(event);
                            /*getCurrentLocation();
                            double latitude = mLastLocation.getLatitude();
                            double longitude = mLastLocation.getLongitude();
                            double elevation;

                            if (getElevation(latitude, longitude) != 0) {
                                elevation = getElevation(latitude, longitude);
                            }
                            elevation = mElevation;
                            DetectedActivities lastActivity =
                                    new DetectedActivities(theActivity, activityType, transitionType,
                                            latitude, longitude, elevation, new Date());
                            database.activityDao().insertActivity(lastActivity);*/
                            //TODO : Add new database where store duration and calories for transition using all location updates


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
                                    .format(getRealTime(event.getElapsedRealTimeNanos()));
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
            double elevation;

            if (getElevation(latitude, longitude) != 0) {
                elevation = getElevation(latitude, longitude);
            }
            //IMPROVE HERE IF STOPS, remove else
            //else{elevation = mElevation;}
            elevation = mElevation;

            Date timeReal= getRealTime(event.getElapsedRealTimeNanos());

            DetectedActivities lastActivity =
                    new DetectedActivities(theActivity, activityType, transitionType, latitude, longitude, elevation,
                            timeReal);

            database.activityDao().insertActivity(lastActivity);
        }
    }

    public Date getRealTime(long timeNanos) {
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
