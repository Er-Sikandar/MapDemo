package com.taxi.apps.utils

import android.Manifest

object Const {
    var EMPTY=""
    var LOCATION_REQUEST_CODE = 110022
    var PICKUP_REQUEST_CODE = 110000
    var DROP_REQUEST_CODE = 220000

    var permissionsRequiredLocation = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

}