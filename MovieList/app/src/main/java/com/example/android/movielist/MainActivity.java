package com.example.android.movielist;

import android.content.res.Configuration;
import android.graphics.Point;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements Fragment_MovieList.OnFragmentInteractionListener {
    private FrameLayout listContainer;
    private FrameLayout detailsContainer;
    private static final String DETAILS_FOCUSED_KEY = "detailsFocusedKey";

    /**************************
     * IMPLEMENT METHODS
     ***************************************/
    @Override
    public void updateDetailsView(Movie selectedMovie) {
        if(detailsContainer.getVisibility() == View.GONE){
            detailsContainer.setVisibility(View.VISIBLE);
            detailsContainer.getLayoutParams().width = getScreenBounds().x;
            listContainer.setVisibility(View.GONE);
            showHomeButton(true);
        }

        Fragment_Details fragment_details = new Fragment_Details();
        Bundle detailsBundle = new Bundle();
        detailsBundle.putParcelable("objSelectedMovie", selectedMovie);
        fragment_details.setArguments(detailsBundle);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.abc_grow_fade_in_from_bottom, R.anim.abc_popup_exit);
        fragmentTransaction.replace(R.id.details_container, fragment_details).commit();
    }

    /***********************
     * END IMPLEMENT METHODS
     **************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the screen metrics
        Point point = getScreenBounds();

        //Check if landscape orientation && valid screen size, then apply adjustments to accommodate overflow menu (default setting is for single view list)
        listContainer = (FrameLayout) findViewById(R.id.list_container);
        detailsContainer = (FrameLayout) findViewById(R.id.details_container);
        if (point.x > point.y) { //Landscape
            //Check if overflow menu is applicable to the current screen size before applying layout
            if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                    (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
                //Apply Overflow menu
                listContainer.getLayoutParams().width = getPixels(320);
                detailsContainer.setVisibility(View.VISIBLE);
            } else { //If small/medium device in landscape orientation
                if(savedInstanceState == null){ //First run
                    listContainer.setVisibility(View.GONE);
                    detailsContainer.setVisibility(View.VISIBLE);
                    detailsContainer.getLayoutParams().width = point.x; //fill the entire width of the screen
                }else{
                    //Check for the last focused view (Details/MovieList) before maximizing the view
                    if(savedInstanceState.getBoolean(DETAILS_FOCUSED_KEY) == true){
                        maximizeDetailsView(point);
                    }else{
                        maximizeMovieListView(point);
                    }
                }
            }
        } else { //Portrait
            /*Check if first time run before loading the fragment
            *(reloading the fragment will cause the fragment's onCreateView method to be called twice)
            */
            if (savedInstanceState == null) {
                Fragment_MovieList movieList = new Fragment_MovieList();
                getSupportFragmentManager().beginTransaction().replace(R.id.list_container, movieList).commit();
            } else {
                //Check if details view is the last focused item
                if (savedInstanceState.getBoolean(DETAILS_FOCUSED_KEY) == true) {
                    maximizeDetailsView(point);
                } else {
                    maximizeMovieListView(point);
                }
            }

        }
    }

    /*Maximize details view and hide movie list view
        *Used for the following scenario:
        * Small/Medium device with focus on details view
        * Large/Xlarge device on portrait orientation with focus on details view
         */
    public void maximizeDetailsView(Point screenBounds){
        showHomeButton(true);
        listContainer.setVisibility(View.GONE);
        detailsContainer.setVisibility(View.VISIBLE);
        detailsContainer.getLayoutParams().width = screenBounds.x; //fill the entire width of the screen
    }

    /*Maximize movie list view and hide details view
    *Used for the following scenario:
    * Small/Medium device with focus on movie list view
    * Large/Xlarge device on portrait orientation with focus on movie list view
     */
    public void maximizeMovieListView(Point screenBounds){
        showHomeButton(false);
        detailsContainer.setVisibility(View.GONE);
        listContainer.setVisibility(View.VISIBLE);
        listContainer.getLayoutParams().width = screenBounds.x; //fill the entire width of the screen
    }

    public void showHomeButton(Boolean showHome) {
        getSupportActionBar().setDisplayShowHomeEnabled(showHome);
        getSupportActionBar().setDisplayHomeAsUpEnabled(showHome);
        if(showHome == true){
            getSupportActionBar().setTitle("");
        }else{
            getSupportActionBar().setTitle("Movie List");
        }
    }

    public Point getScreenBounds() {
        //Get the screen metrics
        WindowManager wm = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        return point;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        /*Check if the user is currently on the details view then mimic a transition of child activity to parent activity
        *(App uses single activity for 'single view movie list', 'single view details', 'overflow menu'
         */
        if (detailsContainer.getVisibility() == View.VISIBLE && listContainer.getVisibility() == View.GONE) {
            showHomeButton(false);
            detailsContainer.setVisibility(View.GONE);
            listContainer.setVisibility(View.VISIBLE);
            listContainer.getLayoutParams().width = getScreenBounds().x; //fill the entire width of the screen
        } else { //If the user is currently on the home screen (movie list/overflow menu) execute onBackPressed event
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(DETAILS_FOCUSED_KEY, (detailsContainer.getVisibility() == View.VISIBLE));
        super.onSaveInstanceState(outState);
    }

    public int getPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.getResources().getDisplayMetrics());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
