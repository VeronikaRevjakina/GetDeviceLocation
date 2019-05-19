package com.test.getdevicelocation2;

import com.test.getdevicelocation2.ValuesMET;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Delete;
import android.renderscript.Sampler;

import java.util.List;

@Dao
public interface ValuesMETDao {
    @Query("SELECT * FROM values_MET WHERE activity_id=:activity_id")
    ValuesMET getValueMETById(int activity_id);

    @Query("SELECT * FROM values_MET")
    List<ValuesMET> getAllValues();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertMETValue(ValuesMET value);

    @Insert()
    void insertAllMETValues(ValuesMET... values);

    @Delete
    void deleteMETValue (ValuesMET value);
}
