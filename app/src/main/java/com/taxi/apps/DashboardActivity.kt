package com.taxi.apps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.internal.ViewUtils
import com.taxi.apps.baseui.BaseActivity
import com.taxi.apps.databinding.ActivityDashboardBinding
import com.taxi.apps.utils.AnimationUtils
import com.taxi.apps.utils.Const
import com.taxi.apps.utils.GpsFetch
import java.util.Arrays


class DashboardActivity : BaseActivity(), OnMapReadyCallback {
    private val TAG="DashboardActivity"
    private val binding by lazy { ActivityDashboardBinding.inflate(layoutInflater) }
    private lateinit var gpsFetch: GpsFetch

    private var mMap: GoogleMap? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var mLocationRequest: LocationRequest
    lateinit var curLocation: Location
    lateinit var curLatLng: LatLng
    lateinit var pickUpLatLng: LatLng
    lateinit var dropLatLng: LatLng
    var Location_address: String? = null
    private var isGrant = false
    private var movingMarker: Marker? = null
    private val nearbyCabMarkerList = arrayListOf<Marker>()
    private var greyPolyLine: Polyline? = null
    private var blackPolyline: Polyline? = null
    private var destinationMarker: Marker? = null
    private var originMarker: Marker? = null

    override fun onRestart() {
         addLog(TAG, "onRestart: ")
        locationPermission()
        super.onRestart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        gpsFetch = GpsFetch(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frags_map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        locationPermission()

        binding.pickUpTextView.setOnClickListener {
            showAutoCompleteActivity(this, Const.PICKUP_REQUEST_CODE)
        }
        binding.dropTextView.setOnClickListener {
            showAutoCompleteActivity(this, Const.DROP_REQUEST_CODE)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }

    private fun locationPermission() {
        val psn = Const.permissionsRequiredLocation
        if (Build.VERSION.SDK_INT >= 23) {
            isGrant = true
            if (checkPermissions(this, psn, Const.LOCATION_REQUEST_CODE)) {
                gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
                    override fun gpsStatus(isGPSEnable: Boolean) {
                        if (isGPSEnable) {
                            getLatLog()
                        }else{
                            showGpsErrorDialog(this@DashboardActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                        }
                    }
                })
            }
        } else {
            gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
                override fun gpsStatus(isGPSEnable: Boolean) {
                    if (isGPSEnable) {
                        getLatLog()
                    }else{
                        showGpsErrorDialog(this@DashboardActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                    }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Const.LOCATION_REQUEST_CODE) {
            val _arPermission= ArrayList<String>()
            if (grantResults.size > 0) {
                for (i in permissions.indices) {
                    if (grantResults[i] != 0) {
                        _arPermission.add(Const.EMPTY + grantResults[i])
                    }
                }
                if (_arPermission.isEmpty() || _arPermission.size == 1) {
                    if (isGrant) {
                        gpsFetch.turnGPSOn(object : GpsFetch.onGpsListener {
                            override fun gpsStatus(isGPSEnable: Boolean) {
                                if (isGPSEnable) {
                                    getLatLog()
                                }else{
                                    showGpsErrorDialog(this@DashboardActivity,resources.getString(R.string.alert),resources.getString(R.string.enable_gps))
                                }
                            }
                        })
                    }
                } else {
                    aDialogOnPermissionDenied(this, false)
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    private fun getLatLog() {
        mLocationRequest = LocationRequest()
        mLocationRequest.setInterval(10000)
        mLocationRequest.setFastestInterval(10000)
        mLocationRequest.setSmallestDisplacement(10f)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }
    var mLocationCallback: LocationCallback = object : LocationCallback() {
        @SuppressLint("MissingPermission", "RestrictedApi")
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                curLocation = locationList[locationList.size - 1]
                curLatLng = LatLng(curLocation.latitude, curLocation.longitude)
                pickUpLatLng=curLatLng
                Location_address=getCompleteAddressString(this@DashboardActivity,curLocation.getLatitude(),curLocation.getLongitude())
                addLog(TAG, "onLocationResult: "+Location_address)
                binding.pickUpTextView.text=Location_address
                mMap!!.setPadding(0, 20, 0, 0)
                mMap!!.isMyLocationEnabled = true
                val cameraPosition = CameraPosition.Builder().target(curLatLng).zoom(15.5f).build()
                mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                val getCarLatLng: MutableList<LatLng> = mutableListOf()
                getCarLatLng.add(LatLng(28.6126118, 77.3485173))
                getCarLatLng.add(LatLng(28.6246863, 77.3236084))
                getCarLatLng.add(LatLng(28.6199957, 77.3568429))
                getCarLatLng.add(LatLng(28.6227722, 77.3848467))
                showNearbyCabs(getCarLatLng)



                
              /*  val currentLatLng = LatLng(location.getLatitude(), location.getLongitude())
                mMap!!.addMarker(MarkerOptions().position(currentLatLng).title(Location_address))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))*/
            }
        }
    }
    fun addOrUpdateMarker(newLatLng: LatLng, locationName: String, map: GoogleMap) {
        if (movingMarker == null) {
            movingMarker = map.addMarker(MarkerOptions().position(newLatLng).title(locationName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        } else {
            movingMarker?.position = newLatLng
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15f))
    }

    /*override*/ fun showNearbyCabs(latLngList: List<LatLng>) {
        nearbyCabMarkerList.clear()
        for (latLng in latLngList) {
            val nearbyCabMarker = addCarMarkerAndGet(latLng)
            nearbyCabMarkerList.add(nearbyCabMarker!!)
        }
    }

    /*override*/ fun showPath(latLngList: List<LatLng>) {
        val builder = LatLngBounds.Builder()
        for (latLng in latLngList) {
            builder.include(latLng)
        }
        val bounds = builder.build()
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 2))
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.GRAY)
        polylineOptions.width(5f)
        polylineOptions.addAll(latLngList)
        greyPolyLine = mMap!!.addPolyline(polylineOptions)

        val blackPolylineOptions = PolylineOptions()
        blackPolylineOptions.width(5f)
        blackPolylineOptions.color(Color.BLACK)
        blackPolyline = mMap!!.addPolyline(blackPolylineOptions)

        originMarker = addOriginDestinationMarkerAndGet(latLngList[0])
        originMarker?.setAnchor(0.5f, 0.5f)
        destinationMarker = addOriginDestinationMarkerAndGet(latLngList[latLngList.size - 1])
        destinationMarker?.setAnchor(0.5f, 0.5f)

        val polylineAnimator = AnimationUtils.polyLineAnimator()
        polylineAnimator.addUpdateListener { valueAnimator ->
            val percentValue = (valueAnimator.animatedValue as Int)
            val index = (greyPolyLine?.points!!.size * (percentValue / 100.0f)).toInt()
            blackPolyline?.points = greyPolyLine?.points!!.subList(0, index)
        }
        polylineAnimator.start()
    }

    private fun addOriginDestinationMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getDestinationBitmap())
        return mMap!!.addMarker(
            MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor)
        )
    }

