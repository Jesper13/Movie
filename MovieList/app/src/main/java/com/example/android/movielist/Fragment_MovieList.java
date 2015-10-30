package com.example.android.movielist;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.JsonReader;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment_MovieList extends Fragment {
    private List<Movie> movieList;
    private TextView listStatus;
    private ListView movieListView;
    private static CustomListAdapter movieAdapter;
    private OnFragmentInteractionListener fragmentInterface;
    private static final String MOVIE_LIST = "movieList";
    private static final String TAG = "Movie";
    private static final String MOVIE_LIST_SETTINGS = "movieListSettings";

    public Fragment_MovieList() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener {
        void updateDetailsView(Movie selectedMovie);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            fragmentInterface = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movielist, container, false);

        if (savedInstanceState != null) {
            movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST);
        }

        movieListView = (ListView) view.findViewById(R.id.list_movie);
        listStatus = (TextView) view.findViewById(R.id.list_status);

        //Check if list exist on the fragments bundle before loading list
        if (movieList != null) {
            movieAdapter = new CustomListAdapter(view.getContext(), R.layout.movie_listitem, movieList);
            movieListView.setAdapter(movieAdapter);
            movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    fragmentInterface.updateDetailsView((Movie) parent.getItemAtPosition(position));
                }
            });
            //Point the user to the last displayed item position
            if (savedInstanceState.getParcelable(MOVIE_LIST_SETTINGS) != null) {
                movieListView.onRestoreInstanceState(savedInstanceState.getParcelable(MOVIE_LIST_SETTINGS));
            }
        } else {
            new parseInBackground().execute();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save movieList to the fragment's bundle to reuse it the the fragment is re-instantiated (orientation change)
        outState.putParcelableArrayList(MOVIE_LIST, (ArrayList) movieList);
        //Save the current settings of the list to be able to restore the last visible item position
        outState.putParcelable(MOVIE_LIST_SETTINGS, movieListView.onSaveInstanceState());
    }

    private class parseInBackground extends AsyncTask<Void, Void, Boolean> {
        public parseInBackground() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            listStatus.setVisibility(View.VISIBLE);
            listStatus.setText("Loading Items...");
            movieList = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = false;

            try {
                URL jsonURL = new URL("https://dl.dropboxusercontent.com/u/5624850/movielist/list_movies_page1.json");
                InputStream jsonInputStream = jsonURL.openStream();

                JsonReader reader = new JsonReader(new InputStreamReader(jsonInputStream, "UTF-8"));

                //read JSON object
                reader.beginObject();

                while (reader.hasNext()) {
                    String currentName = reader.nextName();
                    if (currentName.equals("data")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            currentName = reader.nextName();
                            if (currentName.equals("movies")) {
                                populateMovieList(reader);
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                        result = true;
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if (aVoid == false) {
                listStatus.setText("Error retrieving data!");
            } else {
                listStatus.setVisibility(View.GONE);
                movieAdapter = new CustomListAdapter(getActivity().getBaseContext(), R.layout.movie_listitem, movieList);
                movieListView.setAdapter(movieAdapter);
                movieListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        fragmentInterface.updateDetailsView((Movie) parent.getItemAtPosition(position));
                    }
                });
            }
        }

        private void populateMovieList(JsonReader reader) throws IOException {
            reader.beginArray();

            while (reader.hasNext()) {
                movieList.add(getMovie(reader));
            }
            reader.endArray();
        }

        private Movie getMovie(JsonReader reader) throws IOException {
            Movie movie = new Movie();

            reader.beginObject();
            while (reader.hasNext()) {
                String currentName = reader.nextName();

                switch (currentName) {
                    case "rating":
                        movie.setRating(reader.nextDouble());
                        break;
                    case "genres":
                        movie.setGenres(getGenres(reader));
                        break;
                    case "language":
                        movie.setLanguage(reader.nextString());
                        break;
                    case "title":
                        movie.setTitle(reader.nextString());
                        break;
                    case "title_long":
                        movie.setTitleLong(reader.nextString());
                        break;
                    case "url":
                        movie.setUrl(reader.nextString());
                        break;
                    case "imdb_code":
                        movie.setImdb_code(reader.nextString());
                        break;
                    case "id":
                        movie.setId(reader.nextInt());
                        break;
                    case "state":
                        movie.setState(reader.nextString());
                        break;
                    case "year":
                        movie.setYear(reader.nextInt());
                        break;
                    case "runtime":
                        movie.setRuntime(reader.nextInt());
                        break;
                    case "overview":
                        movie.setOverview(reader.nextString());
                        break;
                    case "slug":
                        movie.setSlug(reader.nextString());
                        break;
                    case "mpa_rating":
                        movie.setMpaRating(reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();

            return movie;
        }

        public List<String> getGenres(JsonReader reader) throws IOException {
            List<String> genreList = new ArrayList<>();
            reader.beginArray();

            while (reader.hasNext()) {
                genreList.add(reader.nextString());
            }
            reader.endArray();

            return genreList;
        }
    }

}
