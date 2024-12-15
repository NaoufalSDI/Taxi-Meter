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

class MapFragment : Fragment(R.layout.fragment_map),
    OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks {

    private var listener: MapFragmentListener? = null
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var isLocationSet = false
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

    private var mapFragmentListener: MapFragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MapFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement MapFragmentListener")
        }
    }

    fun setMapFragmentListener(listener: MapFragmentListener) {
        this.mapFragmentListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_dark_style)
            )
            if (!success) {
                Log.e("MapFragment", "Failed to apply map style.")
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
        requestLocationPermission()
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            enableUserLocation()
        }
    }

    fun startTracking() {
        isTracking = true
        totalDistance = 0f
        startMarker?.remove()
        startMarker = null
        lastLocation = null

        // Initialize locationCallback here to ensure it's always defined
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    lastLocation = location
                    val initialLatLng = LatLng(location.latitude, location.longitude)

                    startMarker = googleMap.addMarker(
                        MarkerOptions()
                            .position(initialLatLng)
                            .title("Start Point")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 16f))

                    isLocationSet = true
                    startLocationUpdates()
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                numUpdates = 1
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            requestLocationPermission()
        }
    }

    fun stopTracking() {
        isTracking = false

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        startMarker?.remove()
        startMarker = null
        totalDistance = 0f
        lastLocation = null
        isLocationSet = false
        mapFragmentListener?.onDistanceUpdated(0f)
    }


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000L
            fastestInterval = 100L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val currentLocation = locationResult.lastLocation

                if (currentLocation != null && isTracking) {
                    handleLocationUpdate(currentLocation)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && isTracking
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun handleLocationUpdate(location: Location) {
        if (lastLocation != null) {
            val distanceMoved = lastLocation?.distanceTo(location) ?: 0f
            if (distanceMoved > 2) {
                totalDistance += distanceMoved
                mapFragmentListener?.onDistanceUpdated(totalDistance)
                val currentLatLng = LatLng(location.latitude, location.longitude)
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng))
            }
        }
        lastLocation = location
    }



    override fun onDestroyView() {
        super.onDestroyView()
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
