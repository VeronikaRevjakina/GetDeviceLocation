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
