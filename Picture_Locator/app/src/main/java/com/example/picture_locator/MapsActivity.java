package com.example.picture_locator;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MarkerOptions mMarker;

    private LatLng mGoalPosition;
    private MenuItem mAnswerButton;

    private static final String KEY_MAP_SCORE = "map_score";
    public static final String EXTRA_MAP_SCORE = "map_score";

    boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//      Display the back button on the App bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Extras
        Bundle extras = getIntent().getExtras();

        // Get the goal position from extras
        if (extras != null) {
            double lat = extras.getDouble(getString(R.string.key_latitude), 0.0);
            double longit = extras.getDouble(getString(R.string.key_longitude), 0.0);

            mGoalPosition = new LatLng(lat, longit);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!answered){
                    mMap.clear();
                    mMarker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED));
                    mMap.addMarker(mMarker);
                }
            }
        });

        // Add a marker in Dartmouth and move the camera
        LatLng dartmouth = new LatLng(43.7033, -72.2885);

//      mGoalPosition=dartmouth;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dartmouth, (float)16.5));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);

        mAnswerButton = menu.findItem(R.id.menu_map_answer);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_map_answer:
                if (answered){
                    finish();
                }else if (mMarker != null){
                    LatLng answer = mMarker.getPosition();
                    computeScore(answer);
                }
                return true;
            default:
                return  true;
        }
    }

    private void computeScore(LatLng answer) {
        // Mark the correct answer on Map
        mMap.addMarker(new MarkerOptions().position(mGoalPosition).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));


        // Convert LatLng of Guess and Goal to Location
        Location guess = new Location("");
        guess.setLatitude(answer.latitude);
        guess.setLongitude(answer.longitude);

        Location goal = new Location("");
        goal.setLatitude(mGoalPosition.latitude);
        goal.setLongitude(mGoalPosition.longitude);

        // Compute distance between guess and goal
        double distance = guess.distanceTo(goal);

        // Score based on distance
        int score = 0;
        if (distance < 20){
            score = 1000;
        }
        else if (distance < 80){
            score = 850;
        }
        else if (distance < 200){
            score = 400;
        }
        else if (distance < 500){
            score = 200;
        }

        Toast.makeText(this, "Score: "+score, Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        data.putExtra(EXTRA_MAP_SCORE, score);
        setResult(RESULT_OK, data);

        answered = true;
        mAnswerButton.setTitle("RETURN");
    }

}
