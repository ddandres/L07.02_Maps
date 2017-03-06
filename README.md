# L07.02_Maps
Lecture 07.02 Maps, DISCA - UPV, Development of apps for mobile devices.

Displays the location of the SDM lab as a marker on Google Maps within a SupportMapFragment.
New markers can be added through their longitudes and latitudes.
Human readable addresses are obtained and displayed in a custom InfoWindow when markers are clicked.
If the InfoWindow is clicked, then the route from SDM lab to that location is obtained through the Goggle Maps Directions API, and it is displayed on the map using the Maps Util library.
