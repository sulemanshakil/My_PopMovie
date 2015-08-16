package com.example.android.my_popmovie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lorenzo on 8/15/2015.
 */
public class ListViewReviewAdapter extends BaseAdapter {

    private final Context mContext;
    private final LayoutInflater mInflater;
    public List<Review> reviews;
    private final Review mLock = new Review();

    public ListViewReviewAdapter(Context context, ArrayList<Review> reviews) {
        this.mContext = context;
        this.reviews=reviews;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public void add(Review object) {
        synchronized (mLock) {
            reviews.add(object);
        }
    }

    public void clear() {
        synchronized (mLock) {
            reviews.clear();
        }
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final Review review = reviews.get(position);
        ViewHolder viewHolder;

        if(view==null){
            view = mInflater.inflate(R.layout.item_review, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) view.getTag();
        viewHolder.contentView.setText(review.getContent());
        viewHolder.authorView.setText("Author: "+review.getAuthor());
        return view;
    }

    private class ViewHolder {
        TextView contentView,authorView;
        public ViewHolder(View view) {
            contentView= (TextView)view.findViewById(R.id.textViewContent);
            authorView=(TextView)view.findViewById(R.id.textViewAuthor);
        }
    }
}
