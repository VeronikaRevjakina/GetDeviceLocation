package com.test.getdevicelocation2;

import android.content.Intent;
import android.os.Bundle;

import android.widget.TextView;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SingleDetectedActivity extends MainActivity {
    private TextView singleActionText;
    private TextView calorieConsumptionSingleActionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_activity);

        singleActionText = findViewById(R.id.singleAction);
        calorieConsumptionSingleActionText = findViewById(R.id.calorieConsumptionSingleAction);
        Intent intent = getIntent();
        String activityIdStr= intent.getStringExtra("key");
        //singleActionText.setText(activityIdStr);
       int activityId=Integer.parseInt(activityIdStr);
       DetectedActivities currentActivity=getDatabase().activityDao().getActivityById(activityId);
       DetectedActivities previousActivity=getDatabase().activityDao().getActivityById(activityId-1);
       //singleActionText.setText(String.valueOf(previousActivity.getElevation()));
        //List<DetectedActivities> currentAndLastActivity=getDatabase().activityDao().getListActivitiesById(activityId);
        double calorieConsumptionSingleAction=getCaloriesForTransitionActivity
                (previousActivity,currentActivity);
        calorieConsumptionSingleActionText.setText(String.valueOf(calorieConsumptionSingleAction));

       // calorieConsumptionSingleActionText.setText(String.valueOf(calorieConsumptionSingleAction));

        double distanceFull=getDistance(previousActivity.getLatitude(),previousActivity.getLongitude(),
                currentActivity.getLatitude(),currentActivity.getLongitude(),
                previousActivity.getElevation(),currentActivity.getElevation());

        long durationBetweenTwoLastActivitiesInMinutes= TimeUnit.MILLISECONDS.toMinutes
                ( currentActivity.getTime().getTime()- previousActivity.getTime().getTime());

        double speed=getSpeed(distanceFull,durationBetweenTwoLastActivitiesInMinutes);
        double distanceFlat=getFlatDistance(previousActivity.getLatitude(),previousActivity.getLongitude(),
                currentActivity.getLatitude(),currentActivity.getLongitude());
        double height= currentActivity.getElevation()-previousActivity.getElevation();
        double slopeInDegrees=getDegreeSlope(distanceFlat,height);

        singleActionText.setText("Duration :"+String.valueOf(durationBetweenTwoLastActivitiesInMinutes)+
                "Distance: "+String.valueOf(distanceFull) +"Speed: "+String.valueOf(speed)+"Slope in degrees: "+
                String.valueOf(slopeInDegrees));

    }

}
