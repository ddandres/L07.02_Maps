/*
 * Copyright (c) 2019. David de Andrés and Juan Carlos Ruiz, DISCA - UPV, Development of apps for mobile devices.
 */

package dadm.labs.l0702_maps;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class GeocoderAsyncTask extends AsyncTask<Double, Void, Address> {

    private WeakReference<MainActivity> activity;

    private double latitude;
    private double longitude;

    GeocoderAsyncTask(MainActivity activity) {
        this.activity = new WeakReference<>(activity);
    }

    /**
     * Translates coordinates into address in a background thread.
     */
    @Override
    protected Address doInBackground(Double... params) {

        if (activity.get() != null) {
            try {
                latitude = params[0];
                longitude = params[1];

                // Hold reference to a Geocoder to translate coordinates into human readable addresses
                Geocoder geocoder;
                // Initialize the Geocoder
                geocoder = new Geocoder(activity.get());

                // Gets a maximum of 1 address from the Geocoder
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                // Check that the Geocoder has obtained at least 1 address
                if ((addresses != null) && (addresses.size() > 0)) {
                    return addresses.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Updates the interface of activity that launched the asynchronous task.
     */
    @Override
    protected void onPostExecute(Address address) {
        if (activity.get() != null) {
            activity.get().displayMarkers(address, latitude, longitude);
        }
    }
}
