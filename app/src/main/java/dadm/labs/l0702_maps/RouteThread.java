/*
 * Copyright (c) 2020. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package dadm.labs.l0702_maps;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class RouteThread extends Thread {

    final private WeakReference<MainActivity> activity;

    final double latitude;
    final double longitude;

    RouteThread(MainActivity activity, double latitude, double longitude) {
        this.activity = new WeakReference<>(activity);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void run() {
        // Updates the interface of activity that launched the asynchronous task to display a progress bar.
        if (activity.get() != null) {
            activity.get().runOnUiThread(() -> activity.get().displayProgressBar());
        }

        // Gets the route between the two markers and creates a matching Polyline

        // URI to ask Google Maps Directions API for the route between the DADM lab and another coordinate.
        // It requires a billing account
        String uri = String.format(Locale.ENGLISH, "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=%1$f,%2$f&destination=39.482463,-0.346415&mode=DRIVING&" +
                "key=PLACE_HERE_YOUR_API_KEY", latitude, longitude);

        try {
            // Launch the related GET request
            URL url = new URL(uri);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            // Check that the request has been successful
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse the response as a JSON object
                JSONObject object = new JSONObject(response.toString());
                // Get the array named "routes"
                JSONArray routesArray = object.getJSONArray("routes");
                // The first object of this array contains an "overview_polyline" object
                JSONObject route = routesArray.getJSONObject(0);
                JSONObject polyline = route.getJSONObject("overview_polyline");
                // This object consists of a string "points" representing the different points
                // that should be connected to display this route on the map.
                // The PolyUtil package decodes this string into a List of LatLng objects
                final List<LatLng> pointsList = PolyUtil.decode(polyline.getString("points"));

                // Return the List of LatLng objects constituting the route to be displayed
                if (activity.get() != null) {
                    this.activity.get().runOnUiThread(() -> activity.get().displayRoute(pointsList));
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
