package com.example.android.my_popmovie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lorenzo on 8/2/2015.
 */
public class TrailerAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final Trailer mLock = new Trailer();
    public List<Trailer> trailers;

    public TrailerAdapter(Context context,List<Trailer> mObects) {
        mContext = context;
        trailers=mObects;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Context getContext() {
        return mContext;
    }

    public void add(Trailer object) {
        synchronized (mLock) {
            trailers.add(object);
        }
    }

    public void clear() {
        synchronized (mLock) {
            trailers.clear();
        }
    }

    @Override
    public int getCount() {
        return trailers.size();
    }

    @Override
    public Object getItem(int position) {
        return trailers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Trailer trailer = trailers.get(position);
        ViewHolder viewHolder;

        if(view==null){
            view = mInflater.inflate(R.layout.item_movie_trailer, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) view.getTag();
        viewHolder.nameView.setText(trailer.getName());

        return view;
    }

    private class ViewHolder {
        TextView nameView;
        public ViewHolder(View view) {
            nameView= (TextView)view.findViewById(R.id.trailer_name);
        }
    }
}
