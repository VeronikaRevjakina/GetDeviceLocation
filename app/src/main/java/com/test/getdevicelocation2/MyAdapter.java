package com.test.getdevicelocation2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        private LayoutInflater inflater;
        private List<DetectedActivities> activities;

        MyAdapter(Context context, List<DetectedActivities> activities) {
            this.activities=activities;
            this.inflater = LayoutInflater.from(context);
        }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_activity, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
        DetectedActivities activity = activities.get(position);
        holder.activityNameView.setText(activity.getDetectedActivity());
        holder.timeOfActivityView.setText(activity.getTime().toString());
    }
    @Override
    public int getItemCount() {
        return activities.size();
    }

 public class ViewHolder extends RecyclerView.ViewHolder {
    final TextView activityNameView;
    final TextView timeOfActivityView;
    ViewHolder(View view){
        super(view);
        activityNameView = (TextView) view.findViewById(R.id.activityNameView);
        timeOfActivityView=view.findViewById(R.id.timeOfActivityView);
        }
}


}
