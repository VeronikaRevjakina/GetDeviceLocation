package com.test.getdevicelocation2;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecyclerActivity extends MainActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);


        Intent intent = getIntent();
        String hoursStr= intent.getStringExtra("hours");
        int hours=Integer.parseInt(hoursStr);
        Date fromTime=new Date();
        fromTime.setHours(new Date().getHours()-hours);
        //List<DetectedActivities> last24HoursActivity=
               // getDatabase().activityDao().getActivitiesBetweenDates(fromTime,new Date());

        //final List<DetectedActivities> last24HoursActivity=getDatabase().activityDao().getExitActivitiesBetweenDates(fromTime,new Date());

        List<DetectedActivities> temp=new ArrayList<>();
        final List<DetectedActivities> last24HoursActivityTemp=getDatabase().activityDao().getExitActivitiesBetweenDates(fromTime,new Date());
        for(int i=1;i<last24HoursActivityTemp.size();i++){
            if(Integer.compare(last24HoursActivityTemp.get(i).getDetectedActivityId(), last24HoursActivityTemp.get(i-1).getDetectedActivityId())!=0){
            temp.add(last24HoursActivityTemp.get(i));
            }
        }
        final List<DetectedActivities> last24HoursActivity=temp;


        // specify an adapter
        mAdapter = new MyAdapter(this,last24HoursActivity);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                DetectedActivities activity = last24HoursActivity.get(position);

                Intent intent = new Intent(view.getContext(), SingleDetectedActivity.class);
                String activityIdStr=String.valueOf(activity.getId());
                intent.putExtra("key",activityIdStr);
                //Toast.makeText(getApplicationContext(), activity.getId() + " is selected!", Toast.LENGTH_SHORT).show();
                startActivity(intent);

                //Toast.makeText(getApplicationContext(), activity.getId() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));




    }


}
