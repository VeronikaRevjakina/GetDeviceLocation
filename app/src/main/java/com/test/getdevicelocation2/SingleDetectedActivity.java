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
singleActionText.setText(activityIdStr);
      /* int activityId=Integer.getInteger(activityIdStr);
        List<DetectedActivities> currentAndLastActivity=getDatabase().activityDao().getListActivitiesById(activityId);
        double calorieConsumptionSingleAction=getCaloriesForTransitionActivity
                (currentAndLastActivity.get(1),currentAndLastActivity.get(0));

        calorieConsumptionSingleActionText.setText(String.valueOf(calorieConsumptionSingleAction));

        double distanceFull=getDistance(currentAndLastActivity.get(1).getLatitude(),currentAndLastActivity.get(1).getLongitude(),
                currentAndLastActivity.get(0).getLatitude(),currentAndLastActivity.get(0).getLongitude(),
                currentAndLastActivity.get(1).getElevation(),currentAndLastActivity.get(0).getElevation());

        long durationBetweenTwoLastActivitiesInMinutes= TimeUnit.MILLISECONDS.toMinutes
                ( currentAndLastActivity.get(0).getTime().getTime()- currentAndLastActivity.get(1).getTime().getTime());

        double speed=getSpeed(distanceFull,durationBetweenTwoLastActivitiesInMinutes);
        double distanceFlat=getFlatDistance(currentAndLastActivity.get(1).getLatitude(),currentAndLastActivity.get(1).getLongitude(),
                currentAndLastActivity.get(0).getLatitude(),currentAndLastActivity.get(0).getLongitude());
        double height= currentAndLastActivity.get(0).getElevation()-currentAndLastActivity.get(1).getElevation();
        double slopeInDegrees=getDegreeSlope(distanceFlat,height);

        singleActionText.setText("Duration :"+String.valueOf(durationBetweenTwoLastActivitiesInMinutes)+
                "Distance: "+String.valueOf(distanceFull) +"Speed: "+String.valueOf(speed)+"Slope in degrees: "+
                String.valueOf(slopeInDegrees)); */

    }

}
