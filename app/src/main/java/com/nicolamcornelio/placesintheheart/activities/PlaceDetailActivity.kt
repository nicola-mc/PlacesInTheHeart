package com.nicolamcornelio.placesintheheart.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.nicolamcornelio.placesintheheart.R
import com.nicolamcornelio.placesintheheart.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_place.*
import kotlinx.android.synthetic.main.activity_place_detail.*
import kotlinx.android.synthetic.main.activity_place_detail.iv_place_image

class PlaceDetailActivity : AppCompatActivity() {
    private lateinit var place: PlaceModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        var placeDetailModel: PlaceModel? = null
        // Check if the intent has information with the code we specified.
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // Get the placeModel object passed from the MainActivity (the item details in RV).
            placeDetailModel = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as PlaceModel?
            if (placeDetailModel != null) {
                place = placeDetailModel
            }
        }

        if (placeDetailModel != null) {
            // Setting the title fo the toolbar with the title of the place.
            setSupportActionBar(toolbar_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = placeDetailModel.title

            toolbar_place_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            // Setting the other fields.
            iv_place_image.setImageURI(Uri.parse(placeDetailModel.image))
            tv_description.text = placeDetailModel.description
            tv_location.text = placeDetailModel.location

            // If we click on SHOW ON MAP button...
            btn_view_on_map.setOnClickListener {
                // Start the intent to the MapActivity.
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, placeDetailModel)
                startActivity(intent)
            }
        }
    }
    // To inflate the menu with the icon to the toolbar.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)

        return true
    }
    // This method is used to activate an intent when the share icon is clicked.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = resources.getResourceName(item.itemId).substringAfter('/').trim()

        if (id == "ic_share") {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val subject = "One of my Place in the Heart!"
            val body = "Hey there! \uD83D\uDE03 " +
                    "Want to share with you this place I love:" +
                    " \uD83D\uDC49\uD83C\uDFFB ${place.title}! " +
                    "I use this fantastic app! " +
                    "Download Place In The Heart now!"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            startActivity(Intent.createChooser(intent, "Share using"))
        }
        
        return super.onOptionsItemSelected(item)
    }

}