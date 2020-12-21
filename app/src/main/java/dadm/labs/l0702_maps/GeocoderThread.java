/*
 * Copyright (c) 2020. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package dadm.labs.l0702_maps;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class GeocoderThread extends Thread {

    final private WeakReference<MainActivity> activity;

    final private double latitude;
    final private double longitude;

    GeocoderThread(MainActivity activity, double latitude, double longitude) {
        this.activity = new WeakReference<>(activity);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void run() {
        // Translates coordinates into address in a background thread
        try {

            // Hold reference to a Geocoder to translate coordinates into human readable addresses
            Geocoder geocoder;
            // Initialize the Geocoder
            geocoder = new Geocoder(activity.get());

            // Gets a maximum of 1 address from the Geocoder
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            // Check that the Geocoder has obtained at least 1 address
            if ((addresses != null) && (addresses.size() > 0)) {
                // Updates the interface of activity that launched the asynchronous task
                if (activity.get() != null) {
                    activity.get().runOnUiThread(() -> activity.get().displayMarkers(addresses.get(0), latitude, longitude));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
