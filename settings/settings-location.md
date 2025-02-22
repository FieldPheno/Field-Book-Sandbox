Location Settings
=================

<figure align="center" class="image">
  <img src="_static/images/settings/location/settings_location_framed.png" width="350px"> 
  <figcaption><i>Location settings screen layout</i></figcaption> 
</figure>

GeoCoordinates
--------------

#### <img ref="gps" style="vertical-align: middle;" src="_static/icons/formats/crosshairs-gps.png" width="20px"> Location Provider

Set the source of location data. The device gps or connect an external device with bluetooth.

#### <img ref="gps" style="vertical-align: middle;" src="_static/icons/formats/select-marker.png" width="20px"> Collection Level

Set the method for collecting location while using Field Book. Can be at
the level of the field, the plot, or the individual observation.

<figure align="center" class="image">
  <img src="_static/images/settings/location/settings_location_collection_level.png" width="325px"> 
  <figcaption><i>Location collect options</i></figcaption> 
</figure>

Geonav
------

#### <img ref="geonav" style="vertical-align: middle;" src="_static/icons/settings/location/map-search.png" width="20px"> Enable Geonav

Enables the device to move between entires based on GPS data. Requires
entries to have GNSS data.

#### <img ref="method" style="vertical-align: middle;" src="_static/icons/settings/location/function-variant.png" width="20px"> Search Method

The method used to match GPS location to entry. Defaults to the distance
method.

<figure align="center" class="image">
  <img src="_static/images/settings/location/settings_geonav_search_method.png" width="325px"> 
  <figcaption><i>GeoNav search method options</i></figcaption> 
</figure>

#### <img ref="log" style="vertical-align: middle;" src="_static/icons/settings/location/signal-distance-variant.png" width="20px"> Proximity Check

Set the distance in km from known plots at which GeoNav should turn off.

#### <img ref="log" style="vertical-align: middle;" src="_static/icons/settings/location/note-off-outline.png" width="20px"> GeoNav Log

Turns on GeoNav logging to a logfile stored in `/storage/geonav/log.txt`

#### <img ref="interval" style="vertical-align: middle;" src="_static/icons/settings/location/timer-sand-empty.png" width="20px"> Update Interval(s)

Changes the time between GeoNav location updates. Can be set to 1s
(default), 5s, or 10s

<figure align="center" class="image">
  <img src="_static/images/settings/location/settings_geonav_update_interval.png" width="325px"> 
  <figcaption><i>GeoNav interval options</i></figcaption> 
</figure>