    private fun addCarMarkerAndGet(latLng: LatLng): Marker? {
        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(getCarBitmap(this))
        return  mMap!!.addMarker(MarkerOptions().position(latLng).flat(true).icon(bitmapDescriptor))
    }
    fun getCarBitmap(context: Context): Bitmap {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_car)
        return Bitmap.createScaledBitmap(bitmap, 50, 100, false)
    }

    fun showAutoCompleteActivity(activity: Activity, requestCode: Int) {
        try {
            if (!Places.isInitialized())
                Places.initialize(activity, "AIzaSyBVZgVlbt68uvrvi38-mpljXPi3huIsreM")
            val fields: List<Place.Field> = Arrays.asList(
                Place.Field.ADDRESS,
                Place.Field.NAME,
                Place.Field.LAT_LNG)
            val intent: Intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY,
                fields
            ).build(activity)
            activity.startActivityForResult(intent, requestCode)
        } catch (e: RuntimeException) {
            Log.e(TAG, "showAutoCompleteActivity: " + e.message)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            try {
                val place = Autocomplete.getPlaceFromIntent(data)
                addLog(TAG,"Add: "+place.name + " " + place.address)
                when (requestCode) {
                    Const.PICKUP_REQUEST_CODE -> {
                        binding.pickUpTextView.text=place.name
                        pickUpLatLng = place.latLng
                       // checkAndShowRequestButton()
                    }

                    Const.DROP_REQUEST_CODE -> {
                        binding.dropTextView.text = place.name
                        dropLatLng = place.latLng
                       // checkAndShowRequestButton()
                    }
                }
            } catch (e:Exception) {
                addLog(TAG, "onActivityResult: " + e.message)
            }
        }
    }

    override fun onStop() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        super.onStop()
    }
}