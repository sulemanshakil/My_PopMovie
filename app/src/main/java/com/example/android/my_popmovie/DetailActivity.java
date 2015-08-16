package com.example.android.my_popmovie;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

/**
 * Created by Lorenzo on 7/29/2015.
 */
public class DetailActivity extends ActionBarActivity {
    TextView textViewTitle,textViewYear,textViewMin,textViewRating,textViewDes;
    ImageView imageView;
    ListView listViewTrailer;
    ListView listViewReview;
    Movie movie;
    TrailerAdapter trailerAdapter;
    ListViewReviewAdapter listViewReviewAdapter;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        movie=(Movie)getIntent().getSerializableExtra("MyMovie");
        textViewTitle=(TextView)findViewById(R.id.textViewTl);
        textViewYear = (TextView)findViewById(R.id.textViewYear);
        textViewMin=(TextView) findViewById(R.id.textViewMin);
        textViewRating=(TextView)findViewById(R.id.textViewRating);
        textViewDes=(TextView)findViewById(R.id.textViewDes);
        listViewTrailer=(ListView)findViewById(R.id.listViewTr);
        listViewReview=(ListView)findViewById(R.id.listViewReview);
        scrollView=(ScrollView)findViewById(R.id.scrollView);

        imageView = (ImageView) findViewById(R.id.imageView);
        final String WEB_PATH ="http://image.tmdb.org/t/p/w342/";
        String Path = new StringBuilder(WEB_PATH).append(movie.getImage()).toString();
        Picasso.with(this).load(Path).into(imageView);

        textViewTitle.setText(movie.getTitle());
        String string = movie.getDate().substring(0,4);
        textViewYear.setText(string);
        textViewRating.setText(movie.getRating() + "/10");
        textViewDes.setText(movie.getOverview());


        trailerAdapter=new TrailerAdapter(this, new ArrayList<Trailer>());
        listViewTrailer.setAdapter(trailerAdapter);
        setListViewHeightBasedOnChildren(listViewTrailer);

        listViewReviewAdapter=new ListViewReviewAdapter(this,new ArrayList<Review>());
        listViewReview.setAdapter(listViewReviewAdapter);
        setListViewHeightBasedOnChildren(listViewReview);

    }

    private void setListViewHeightBasedOnChildren(ListView  listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() -1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        new FetchReviewsTask().execute(Integer.toString(movie.getId()));
        new FetchTrailersTask().execute(Integer.toString(movie.getId()));
    }

    public class addFavMovieTask extends AsyncTask<Movie,Void,Void>{

        @Override
        protected Void doInBackground(Movie... params) {
            Movie mMovie = params[0];

            List<String> favouriteMovies= getfavouriteMovie();
            if(!favouriteMovies.contains(movie.getTitle())){
                // insert into favourite movie database
                MovieDbHelper movieDbHelper = new MovieDbHelper(getApplicationContext());
                SQLiteDatabase db = movieDbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                ContentValues values = new ContentValues();
                values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
                values.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
                values.put(MovieContract.MovieEntry.COLUMN_IMAGE,mMovie.getImage() );
                values.put(MovieContract.MovieEntry.COLUMN_IMAGE2,mMovie.getImage2() );
                values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,mMovie.getOverview());
                values.put(MovieContract.MovieEntry.COLUMN_RATING, mMovie.getRating());
                values.put(MovieContract.MovieEntry.COLUMN_DATE, mMovie.getDate());
                db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
            }
            return null;
        }
    }

    public void addFavouriteMovie(View view){
        new addFavMovieTask().execute(movie);
    }


    public List<String> getfavouriteMovie(){
        MovieDbHelper movieDbHelper = new MovieDbHelper(this);
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
        String sortOrder =
                MovieContract.MovieEntry.COLUMN_TITLE+ " DESC";

        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);

        List<String> movieNames= new ArrayList<String>();
        List<Movie> moviesFavList= new ArrayList<>() ;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                    Movie movie = new Movie(cursor);
                    moviesFavList.add(movie);
                Log.i("Message", cursor.getString(1) );
                movieNames.add(cursor.getString(1));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return movieNames;
        // Log.i("Message","Show fav items");
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>>{

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Review> results = new ArrayList<>();

            for(int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                results.add(new Review(review));
                Log.i("mycount ",""+i);
            }
            return results;
        }

        @Override
        protected List<Review> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
               // Log.i("message",jsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    if (listViewReviewAdapter != null) {
                        listViewReviewAdapter.clear();
                        for (Review review : reviews) {
                            listViewReviewAdapter.add(review);
                        }
                    }
                }
            }
            setListViewHeightBasedOnChildren(listViewReview);        }
    }

    private class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for(int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                // Only show Trailers which are on Youtube
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Trailer trailerModel = new Trailer(trailer);
                    results.add(trailerModel);
                }
            }
            return results;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                Log.i(LOG_TAG,jsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            trailerAdapter.add(trailer);
                        }
                    }
                }
            }
            setListViewHeightBasedOnChildren(listViewTrailer);
        }
    }
}
