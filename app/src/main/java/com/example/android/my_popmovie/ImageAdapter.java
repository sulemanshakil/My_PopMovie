package com.example.android.my_popmovie;

import android.graphics.Color;
import android.util.Log;
import android.widget.ListAdapter;

/**
 * Created by Lorenzo on 7/26/2015.
 */
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;

    // Keep all Images in array
    public List<Movie> movies;
    // Constructor

    public void setData(List<Movie> movies){
        this.movies=movies;
    }

    public Movie getMovieItem(int position){
        return movies.get(position);
    }

    public ImageAdapter(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
       if(movies==null){
           return 0;
       }else {
        return movies.size();
       }
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String WEB_PATH ="http://image.tmdb.org/t/p/w342/";
        String Path = new StringBuilder(WEB_PATH).append(movies.get(position).getImage()).toString();
        ImageView imageView = new ImageView(mContext);
        Picasso.with(mContext).load(Path).into(imageView);
        return imageView;
    }

}