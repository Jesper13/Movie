package com.example.android.movielist;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Jesper on 10/26/2015.
 */
public class Movie implements Parcelable{ //Implement Parcelable: used for passing arrayList of movie to fragment through bundle
    private long id;
    private String title;
    private String titleLong;
    private double rating;
    private List<String> genres;
    private String language;
    private String url;
    private String imdb_code;
    private int year;
    private int runtime;
    private String overview;
    private String slug;
    private String mpaRating;
    private String state;

    public Movie() {
    }

    public Movie(Parcel in){
        //Get items the same order as it is written
        this.title = in.readString();
        this.rating = in.readDouble();
        this.overview = in.readString();
        this.year = in.readInt();
        this.slug = in.readString();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleLong() {
        return titleLong;
    }

    public void setTitleLong(String titleLong) {
        this.titleLong = titleLong;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImdb_code() {
        return imdb_code;
    }

    public void setImdb_code(String imdb_code) {
        this.imdb_code = imdb_code;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getMpaRating() {
        return mpaRating;
    }

    public void setMpaRating(String mpaRating) {
        this.mpaRating = mpaRating;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //write instance variables that will be needed for displaying details of movie
        dest.writeString(this.title);
        dest.writeDouble(this.rating);
        dest.writeString(this.overview);
        dest.writeInt(this.year);
        dest.writeString(this.slug);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

}
