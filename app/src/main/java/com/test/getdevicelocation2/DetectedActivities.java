package com.test.getdevicelocation2;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;


import com.test.getdevicelocation2.DateConverter;

import java.util.Date;
@Entity(tableName ="activities")
@TypeConverters({DateConverter.class})
public class DetectedActivities {

    @PrimaryKey (autoGenerate = true)
    private int id;

    @ColumnInfo(name = "detected_activity_name")
    private String detectedActivity;

    @ColumnInfo(name="detected_activity_id")
    private int detectedActivityId; //can do foreign key on MET id ,but i dont need delete on cascade

    @ColumnInfo(name="transition_type")
    private int transitionType; //0-ENTER 1-EXIT

    @ColumnInfo(name="latitude")
    private double latitude;

    @ColumnInfo(name="longitude")
    private double longitude;

    @ColumnInfo(name="elevation")
    private double elevation; //current elevation

    @ColumnInfo(name="time") //typeAffinity = Timestamp)
    private Date time;

    //public DetectedActivities(){}

    public DetectedActivities(String detectedActivity,int detectedActivityId,int transitionType,
                              double latitude,double longitude,double elevation,Date time){

        this.detectedActivity=detectedActivity;
        this.detectedActivityId=detectedActivityId;
        this.transitionType=transitionType;
        this.latitude=latitude;
        this.longitude=longitude;
        this.elevation=elevation;
        this.time=time;
    }

    public String getDetectedActivity() {
        return detectedActivity;
    }


    public void setElevation(double elevation) {

        this.elevation = elevation;
    }

    public int getTransitionType() {

        return transitionType;
    }

    public Date getTime() {

        return time;
    }

    public double getLongitude() {

        return longitude;
    }

    public double getLatitude() {

        return latitude;
    }


    public int getId() {

        return id;
    }

    public double getElevation() {

        return elevation;
    }

    public int getDetectedActivityId() {

        return detectedActivityId;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    public void setTime(Date time) {

        this.time = time;
    }

    public void setLongitude(double longitude) {

        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {

        this.latitude = latitude;
    }

    public void setId( int id) {

        this.id = id;
    }

    public void setDetectedActivityId(int detectedActivityId) {

        this.detectedActivityId = detectedActivityId;
    }

    public void setDetectedActivity(String detectedActivity) {

        this.detectedActivity = detectedActivity;
    }
}
