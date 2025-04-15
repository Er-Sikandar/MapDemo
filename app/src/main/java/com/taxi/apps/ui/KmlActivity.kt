package com.taxi.apps.ui

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.android.data.kml.KmlContainer
import com.google.maps.android.data.kml.KmlLayer
import com.google.maps.android.data.kml.KmlPolygon
import com.taxi.apps.R
import com.taxi.apps.databinding.ActivityKmlBinding
import com.taxi.apps.databinding.GridSheetLayoutBinding


class KmlActivity : AppCompatActivity(), OnMapReadyCallback{
    private val TAG="KmlActivity"
    private val binding by lazy { ActivityKmlBinding.inflate(layoutInflater) }
    private var mMap: GoogleMap? = null
    private var currentPolygonMarker: Marker? = null
    private var areaSuitability: String = ""
    private var coordinatesString: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frags_mapKml) as SupportMapFragment?
        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        }


        binding.btnCheckIn.setOnClickListener{
         if (!TextUtils.isEmpty(areaSuitability) && !TextUtils.isEmpty(coordinatesString)){
             gridDetailsPopup()
         }else{
             Toast.makeText(applicationContext,"Please select Polygon then check in.", Toast.LENGTH_LONG).show()
         }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        try {
            val kmlLayer = KmlLayer(mMap, R.raw.suitability_map, getApplicationContext())
            kmlLayer.addLayerToMap()
            kmlLayer.setOnFeatureClickListener { feature ->
                Log.e("KML_CLICK", "Clicked feature with ID: ${feature.id}")
                areaSuitability=feature.id.toString()
                val geometry = feature.geometry
                if (geometry is KmlPolygon) {
                    Log.e("KML", "Polygon Coordinates:")
                    val outerBoundary = geometry.outerBoundaryCoordinates
                    if (outerBoundary.isNotEmpty()) {
                         coordinatesString = outerBoundary.joinToString(", ") { latLng ->
                            "${latLng.latitude},${latLng.longitude}"
                        }
                        for (latLng in outerBoundary) {
                            Log.e("KML", "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
                        }
                        val center = getPolygonCenter(outerBoundary)
                        currentPolygonMarker?.remove()
                        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_pin)
                        currentPolygonMarker=mMap?.addMarker(MarkerOptions()
                            .position(center)
                            .title("Polygon Clicked")
                            .snippet("Suitability: ${feature.id} Suitability"))
                    }

                }
            }
            for (container in kmlLayer.containers) {
                moveCamHere(container)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMapReady Ex: ${e.message}")
            e.printStackTrace()
        }


    }

    private fun moveCamHere(container: KmlContainer) {
        for (placemark in container.placemarks) {
            val geometry = placemark.geometry
            if (geometry is KmlPolygon) {
                Log.e("KML", "Move Camera Here")
                val outerBoundary = geometry.outerBoundaryCoordinates
                if (outerBoundary.isNotEmpty()) {
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(outerBoundary[0], 14f))
                    break
                }
            }
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
    private fun gridDetailsPopup() {
        val sheetDialog = AlertDialog.Builder(this@KmlActivity).create()
        val binding = GridSheetLayoutBinding.inflate(layoutInflater)
        sheetDialog.setView(binding.root)
        sheetDialog.setCancelable(false)
        sheetDialog.setCanceledOnTouchOutside(false)
        binding.apply {
            tvAreaSuitability.text="Area Suitability: ${areaSuitability} Suitability"
            tvCoordinates.text="Coordinates: $coordinatesString"
            tvNavigate.setOnClickListener {

            }
            tvCancel.setOnClickListener {
                sheetDialog.dismiss()
            }
        }
        sheetDialog.show()
    }
}


