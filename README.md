# L07.02_Maps
Lecture 07.02 Maps, DISCA - UPV, Development of apps for mobile devices.

Displays the location of the DADM lab as a marker on Google Maps within a SupportMapFragment.
You must get your API_KEY for Google Maps and include it in the Manifest to enable the map.
New markers can be added through their longitudes and latitudes.
Human readable addresses are obtained and displayed in a custom InfoWindow when markers are clicked.
If the InfoWindow is clicked, then the route from DADM lab to that location is obtained through the Google Maps Directions API, and it is displayed on the map using the Maps Util library.
Google Maps Directions API requires a billing account, although you can try it once a day freely.
You must get your API_KEY and include it within the URI created in RouteAsyncTask.java.
