package com.example.android.movielist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesper on 10/26/2015.
 */
public class CustomListAdapter extends ArrayAdapter<Movie> {
    private ViewHolder myViewHolder;
    private final int maxMemory = (int) (Runtime.getRuntime().maxMemory()); //Get Max Byte the application allocation
    private final int cacheSize = maxMemory / 7; //Assign 1/7 of the max byte(application) as the max memory for the LruCache
    private LruCache<String, Bitmap> backDropImageMemoryCache;
    private List<String> downloadQueue;

    public CustomListAdapter(Context context, int resource, List<Movie> objects) {
        super(context, resource, objects);
        this.backDropImageMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
        this.downloadQueue = new ArrayList<>();
    }

    //Use ViewHolder to save reference of view items (smooth scrolling)
    private static class ViewHolder {
        private TextView title;
        private TextView year;
        private ImageView backDrop;
        private ProgressBar backDropProgress;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.movie_listitem, parent, false);

            myViewHolder = new ViewHolder();
            myViewHolder.title = (TextView) convertView.findViewById(R.id.list_title);
            myViewHolder.year = (TextView) convertView.findViewById(R.id.list_year);
            myViewHolder.backDrop = (ImageView) convertView.findViewById(R.id.list_backdrop);
            myViewHolder.backDropProgress = (ProgressBar) convertView.findViewById(R.id.list_loading);

            convertView.setTag(myViewHolder);
        } else {
            myViewHolder = (ViewHolder) convertView.getTag();
        }

        myViewHolder.title.setText(getItem(position).getTitle());
        myViewHolder.title.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in));
        myViewHolder.year.setText(Integer.toString(getItem(position).getYear()));
        myViewHolder.year.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in));

        /*Update the tag of the current imageView to inform the current running task associated with this ImageView
        *that the imageView has been recycled, thus omitting any UI update from the currently running task
         */
        myViewHolder.backDrop.setTag(position);
        myViewHolder.backDrop.setImageBitmap(null);
        myViewHolder.backDropProgress.setVisibility(View.GONE);

        //This method  would either load bitmap stored on cache or download bitmap from the server
        loadBitmap(myViewHolder.backDrop, myViewHolder.backDropProgress, getItem(position).getSlug());

        return convertView;
    }


    //Load Image on a separate thread (smooth scrolling)
    private class LoadImage extends AsyncTask<Void, Void, Boolean> {
        private final String slug;
        private Bitmap backDropBmp;
        private final ImageView backDropView;
        private final Object preBackDropTag;
        private final ProgressBar backDropLoading;

        public LoadImage(ImageView backDrop, String slug) {
            this.backDropView = backDrop;
            this.slug = slug;
            this.preBackDropTag = backDrop.getTag();
            backDropLoading = (ProgressBar) backDrop.getRootView().findViewById(R.id.list_loading);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            downloadQueue.add(slug);
            backDropLoading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                URL imageURL = new URL("https://dl.dropboxusercontent.com/u/5624850/movielist/images/" + slug + "-backdrop.jpg");
                backDropBmp = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                addBitmapToBackDropMemoryCache(slug, backDropBmp);
                result = true;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            downloadQueue.remove(slug);

            if (result == true) {
                /*Compare the initial tag of the backDrop to its current tag before assigning the downloaded bitmap (must be equal)
                *-If the initial tag is not equal current tag then it means the current imageView is already recycled
                 */
                if (preBackDropTag == backDropView.getTag()) {
                    backDropView.setImageBitmap(backDropBmp);
                    backDropLoading.setVisibility(View.GONE);
                }
                backDropBmp = null;
            }
        }

    }

    public void addBitmapToBackDropMemoryCache(String key, Bitmap bitmap) {
        if (backDropImageMemoryCache.get(key) == null) { //If image is not yet stored on the cache
            backDropImageMemoryCache.put(key, bitmap);
        }
    }

    public void loadBitmap(ImageView backDropView, ProgressBar bar, String slug) {
        final Bitmap backDropBitmap = backDropImageMemoryCache.get(slug);

        if (backDropBitmap != null) { //Check if the image already stored on the cache
            backDropView.setImageBitmap(backDropBitmap);
            backDropView.setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.abc_fade_in));
        } else {//If not yet stored on the cache, call LoadImage AsyncTask to download the image, update the backDropMemoryCache, and run loadBitmap
            if(!downloadQueue.contains(slug)) {
                new LoadImage(backDropView, slug).execute();
            }else{
                bar.setVisibility(View.VISIBLE);
            }
        }
    }

}
