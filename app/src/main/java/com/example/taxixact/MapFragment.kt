package com.example.taxixact

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import pub.devrel.easypermissions.EasyPermissions

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private var listener: MapFragmentListener? = null
    private lateinit var googleMap: GoogleMap
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val location = locationResult.lastLocation ?: return
            handleLocationUpdate(location)
        }
    }
    private var isTracking = false
    private var startMarker: Marker? = null
    private var lastLocation: Location? = null
    private var totalDistance = 0f

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    interface MapFragmentListener {
        fun onDistanceUpdated(distance: Float)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MapFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement MapFragmentListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        applyMapStyle()
        requestLocationPermission()
    }

    private fun applyMapStyle() {
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_dark_style)
            )
            if (!success) Log.e("MapFragment", "Failed to apply map style.")
        } catch (e: Resources.NotFoundException) {
            Log.e("MapFragment", "Map style not found.", e)
        }
    }

    private fun enableUserLocation() {
        if (hasLocationPermission()) {
            googleMap.isMyLocationEnabled = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

        if (hasLocationPermission()) {
            enableUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    fun requestLocationPermission() {
        if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableUserLocation()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Location permission is required to use this feature.",
                LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startTracking() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        isTracking = true
        resetTrackingData()

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopTracking() {
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        resetTrackingData()
        listener?.onDistanceUpdated(0f)
    }

    private fun resetTrackingData() {
        startMarker?.remove()
        startMarker = null
        lastLocation = null
        totalDistance = 0f
    }

    private fun handleLocationUpdate(location: Location) {
        if (lastLocation == null) {
            lastLocation = location
            startMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title("Start")
            )
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startMarker!!.position, 15f))
            return
        }

        val distanceMoved = lastLocation!!.distanceTo(location)
        if (distanceMoved > 2) {
            totalDistance += distanceMoved
            listener?.onDistanceUpdated(totalDistance)
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
        }

        lastLocation = location
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTracking()
    }
}
