package com.ecosa.devicemovementtracker.database;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class RoomRepository {

    private DaoAccess myDao;
    private Context context;
    public static AppDatabase myDatabase;
    private List<Coordinate> coordinates;


    public RoomRepository(Application application){
        AppDatabase database = AppDatabase.getInstance(application);
        myDao = database.daoAccess();
        coordinates = myDao.getAllCoordinates();


    }

    public void addCoordinate(Coordinate coordinate){

        new addCoordinateAsyncTask(myDao).execute(coordinate);
    }


    public List<Coordinate> getAllCoordinates(){
        return coordinates;
    }

    private static class addCoordinateAsyncTask extends AsyncTask<Coordinate, Void, Void> {

        private DaoAccess myDao;

        private addCoordinateAsyncTask(DaoAccess myDao){
            this.myDao = myDao;
        }

        @Override
        protected Void doInBackground(Coordinate... coordinates) {
            myDao.insertCoordinate(coordinates[0]);
            return null;
        }
    }
}

