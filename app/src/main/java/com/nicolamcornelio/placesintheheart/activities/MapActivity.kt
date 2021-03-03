package com.nicolamcornelio.placesintheheart.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nicolamcornelio.placesintheheart.R
import com.nicolamcornelio.placesintheheart.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_place.*
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mPlaceDetail: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // If the intent has information of this code (details of a place).
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // Get the data from the intent (the place's information).
            mPlaceDetail = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as PlaceModel?
        }
        // Setting the toolbar if the data from the intent is not null.
        if (mPlaceDetail != null) {

            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mPlaceDetail!!.title

            toolbar_map.setNavigationOnClickListener {
                onBackPressed()
            }
            // Getting the fragment in the activity_map.xml by his id.
            // So this will load the map.
            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)
        }
    }
    // This is for putting the markers on the chose locations.
    // We implement what happen after that the map is ready. This method is of OnMapReadyCallback.
    override fun onMapReady(googleMap: GoogleMap?) {
        // We get the lat and lng by the mPlaceDetail arrived from the intent.
        // These cords are needed for putting the marker on the map
        val position = LatLng(mPlaceDetail!!.latitude, mPlaceDetail!!.longitude)

        // Creating the marker with the addMarker method.
        googleMap!!.addMarker(MarkerOptions().position(position).title(mPlaceDetail!!.location))
        // Zooming to that position, when we open a place in the map.
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 10f)
        // With this there'll be an animation that will bring the user's view to that place. COOL!
        googleMap.animateCamera(newLatLngZoom)
    }
}