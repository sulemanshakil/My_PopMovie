package com.example.android.my_popmovie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Lorenzo on 8/2/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = MovieDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME= "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_IMAGE + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_IMAGE2 + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_RATING + " INTEGER, " +
                MovieContract.MovieEntry.COLUMN_DATE + " TEXT);";

        Log.i(LOG_TAG,SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_MOVIE_TABLE );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
