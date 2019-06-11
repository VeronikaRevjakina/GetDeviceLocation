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
        String activityIdStr = intent.getStringExtra("key");
        //singleActionText.setText(activityIdStr);
        int activityId = Integer.parseInt(activityIdStr);
        //int activity_id_enter = getDatabase().activityDao().getActivityPrevById(activityId);
        List<DetectedActivities> allActivitiesForTransition = getDatabase().activityDao().getFullTransitionById(activityId);

        /*DetectedActivities currentActivity = getDatabase().activityDao().getActivityById(activityId);
        DetectedActivities previousActivity = getDatabase().activityDao().getActivityById(activityId - 1);*/

        //singleActionText.setText(String.valueOf(previousActivity.getElevation()));
        //List<DetectedActivities> currentAndLastActivity=getDatabase().activityDao().getListActivitiesById(activityId);

        DetectedActivities previousActivity=allActivitiesForTransition.get(0);
          DetectedActivities     currentActivity=allActivitiesForTransition.get(allActivitiesForTransition.size()-1);
        double calorieConsumptionSingleAction=0.0;
        double distanceFull=0.0;
        long durationBetweenTwoLastActivitiesInMinutes=0;
        double totalHeightChange=0.0;
        if( currentActivity.getDetectedActivityId()==3){
            durationBetweenTwoLastActivitiesInMinutes= durationBetweenTwoLastActivitiesInMinutes+TimeUnit.MILLISECONDS.toMinutes
                    ( currentActivity.getTime().getTime()- previousActivity.getTime().getTime());
            calorieConsumptionSingleAction=getCaloriesForTransitionActivity
                    (previousActivity, currentActivity);
            calorieConsumptionSingleActionText.setText("Calories consumption for chosen activity : " +
                    String.valueOf(calorieConsumptionSingleAction));
            distanceFull=0.0;
            totalHeightChange=0.0;
            singleActionText.setText("Total duration : " + String.valueOf(durationBetweenTwoLastActivitiesInMinutes) +
                    "  Total distance: " + String.valueOf(distanceFull) + "  Total height change: " + String.valueOf(totalHeightChange));
        }

        for (int i = 0; i < allActivitiesForTransition.size()-1; i++) {

            previousActivity=allActivitiesForTransition.get(i);
            currentActivity=allActivitiesForTransition.get(i+1);

             calorieConsumptionSingleAction = calorieConsumptionSingleAction+getCaloriesForTransitionActivity
                    (previousActivity, currentActivity);
             // calorieConsumptionSingleActionText.setText(String.valueOf(calorieConsumptionSingleAction));

            distanceFull = distanceFull+getDistance(previousActivity.getLatitude(), previousActivity.getLongitude(),
                    currentActivity.getLatitude(), currentActivity.getLongitude(),
                    previousActivity.getElevation(), currentActivity.getElevation());



             durationBetweenTwoLastActivitiesInMinutes= durationBetweenTwoLastActivitiesInMinutes+TimeUnit.MILLISECONDS.toMinutes
                    ( currentActivity.getTime().getTime()- previousActivity.getTime().getTime());

            //double speed = getSpeed(distanceFull, durationBetweenTwoLastActivitiesInMinutes);
            double distanceFlat = getFlatDistance(previousActivity.getLatitude(), previousActivity.getLongitude(),
                    currentActivity.getLatitude(), currentActivity.getLongitude());
            double height = currentActivity.getElevation() - previousActivity.getElevation();
            totalHeightChange=totalHeightChange+height;

            //double slopeInDegrees = getDegreeSlope(distanceFlat, height);



        }

        calorieConsumptionSingleActionText.setText("Calories consumption for chosen activity : " +
                String.valueOf(calorieConsumptionSingleAction));

        singleActionText.setText("Total duration : " + String.valueOf(durationBetweenTwoLastActivitiesInMinutes) +
                "  Total distance: " + String.valueOf(distanceFull) + "  Total height change: " + String.valueOf(totalHeightChange));
    }

}
