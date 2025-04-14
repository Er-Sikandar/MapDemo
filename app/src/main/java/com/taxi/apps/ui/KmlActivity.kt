package com.taxi.apps.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPolygon
import com.google.maps.android.data.kml.KmlStyle
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
            for (container in kmlLayer.containers) {
                getPlacemarksFromContainer(container)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMapReady Ex: ${e.message}")
            e.printStackTrace()
        }
       /* val kmlUrl = "https://developers.google.com/maps/documentation/javascript/examples/kml/westcampus.kml"
       // val kmlUrl = "http://developers.google.com/kml/documentation/KML_Samples.kml"
        Thread {
            try {
                val url = URL(kmlUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    // Create KmlLayer from InputStream
                    val kmlLayer = KmlLayer(mMap, inputStream, applicationContext)
                    runOnUiThread {
                        kmlLayer.addLayerToMap()
                    }
                } else {
                    Log.e(TAG, "HTTP error: ${connection.responseCode}")
                }

            } catch (e: Exception) {
                Log.e(TAG, "KML load error: ${e.message}")
                e.printStackTrace()
            }
        }.start()*/


    }

    private fun getPlacemarksFromContainer(container: KmlContainer) {
        for (placemark in container.placemarks) {
            val styleUrl = placemark.styleId // e.g., "#high"

            val geometry = placemark.geometry
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