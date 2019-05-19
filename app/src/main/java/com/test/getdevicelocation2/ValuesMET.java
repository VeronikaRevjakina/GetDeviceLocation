package com.test.getdevicelocation2;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName ="values_MET")
public class ValuesMET {

    @PrimaryKey(autoGenerate = true)
    private int id;  //id of an Activity 7-WALKING 3-STILL 1-ON_BICYCLE 8-RUNNING

    @ColumnInfo(name = "activity_id")
    @NonNull
    private int activity_id;

    @ColumnInfo(name = "valueMET")

    /* @param valueMET is standard value per minute amount of wasted calories which should be multiplied by
    RMR of user and amount of minutes, it was counted by professionals and there is tables of values for it */

    private double valueMET;

    public ValuesMET( int activity_id,  double valueMET){
        this.activity_id=activity_id;
        this.valueMET=valueMET;
    }

    @NonNull
    public int getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(@NonNull int activity_id) {
        this.activity_id = activity_id;
    }

    public void setValueMET(double valueMET) {
        this.valueMET = valueMET;
    }

    public void setId( int id) {

        this.id = id;
    }


    public int getId() {
        return id;
    }

    //Do not need method to change id as it constant and dont need setValueMET ,because its
    // only possible to add both variables

    /*public void setId(@NonNull int id) {
        this.id = id;
    }*/

    public double getValueMET(){
        return valueMET;
    }




}
