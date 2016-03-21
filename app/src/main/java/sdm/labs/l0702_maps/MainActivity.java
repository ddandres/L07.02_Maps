package sdm.labs.l0702_maps;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    Geocoder geocoder;
    GoogleMap map;
    Polyline route = null;

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
        etLongitude = (EditText) findViewById(R.id.etLongitude);
        etLatitude = (EditText) findViewById(R.id.etLatitude);
        bAddMarker = (Button) findViewById(R.id.bAddMarker);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this);

        infoWindowAdapter = new MyInfoWindowAdapter();
    }

    public void translateCoordinates(View view) {
        if (isConnectionAvailable()) {
            (new GeocoderAsyncTask()).execute(
                    Double.valueOf(etLatitude.getText().toString()),
                    Double.valueOf(etLongitude.getText().toString()));
        }
    }

    private void addMarker(double latitude, double longitude, String title, String snippet, float color) {
        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(latitude, longitude));
        options.title(title);
        options.snippet(snippet);
        options.icon(BitmapDescriptorFactory.defaultMarker(color));

        map.addMarker(options);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (map != null) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mNormalMap:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mTerrainMap:
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mSatelliteMap:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
        return true;
    }

    /**
     * Check whether Internet connectivity is available.
     */
    private boolean isConnectionAvailable() {

        // Get a reference to the ConnectivityManager
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get information for the current default data network
        NetworkInfo info = manager.getActiveNetworkInfo();
        // Return true if there is network connectivity
        return ((info != null) && info.isConnected());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.482463, -0.346415), 10));
        map.setInfoWindowAdapter(infoWindowAdapter);
        map.setOnInfoWindowClickListener(this);

        addMarker(39.482463, -0.346415, getResources().getString(R.string.lab_sdm_title),
                getResources().getString(R.string.lab_sdm_snippet), BitmapDescriptorFactory.HUE_GREEN);

        supportInvalidateOptionsMenu();
        bAddMarker.setVisibility(View.VISIBLE);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (isConnectionAvailable()) {
            (new RouteAsyncTask()).execute(marker.getPosition().latitude, marker.getPosition().longitude);
        }
    }

    private class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View result = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            TextView tvTitle = (TextView) result.findViewById(R.id.tvTitle);
            TextView tvSnippet = (TextView) result.findViewById(R.id.tvSnippet);
            TextView tvCoordinates = (TextView) result.findViewById(R.id.tvCoordinates);
            tvTitle.setText(marker.getTitle());
            tvSnippet.setText(marker.getSnippet());
            tvCoordinates.setText(
                    String.format(
                            getResources().getString(R.string.info_window_coordinates),
                            marker.getPosition().latitude,
                            marker.getPosition().longitude));

            return result;
        }
    }

    private class GeocoderAsyncTask extends AsyncTask<Double, Void, Address> {

        double latitude;
        double longitude;

        @Override
        protected Address doInBackground(Double... params) {
            try {
                latitude = params[0];
                longitude = params[1];

                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if ((addresses != null) && (addresses.size() > 0)) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            String title;
            StringBuilder snippet;

            snippet = new StringBuilder();
            if (address != null) {
                int addressLines = address.getMaxAddressLineIndex();
                if (addressLines != -1) {
                    title = address.getAddressLine(0);
                    if (addressLines > 1) {
                        snippet.append(address.getAddressLine(1));
                        for (int i = 2; i <= addressLines; i++) {
                            snippet.append(", ").append(address.getAddressLine(i));
                        }
                    }
                } else {
                    title = getResources().getString(R.string.geocoder_not_available);
                }
            } else {
                title = getResources().getString(R.string.geocoder_not_available);
            }
            addMarker(latitude, longitude, title, snippet.toString(), BitmapDescriptorFactory.HUE_RED);
        }
    }

    private class RouteAsyncTask extends AsyncTask<Double, Void, List<LatLng>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<LatLng> doInBackground(Double... params) {

            List<LatLng> pointsList = null;

            String uri = String.format("https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=%1$f,%2$f&destination=39.482463,-0.346415&mode=driving&" +
                    "key=AIzaSyDRJmG9bE2sLHX0EW5BZJ4C8lvk71r7s0s", params[0], params[1]);

            try {
                URL url = new URL(uri);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject object = new JSONObject(response.toString());
                    JSONArray routesArray = object.getJSONArray("routes");
                    JSONObject route = routesArray.getJSONObject(0);
                    JSONObject polyline = route.getJSONObject("overview_polyline");
                    pointsList = PolyUtil.decode(polyline.getString("points"));

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return pointsList;
        }

        @Override
        protected void onPostExecute(List<LatLng> result) {
            if (result != null) {
                if (route != null) {
                    route.remove();
                }
                route = map.addPolyline(new PolylineOptions()
                        .addAll(result)
                        .color(Color.parseColor("#FF0000"))
                        .width(12)
                        .geodesic(true));
            } else {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
