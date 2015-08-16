package com.example.android.my_popmovie;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    private static final String POPULARITY_DESC = "popularity.desc";
    private static final String RATING_DESC = "vote_average.desc";

    ImageAdapter imageAdapter;
    GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = (Movie) imageAdapter.getMovieItem(position);
                Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                intent.putExtra("MyMovie",movie);
                MainActivity.this.startActivity(intent);
                Log.i("Movie name",movie.getTitle());
            }
        });

       fetchMovies(POPULARITY_DESC);
    }

    public void fetchMovies(String mSortBy){
        new FetchMoviesTask().execute(mSortBy);
    }

    public class FetchMoviesTask extends AsyncTask <String, Void, List<Movie>>{
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        private List<Movie> getMoviesDataFromJson(String jsonStr) throws JSONException {
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray("results");

            List<Movie> results = new ArrayList<>();

            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                Movie movieModel = new Movie(movie);
                results.add(movieModel);
             //   Log.i("poster_path",results.get(i)+"");
            }

            return results;
        }


        @Override
        protected List<Movie> doInBackground(String... params) {

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.key))
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();
                Log.i("mes",jsonStr);

            }catch (IOException e) {
                Log.e(LOG_TAG, "Error ",e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream",e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            imageAdapter.setData(movies);
            imageAdapter.notifyDataSetChanged();
            super.onPostExecute(movies);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.popularity_settings) {
            fetchMovies(POPULARITY_DESC);
            return true;
        }
        if (id == R.id.rating_setting) {
            fetchMovies(RATING_DESC);
            return true;
        }
        if (id == R.id.favourite_setting) {
            new fetchDataFav().execute("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class fetchDataFav extends AsyncTask<String,Void,List<Movie>>{

        @Override
        protected List<Movie> doInBackground(String... params) {
            MovieDbHelper movieDbHelper = new MovieDbHelper(getApplicationContext());
            SQLiteDatabase db = movieDbHelper.getWritableDatabase();

            String[] projection = {
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                    MovieContract.MovieEntry.COLUMN_TITLE,
                    MovieContract.MovieEntry.COLUMN_IMAGE,
                    MovieContract.MovieEntry.COLUMN_IMAGE2,
                    MovieContract.MovieEntry.COLUMN_OVERVIEW,
                    MovieContract.MovieEntry.COLUMN_RATING,
                    MovieContract.MovieEntry.COLUMN_DATE,
            };

            // How you want the results sorted in the resulting Cursor
            String sortOrder = MovieContract.MovieEntry.COLUMN_TITLE+ " DESC";

            Cursor cursor = db.query(
                    MovieContract.MovieEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

            List<String> movieNames= new ArrayList<String>();
            List<Movie> moviesFavList= new ArrayList<>() ;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie(cursor);
                    moviesFavList.add(movie);
                  //  Log.i("Message", cursor.getString(1) );
                    movieNames.add(cursor.getString(1));
                } while (cursor.moveToNext());
                cursor.close();
            }
            return moviesFavList;
            // Log.i("Message","Show fav items");
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            imageAdapter.setData(movies);
            imageAdapter.notifyDataSetChanged();
        }
    }
}
