package com.test.getdevicelocation2;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeadActivity extends MainActivity {

    private Button buttonCalcActivity;
    private Button getActivityListRecyclerButton;
    private TextView locationText;
    private TextView elevationText;
    private TextView actionText;
    private TextView caloriesConsumptionText;
    private EditText timeEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic);

        buttonCalcActivity=findViewById(R.id.CalcActivity);
        locationText = findViewById(R.id.location);
        elevationText = findViewById(R.id.elevation);
        actionText=findViewById(R.id.action);
        caloriesConsumptionText = findViewById(R.id.calorieConsumption);
        timeEdit=findViewById(R.id.timeEdit);

        getActivityListRecyclerButton=findViewById(R.id.getActivityListRecyclerView);

        getActivityListRecyclerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(v.getContext(),RecyclerActivity.class);
                startActivity(i);
            }});

    //textView1.setText("Location: "+location.toString());

                /*try {
                    textView4.setText("RMR is: "+ String.valueOf(
                            countRMRUsingMifflinJeorEquation(1,60,165,21)));
                } catch (InvalidParameterException e){
                    e.printStackTrace();
                }*/
        buttonCalcActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //textView1.setText("Location: "+location.toString());

                /*try {
                    textView4.setText("RMR is: "+ String.valueOf(
                            countRMRUsingMifflinJeorEquation(1,60,165,21)));
                } catch (InvalidParameterException e){
                    e.printStackTrace();
                }*/


                getCurrentLocation();
                countRMRUsingMifflinJeorEquation(1,60,165,21);
                //if(mLastLocation!=null) {

                double Lon = getmLastLocation().getLongitude();
                double Lat = getmLastLocation().getLatitude();

                String time = getCurrentTime();
                locationText.setText("Latitude: " + String.valueOf(Lat) + "   Longitude: " + String.valueOf(Lon) + "   Time:" + time);

                double elevation;

                if (getElevation(Lat,Lon)!=0.0){
                    elevation=getElevation(Lat,Lon);}
                elevation=getmElevation();

                elevationText.setText("Elevation: " + String.valueOf(elevation));


                //}
                //else textView1.setText("Location is unknown");


                List<ValuesMET> valuesMET=getDatabase().valueDao().getAllValues();
                List<DetectedActivities> activities=getDatabase().activityDao().getAll();

                //Date activityTime=new Date(119,4,24,9,46,6);
                //Date fromTime=new Date(119,4,18,13,46,6);

                Date fromTime=new Date();
                int hours=Integer.parseInt(timeEdit.getText().toString());
                fromTime.setHours(fromTime.getHours()-hours);

                Date timeMinus1Hour=new Date();
                timeMinus1Hour.setHours(timeMinus1Hour.getHours()-1);

                // https://docs.oracle.com/javase/7/docs/api/java/util/Date.html
                DetectedActivities testActivity1=
                        new DetectedActivities
                                (new String("WALKING"),7,0,
                                        Lat+30,Lon-30,elevation-100,timeMinus1Hour);
                DetectedActivities testActivity2=
                        new DetectedActivities
                                (new String("WALKING"),7,1,Lat,Lon,elevation,new Date());
                AppDatabase database=getDatabase();
                database.activityDao().insertActivity(testActivity1);
                database.activityDao().insertActivity(testActivity2);

               DetectedActivities lastActivity=database.activityDao().getLastActivity();


               List<DetectedActivities> twoLastActivities=database.activityDao().getTwoLastActivities();
                long durationBetweenTwoLastActivitiesInMinutes= TimeUnit.MILLISECONDS.toMinutes
                        (twoLastActivities.get(0).getTime().getTime()-twoLastActivities.get(1).getTime().getTime());

                actionText.setText("Recent activity : " +String.valueOf(durationBetweenTwoLastActivitiesInMinutes)+ " min "
                        + lastActivity.getDetectedActivity());

                List<DetectedActivities> resultActivities=database.activityDao().getAll();
                List<DetectedActivities> last24HoursActivity=
                        database.activityDao().getActivitiesBetweenDates(fromTime,new Date());

                double resultCaloriesConsumption=getCaloriesConsumptionBetweenDates(fromTime,new Date());

                caloriesConsumptionText.setText("Calries consumprion for your interval :"+
                        String.valueOf(resultCaloriesConsumption));


            }
        });
}
}
