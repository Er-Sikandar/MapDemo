package com.taxi.apps.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlLineString
import com.google.maps.android.data.kml.KmlPoint
import com.google.maps.android.data.kml.KmlPolygon
import com.taxi.apps.R
import com.taxi.apps.databinding.ActivityKmlBinding
import java.net.HttpURLConnection
import java.net.URL


class KmlActivity : AppCompatActivity(), OnMapReadyCallback{
    private val TAG="KmlActivity"
    private val binding by lazy { ActivityKmlBinding.inflate(layoutInflater) }
    private var mMap: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frags_mapKml) as SupportMapFragment?
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        }


        binding.btnCheckIn.setOnClickListener{

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val shimla = LatLng(31.1048, 77.1734)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(shimla, 14f))
        try {
            val kmlLayer = KmlLayer(mMap, R.raw.suitability_map, getApplicationContext())
            kmlLayer.addLayerToMap()
            kmlLayer.setOnFeatureClickListener { feature ->
                Log.e("KML_CLICK", "Clicked feature with ID: ${feature.id}")
                val geometry = feature.geometry
                if (geometry is KmlPolygon) {
                    Log.e("KML", "Polygon Coordinates:")
                    val outerBoundary = geometry.outerBoundaryCoordinates
                    for (latLng in outerBoundary) {
                        Log.e("KML", "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
                    }
                    val center = getPolygonCenter(outerBoundary)
                    mMap?.addMarker(MarkerOptions()
                            .position(center)
                            .title("Polygon Clicked")
                            .snippet("Feature ID: ${feature.id}"))
                }
            }
           /* for (container in kmlLayer.containers) {
                getPlacemarksFromContainer(container)
            }*/

        } catch (e: Exception) {
            Log.e(TAG, "onMapReady Ex: ${e.message}")
            e.printStackTrace()
        }


    }
    fun getPolygonCenter(points: List<LatLng>): LatLng {
        var lat = 0.0
        var lng = 0.0
        for (point in points) {
            lat += point.latitude
            lng += point.longitude
        }
        val size = points.size
        return LatLng(lat / size, lng / size)
    }


    private fun getPlacemarksFromContainer(container: KmlContainer) {
        for (placemark in container.placemarks) {
            val styleUrl = placemark.styleId // e.g., "#high"
            val geometry = placemark.geometry

            val name = placemark.getProperty("name") ?: "Unknown"
            when (geometry) {
                is KmlPolygon -> {
                    val polygon = mMap?.addPolygon(PolygonOptions()
                            .addAll(geometry.outerBoundaryCoordinates)
                            .clickable(true))
                    polygon?.tag = name
                }
                is KmlPoint -> {
                    val position = LatLng(geometry.geometryObject.latitude, geometry.geometryObject.longitude)
                    val marker = mMap?.addMarker(MarkerOptions()
                            .position(position).title(name)
                    )
                    marker?.tag = name
                }
                is KmlLineString -> {
                    val polyline = mMap?.addPolyline(PolylineOptions()
                            .addAll(geometry.geometryObject)
                            .color(0xFF0000FF.toInt()).clickable(true)
                    )
                    polyline?.tag = name
                }
            }

            if (geometry is KmlPolygon) {
                Log.e("KML", "Style: $styleUrl")
                Log.e("KML", "Polygon Coordinates:")
                val outerBoundary = geometry.outerBoundaryCoordinates
                for (latLng in outerBoundary) {
                    Log.e("KML", "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
                }
                val style = placemark.inlineStyle


            }
        }
        // Recursive for nested containers
        for (childContainer in container.containers) {
            getPlacemarksFromContainer(childContainer)
        }
    }


}