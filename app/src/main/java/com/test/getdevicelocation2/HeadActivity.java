package com.test.getdevicelocation2;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import java.util.Date;
import java.util.List;

public class HeadActivity extends MainActivity {

    private Button buttonCalcActivity;
    private TextView locationText;
    private TextView elevationText;
    private TextView actionText;
    private TextView caloriesConsumptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic);

        buttonCalcActivity=findViewById(R.id.CalcActivity);
        locationText = findViewById(R.id.location);
        elevationText = findViewById(R.id.elevation);
        actionText=findViewById(R.id.action);
        caloriesConsumptionText = findViewById(R.id.calorieConsumption);

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

                Date activityTime=new Date(119,4,18,14,46,6);
                Date fromTime=new Date(119,4,18,13,46,6);
                // https://docs.oracle.com/javase/7/docs/api/java/util/Date.html
                DetectedActivities testActivity1=
                        new DetectedActivities
                                (new String("RUNNING"),8,0,Lat,Lon,elevation,activityTime);
                DetectedActivities testActivity2=
                        new DetectedActivities
                                (new String("RUNNING"),8,1,Lat,Lon,elevation,new Date());
                AppDatabase database=getDatabase();
                database.activityDao().insertActivity(testActivity1);
                database.activityDao().insertActivity(testActivity2);

                List<DetectedActivities> resultActivities=database.activityDao().getAll();
                List<DetectedActivities> last24HoursActivity=
                        database.activityDao().getActivitiesBetweenDates(fromTime,new Date());

                double resultCaloriesConsumption=getCaloriesConsumptionBetweenDates(fromTime,new Date());

                caloriesConsumptionText.setText(String.valueOf(resultCaloriesConsumption));
            }
        });
}
}
