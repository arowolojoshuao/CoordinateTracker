package com.ecosa.devicemovementtracker.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;


@Dao
public interface DaoAccess {

    @Insert(onConflict = REPLACE)
      void insertCoordinate(Coordinate coordinate);


    @Query("SELECT * FROM Coordinate")
     List<Coordinate> getAllCoordinates();

    @Query("SELECT * FROM Coordinate WHERE id =:coordinateId")
    LiveData<Coordinate> getCoordinateById(int coordinateId);


    @Update
    void updateCoordinate(Coordinate coordinate);


    @Delete
    void deleteCoordinate(Coordinate coordinate);
}