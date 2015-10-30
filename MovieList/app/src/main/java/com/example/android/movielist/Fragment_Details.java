package com.example.android.movielist;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_Details extends Fragment {
    private Movie movie;
    private Bitmap coverBmp;
    private Bitmap backDropBmp;
    private static final String KEY_BACKDROP = "backDropBmp";
    private static final String KEY_COVER = "coverBmp";
    private ImageView cover; //Layout metrics of this ImageView will be updated on 'onResume' event based on the layoutParam of the fragments view container

    public Fragment_Details() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        movie = getArguments().getParcelable("objSelectedMovie");
        View v = inflater.inflate(R.layout.movie_details, container, false);

        TextView title = (TextView) v.findViewById(R.id.details_title);
        TextView year = (TextView) v.findViewById(R.id.details_year);
        TextView rating = (TextView) v.findViewById(R.id.details_rating);
        TextView overview = (TextView) v.findViewById(R.id.details_overview);
        ImageView backdrop = (ImageView) v.findViewById(R.id.details_backdrop);
        cover = (ImageView) v.findViewById(R.id.details_cover);
        title.setText(movie.getTitle());
        year.setText(year.getText().toString() + Integer.toString(movie.getYear()));
        rating.setText(rating.getText().toString() + Double.toString(movie.getRating()));
        overview.setText(movie.getOverview());


        if (savedInstanceState != null) {
            coverBmp = savedInstanceState.getParcelable(KEY_COVER);
            backDropBmp = savedInstanceState.getParcelable(KEY_BACKDROP);
        }

        //Check if bitmap is already stored on bundle before assigning it to imageView (cover)
        if (coverBmp != null) {
            cover.setImageBitmap(coverBmp);
            cover.setAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.abc_fade_in));
        } else {
            String coverUrl = "https://dl.dropboxusercontent.com/u/5624850/movielist/images/" + movie.getSlug() + "-cover.jpg";
            new loadImage(cover, coverUrl).execute();
        }

        //Check if bitmap is already stored on bundle before assigning it to imageView (backdrop)
        if (backDropBmp != null) {
            backdrop.setImageBitmap(backDropBmp);
            backdrop.setAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.abc_fade_in));
        } else {
            String backDropUrl = "https://dl.dropboxusercontent.com/u/5624850/movielist/images/" + movie.getSlug() + "-backdrop.jpg";
            new loadImage(backdrop, backDropUrl).execute();
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final View containerView = getView();
        containerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (v.getWidth() > 0) {
                    adjustLayoutBasedOnContainerMetrics(v);
                    v.removeOnLayoutChangeListener(this);
                }
            }
        });
    }

    public void adjustLayoutBasedOnContainerMetrics(View containerView) {
        int thumbWidth;
        int thumbHeight;
        RelativeLayout upperSection = (RelativeLayout) containerView.findViewById(R.id.layout_details_upper);

        //Adjust upper section layout base on the container's orientation
        if(containerView.getWidth() > containerView.getHeight()) {
            upperSection.getLayoutParams().height = (containerView.getWidth() / 2);
        }else{
            upperSection.getLayoutParams().height = (int)(containerView.getWidth() / 1.5);
        }

        //Adjust thumbnail size base on the container's orientation
        if (containerView.getWidth() > containerView.getHeight()) {//Check if the container is in landscape view
            thumbHeight = upperSection.getLayoutParams().height / 2;
        } else {
            thumbHeight = (int) (upperSection.getLayoutParams().height / 1.5);
        }

        thumbWidth = (int) (thumbHeight / 1.5);
        cover.getLayoutParams().height = thumbHeight;
        cover.getLayoutParams().width = thumbWidth;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Store bitmap to values to bundle to eliminate reloading it from network when orientation changes
        outState.putParcelable(KEY_BACKDROP, backDropBmp);
        outState.putParcelable(KEY_COVER, coverBmp);
    }

    private class loadImage extends AsyncTask<Void, Void, Boolean> {
        private ImageView imageView;
        private Bitmap imageBitmap;
        private String url;

        public loadImage(ImageView imageView, String url) {
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;
            try {
                URL imageURL = new URL(url);
                imageBitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
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
            if (result == true && imageView != null) {
                imageView.setBackgroundColor(Color.parseColor("#9A000000"));
                imageView.setImageBitmap(imageBitmap);
                imageView.setAnimation(AnimationUtils.loadAnimation(imageView.getContext(), R.anim.abc_fade_in));
                //Assign current bitmap to its respective bitmap placeholder (to be add on the fragment's bundle onSaveInstanceState)
                if (imageView.getId() == R.id.details_cover) {
                    coverBmp = imageBitmap;
                } else {
                    backDropBmp = imageBitmap;
                }
            }

            if(imageView != null) {
                //Display text (Title, year, rating) after layout resize
                LinearLayout headerContainer = (LinearLayout) imageView.getRootView().findViewById(R.id.details_heading_container);
                headerContainer.setVisibility(View.VISIBLE);
                headerContainer.setAnimation(AnimationUtils.loadAnimation(imageView.getContext(), R.anim.abc_fade_in));
            }
        }
    }

}
