package com.taxi.apps.baseui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.taxi.apps.R
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun showToast(context: Context,title: String){
        Toast.makeText(context,title, Toast.LENGTH_LONG).show()
    }
    fun addLog(tag:String,values:String){
        Log.e(tag, "Data Log: $values")
    }

    fun checkPermissions(context: Activity?, permissions: Array<String>, permissionRequestCode: Int): Boolean {
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        for (p in permissions) {
            val result = ContextCompat.checkSelfPermission(context!!, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(context!!, listPermissionsNeeded.toTypedArray(), permissionRequestCode)
            return false
        }
        return true
    }
    fun aDialogOnPermissionDenied(mContext: AppCompatActivity, isCancel: Boolean) {
        val alertDialogBuilder = AlertDialog.Builder(mContext)
        alertDialogBuilder.setTitle(mContext.getResources().getString(R.string.alert))
        alertDialogBuilder.setMessage(
            mContext.getResources().getString(R.string.reGrantPermissionMsg)
        )
        alertDialogBuilder.setPositiveButton(
            mContext.getResources().getString(R.string.action_settings)
        ) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            val uri = Uri.fromParts("package", mContext.packageName, null)
            val settingsIntent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            mContext.startActivity(settingsIntent)
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setCancelable(isCancel)
        alertDialog.show()
    }
    fun isConnected(ctx: Context): Boolean {
        val connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = connectivityManager.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    fun showGpsErrorDialog(context: Activity, title: String?, msg: String?) {
        val alertBuilder = AlertDialog.Builder(context)
        alertBuilder.setCancelable(false)
        alertBuilder.setTitle(title)
        alertBuilder.setMessage(msg)
        alertBuilder.setPositiveButton(android.R.string.ok) { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        val alert = alertBuilder.create()
        alert.show()
    }

    fun getCompleteAddressString(context: Context?, LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(context!!, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()
                Log.w("MyCurrentloctionaddress", strReturnedAddress.toString())
            } else {
                Log.w("MyCurrentloctionaddress", "No Address returned!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w("MyCurrentloctionaddress", "Canont get Address!")
        }
        return strAdd
    }

    fun getDestinationBitmap(): Bitmap {
        val height = 20
        val width = 20
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    fun getRotation(start: LatLng, end: LatLng): Float {
        val latDifference: Double = abs(start.latitude - end.latitude)
        val lngDifference: Double = abs(start.longitude - end.longitude)
        var rotation = -1F
        when {
            start.latitude < end.latitude && start.longitude < end.longitude -> {
                rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
            }
            start.latitude >= end.latitude && start.longitude < end.longitude -> {
                rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
            }
            start.latitude >= end.latitude && start.longitude >= end.longitude -> {
                rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
            }
            start.latitude < end.latitude && start.longitude >= end.longitude -> {
                rotation =
                    (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
            }
        }
        addLog("TAG", "getRotation: $rotation")
        return rotation
    }

}