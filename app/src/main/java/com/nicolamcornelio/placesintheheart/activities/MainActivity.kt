package com.nicolamcornelio.placesintheheart.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nicolamcornelio.placesintheheart.adapters.PlacesAdapter
import com.nicolamcornelio.placesintheheart.R
import com.nicolamcornelio.placesintheheart.database.DatabaseHandler
import com.nicolamcornelio.placesintheheart.models.PlaceModel
import com.nicolamcornelio.placesintheheart.utils.SwipeToDeleteCallback
import com.nicolamcornelio.placesintheheart.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddPlace.setOnClickListener {
            val intent = Intent(this, AddPlaceActivity::class.java)
            // We use this instead of startActivity() to refresh dynamically the places list.
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        // Get all the places from the DB.
        getPlacesFromDB()
    }
    // We'll setup the RecyclerView with the latest data.
    private fun setupPlacesRecyclerView(placeList: ArrayList<PlaceModel>) {
        // Our RecyclerView id. Setting his layout to linear layout.
        rv_places_list.layoutManager = LinearLayoutManager(this)
        rv_places_list.setHasFixedSize(true)

        // Get an instance of PlacesAdapter. We pass to the constructor the list of HP.
        val placesAdapter = PlacesAdapter(this, placeList)
        rv_places_list.adapter = placesAdapter
        // Now I call the getter of the OnClickListener interface object and the override onClick.
        placesAdapter.setOnClickListener(object: PlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: PlaceModel) {
                // When I do so, I want to create an intent that move us to the HPDetailActivity.
                val intent = Intent(this@MainActivity,
                    PlaceDetailActivity::class.java)
                // I want to put some extra information into the intent.
                intent.putExtra(EXTRA_PLACE_DETAILS, model)
                startActivity(intent)
            }
        })
        // Implementing the edit with the swipe.
        val editSwipeHandler = object : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // The adapter should notify the added item.
                val adapter = rv_places_list.adapter as PlacesAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }
        // Now we attach the swiping functionality to the RecyclerView, otherwise will not work.
        // These two lines in fact are very important.
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_places_list)

        // Implementing the delete with the swipe.
        val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // The adapter should notify the added item.
                val adapter = rv_places_list.adapter as PlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                // Recall this method to show "NO PLACES" or the list of places, if there're in DB.
                getPlacesFromDB()
            }
        }
        // Now we attach the swiping functionality to the RecyclerView, otherwise will not work.
        // These two lines in fact are very important.
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_places_list)
    }
    // This method gets all the entries from the DB and makes visible or gone particular views, if
    // there are entries in the DB or not (i.e. showing "No Places Added" or the list of places).
    private fun getPlacesFromDB() {
        val dbHandler = DatabaseHandler(this)
        // Get the places list from the DB.
        val getPlaceList : ArrayList<PlaceModel> = dbHandler.getPlacesList()

        if (getPlaceList.size > 0) {
            // Make the RecyclerView visible.
            rv_places_list.visibility = View.VISIBLE
            // Make the no records available text gone.
            tv_no_records_available.visibility = View.GONE
            // Let's setup the RecyclerView. We'll pass so to this method the list of HP.
            setupPlacesRecyclerView(getPlaceList)
        } else {    // If there are no places saved, just hide the RV and show the TV.
            rv_places_list.visibility = View.GONE
            tv_no_records_available.visibility = View.VISIBLE
        }
    }
    // Remember that we call startActivityForResult() so we need to override this method.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // So when I add a new place, the list is automatically refreshed now!
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getPlacesFromDB()
            } else {
                Log.i("Activity", "Cancelled or Back pressed.")
            }
        }
    }

    companion object {
        // Code for startActivityForResult() in order to refresh the list after adding one place.
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}