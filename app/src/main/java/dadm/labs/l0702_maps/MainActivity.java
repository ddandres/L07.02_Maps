/*
 * Copyright (c) 2019. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package dadm.labs.l0702_maps;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

// Displays the location of the SDM lab as a marker on Google Maps within a SupportMapFragment.
// New markers can be added through their longitudes and latitudes.
// Human readable addresses are obtained and displayed in a custom InfoWindow when markers are clicked.
// If the InfoWindow is clicked, then the route from DADM lab to that location is obtained through
// the Goggle Maps Directions API, and it is displayed on the map using the Maps Util library.
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    // Hold reference to the GoogleMap used to display location information
    GoogleMap map;
    // Hold reference to the route being displayed on the map
    Polyline route = null;

    // Hold reference to a custom adapter to display information associated to each marker
    MyInfoWindowAdapter infoWindowAdapter;

    // Hold reference to Views
    EditText etLongitude;
    EditText etLatitude;
    Button bAddMarker;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep references to View elements
        etLongitude = findViewById(R.id.etLongitude);
        etLatitude = findViewById(R.id.etLatitude);
        bAddMarker = findViewById(R.id.bAddMarker);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.bAddMarker).setOnClickListener(v -> translateCoordinates());

        // Keep a reference to the fragment displaying the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fcvMap);
        // Wait for the map to be ready before adding any marker or route
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        // Adapter for the custom InfoWindow
        infoWindowAdapter = new MyInfoWindowAdapter();
    }

    // This method is called whenever the button to add a marker on the map is clicked.
    // It launches an AsyncTask to translate longitude and latitude into a human readable address.
    // view View that generated the event (useless in this case)
    private void translateCoordinates() {

        double lat, lng;

        lat = Double.parseDouble(etLatitude.getText().toString());
        lng = Double.parseDouble(etLongitude.getText().toString());

        // Check that the coordinates are valid
        if ((lat >= -90.0f) && (lat <= 90.0f) && (lng >= -180.0f) && (lng <= 180.0f)) {
            // Check that the Internet connection is available
            if (isConnectionAvailable()) {
                // Use a Geocoder in a background task
                new GeocoderThread(this, lat, lng).start();
            }
        }
        // Notify the user that coordinates are not valid
        else {
            Toast.makeText(MainActivity.this, R.string.invalid_coordinates, Toast.LENGTH_SHORT).show();
        }
    }

    // This method adds a new marker to the map.
    // latitude  Latitude where the marker should be located.
    // longitude Longitude where the marker should be located.
    // title     Title of the InfoWindow associated to the marker.
    // snippet   Description to be displayed in the associated InfoWindow.
    // color     Color of the marker on the map.
    private void addMarker(double latitude, double longitude, String title, String snippet, float color) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(latitude, longitude));
        options.title(title);
        options.snippet(snippet);
        options.icon(BitmapDescriptorFactory.defaultMarker(color));

        map.addMarker(options);
    }

    // This method is executed when the activity is created to populate the ActionBar with actions.
    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {

        // Add the menu to the ActionBar only if the map has been initialized.
        if (map != null) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    // This method is executed when any action from the ActionBar is selected.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Determine the action to take place according to the Id of the action selected
        final int selectedItem = item.getItemId();
        if (selectedItem == R.id.mNormalMap) {
            // Display the GoogleMap using in the regular mode
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (selectedItem == R.id.mTerrainMap) {
            // Display the GoogleMap using in terrain mode
            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (selectedItem == R.id.mSatelliteMap) {
            // Display the GoogleMap using in satellite mode
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        return true;
    }

    // Check whether Internet connectivity is available.
    private boolean isConnectionAvailable() {

        // Get a reference to the ConnectivityManager
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get information for the current default data network
        NetworkInfo info = manager.getActiveNetworkInfo();
        // Return true if there is network connectivity
        return ((info != null) && info.isConnected());
    }

    // This method is called whenever the Google;ap is ready to be used.
    // googleMap The initialized GoogleMap object.
    // You should keep a reference to this object to manipulate the map,
    // add markers to it, and so on.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        // Keep a reference to the initialized GoogleMap object
        map = googleMap;

        // Move the camera to the location of the SDM lab using a zoom level of 10
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.482463, -0.346415), 10));
        // Associate a custom InfoWindowAdapter to the map
        map.setInfoWindowAdapter(infoWindowAdapter);
        // This activity will be in charge of managing click events on InfoWindows
        map.setOnInfoWindowClickListener(this);

        // Add a green marker on the location of the SDM lab, including related information
        addMarker(39.482463, -0.346415, getResources().getString(R.string.lab_dadm_title),
                getResources().getString(R.string.lab_dadm_snippet), BitmapDescriptorFactory.HUE_GREEN);

        // Display options in the Actionbar
        supportInvalidateOptionsMenu();
        // Display the button to add new markers on the map
        bAddMarker.setVisibility(View.VISIBLE);
    }

    // This method will be called whenever an InfoWindow is clicked.
    // marker Marker that was clicked.
    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {

        // Check whether the Internet connection is available
        if (isConnectionAvailable()) {
            // Obtain the route between the SDM lab and this marker using an AsyncTask
            new RouteThread(this, marker.getPosition().latitude, marker.getPosition().longitude).start();
        }
    }

    // Custom InfoWindowAdapter to display the name of the place represented by the marker,
    // its address, and its location coordinates.
    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            // Inflate a View from the XML file representing the contents of the InfoWindow
            View result = getLayoutInflater().inflate(R.layout.custom_info_window, null, false);
            // Get a reference to the View objects in charge of displaying
            // the title, snippet and coordinates of the marker
            TextView tvTitle = result.findViewById(R.id.tvTitle);
            TextView tvSnippet = result.findViewById(R.id.tvSnippet);
            TextView tvCoordinates = result.findViewById(R.id.tvCoordinates);
            // Set the name of the place as title
            tvTitle.setText(marker.getTitle());
            // Set the address as description
            tvSnippet.setText(marker.getSnippet());
            // Set the coordinates
            tvCoordinates.setText(
                    String.format(
                            getResources().getString(R.string.info_window_coordinates),
                            marker.getPosition().latitude,
                            marker.getPosition().longitude));

            return result;
        }
    }


    public void displayProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void displayRoute(List<LatLng> result) {
        // Check that the route was successfully obtained
        if (result != null) {

            // Remove any route previously displayed on the map
            if (route != null) {
                route.remove();
            }
            // Draw a red line in the map using the List of coordinates received
            route = map.addPolyline(new PolylineOptions()
                    .addAll(result)
                    .color(Color.parseColor("red"))
                    .width(12)
                    .geodesic(true));
        }
        // If no route was obtained then show a message saying so
        else {
            Toast.makeText(MainActivity.this, R.string.route_not_available, Toast.LENGTH_SHORT).show();
        }

        // Hide the progress bar
        progressBar.setVisibility(View.INVISIBLE);

    }

    public void displayMarkers(Address address, double latitude, double longitude) {
        String title;
        StringBuilder snippet = new StringBuilder();

        // Check that the Geocoder got an address
        if (address != null) {
            int addressLines = address.getMaxAddressLineIndex();
            if (addressLines != -1) {
                // First line of the address is the name of the place
                title = address.getAddressLine(0);
                // The rest of the lines of the address is the description (comma separated lines)
                if (addressLines > 1) {
                    snippet.append(address.getAddressLine(1));
                    for (int i = 2; i <= addressLines; i++) {
                        snippet.append(", ").append(address.getAddressLine(i));
                    }
                }
            }
            // If no address available then show a message saying so
            else {
                title = getResources().getString(R.string.geocoder_not_available);
            }
        }
        // If no address available then show a message saying so
        else {
            title = getResources().getString(R.string.geocoder_not_available);
        }

        // Add a red marker with the title, description, and coordinates information
        addMarker(latitude, longitude, title, snippet.toString(), BitmapDescriptorFactory.HUE_RED);
    }

}
