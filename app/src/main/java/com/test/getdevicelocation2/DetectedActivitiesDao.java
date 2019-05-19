package com.test.getdevicelocation2;

import com.test.getdevicelocation2.DetectedActivities;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Delete;


import java.util.List;

@Dao
public interface DetectedActivitiesDao {

    @Query("SELECT * FROM activities")
    List<DetectedActivities> getAll();

    @Insert()
    void insertActivity(DetectedActivities activity);

    @Insert()
    void insertAllActivities(DetectedActivities... activities);

    @Delete
    void deleteActivity (DetectedActivities activity);

    @Query("SELECT * FROM activities ORDER BY Id DESC LIMIT 1")
    DetectedActivities getLastActivity();

    @Query("SELECT * FROM activities ORDER BY Id DESC LIMIT 2")
    List<DetectedActivities> getTwoLastActivities();
}
