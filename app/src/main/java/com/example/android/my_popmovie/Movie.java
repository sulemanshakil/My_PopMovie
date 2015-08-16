package com.example.android.my_popmovie;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Lorenzo on 7/26/2015.
 */
public class Movie implements Serializable {
    private int id;
    private String title; // original_title
    private String image; // poster_path
    private String image2; // backdrop_path
    private String overview;
    private int rating; // vote_average
    private String date; // release_date

    public Movie(JSONObject movie) throws JSONException {
        this.id = movie.getInt("id");
        this.title = movie.getString("original_title");
        this.image = movie.getString("poster_path");
        this.image2 = movie.getString("backdrop_path");
        this.overview = movie.getString("overview");
        this.rating = movie.getInt("vote_average");
        this.date = movie.getString("release_date");
    }

    public Movie(Cursor movie)  {
        this.id = movie.getInt(0);
        this.title = movie.getString(1);
        this.image = movie.getString(2);
        this.image2 = movie.getString(3);
        this.overview = movie.getString(4);
        this.rating = movie.getInt(5);
        this.date = movie.getString(6);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getImage2() {
        return image2;
    }

    public String getOverview() {
        return overview;
    }

    public int getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

}
