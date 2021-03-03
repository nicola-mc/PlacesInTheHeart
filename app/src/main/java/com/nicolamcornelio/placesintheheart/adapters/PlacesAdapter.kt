package com.nicolamcornelio.placesintheheart.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nicolamcornelio.placesintheheart.R
import com.nicolamcornelio.placesintheheart.activities.AddPlaceActivity
import com.nicolamcornelio.placesintheheart.activities.MainActivity
import com.nicolamcornelio.placesintheheart.database.DatabaseHandler
import com.nicolamcornelio.placesintheheart.models.PlaceModel
import kotlinx.android.synthetic.main.item_place.view.*

// Creating an adapter class for binding it to the RecyclerView.
open class PlacesAdapter(
    private val context: Context,
    private var list: ArrayList<PlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // An object of our interface declared a the bottom here.
    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_place,
                parent,
                false
            )
        )
    }
    /**
     * Why we need this method? Because our class Adapter CANNOT implements onClickListener!
     * This workaround to do so is the best practise to implement the clickable items of a RV.
     * Here we actually also bind the onClickListener to the one we are passing.
    */
    fun setOnClickListener(onClickListener: OnClickListener) {
        // So this is just a simple setter of the object of OnClickListener interface.
        this.onClickListener = onClickListener
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // model is the current view from the RecyclerView.
        val model = list[position]
        // Binding all the items. An item of the RV has: title, description and image.
        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvDescription.text = model.description
            // When user taps some item in the RecyclerView...
            holder.itemView.setOnClickListener{
                // Check first if the object isn't null...
                if (onClickListener != null) {
                    // And then call the onClick method passing the number of the item tapped.
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }
    // Notify the Adapter when, swiping an item, it changes. THIS WILL CALLED FROM MainActivity!
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        // I want to notify our adapter that from this particular list element I'd like to change.
        val intent = Intent(context, AddPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])

        activity.startActivityForResult(intent, requestCode)
        // Now the adapter has to be notified of any changed made to the RecyclerView!
        notifyItemChanged(position)
    }

    // This method removes the entry from the adapter, but we need to do this effectively in DB.
    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        // Calling the DB. The row will be removed.
        val isDeleted = dbHandler.deletePlace(list[position])
        // Here we'll remove it from the adapter.
        if (isDeleted > 0) {
            list.removeAt(position)
            // Adapter has to be notified of the changes.
            notifyItemRemoved(position)
        }
    }


    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    /**
     * We need this interface in order to implement the tap for each item in the RecyclerView.
     */
    interface OnClickListener {
        fun onClick(position: Int, model: PlaceModel)
    }
}
// END