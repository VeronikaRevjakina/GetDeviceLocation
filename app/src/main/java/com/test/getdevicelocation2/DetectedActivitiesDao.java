package com.test.getdevicelocation2;

import com.test.getdevicelocation2.DetectedActivities;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.TypeConverters;


import java.util.Date;
import java.util.List;

@Dao
@TypeConverters({DateConverter.class})
public interface DetectedActivitiesDao {

    @Query("SELECT * FROM activities")
    List<DetectedActivities> getAll();

    //@TypeConverters({DateConverter.class})
    //Long checkTime=DateConverter.dateToTimestamp(new Date());
    // https://www.sqlitetutorial.net/sqlite-date/   https://www.sqlite.org/lang_datefunc.html

    @Query("SELECT * FROM activities WHERE  time BETWEEN :from AND :to ; ") //datetime('now','-1 day')
    List<DetectedActivities> getActivitiesBetweenDates(Date from,Date to);

    @Query("SELECT * FROM activities WHERE Id=:id ")
    DetectedActivities getActivityById(int id);

    @Query("SELECT Id FROM activities WHERE Id<:id AND transition_type=0 ORDER BY Id DESC LIMIT 1")
    int getActivityPrevById(int id);

    @Query("SELECT * FROM activities WHERE Id >= ( SELECT Id FROM activities WHERE Id<:id AND transition_type=0 ORDER BY Id DESC LIMIT 1) AND Id<=:id ")
    List<DetectedActivities> getFullTransitionById(int id);


    @Query("SELECT * FROM activities WHERE Id=:id AND Id=:id-1 ORDER BY Id DESC")
    List<DetectedActivities> getListActivitiesById(int id);

    @Query("SELECT * FROM activities WHERE transition_type=1 AND (time BETWEEN :from AND :to)")
    List<DetectedActivities> getExitActivitiesBetweenDates(Date from,Date to);

    @Query("SELECT * FROM activities WHERE transition_type=1 ")
    List<DetectedActivities> getAllExitActivities();

    @Insert()
    void insertActivity(DetectedActivities activity);

    @Insert()
    void insertAllActivities(List<DetectedActivities> activities);

    @Delete
    void deleteActivity (DetectedActivities activity);

    @Query("SELECT * FROM activities ORDER BY Id DESC LIMIT 1")
    DetectedActivities getLastActivity();

    @Query("SELECT * FROM activities ORDER BY Id DESC LIMIT 2")
    List<DetectedActivities> getTwoLastActivities();
}
