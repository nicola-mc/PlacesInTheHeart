package com.nicolamcornelio.placesintheheart.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

// This class will inherits from AsyncTask cause we don't want to block the main thread.
// We'll use this class for get the address of curr location from cords and display it in the box.
class GetAddressFromLatLng(
        context: Context,
        private val latitude: Double,
        private val longitude: Double
        ) : AsyncTask<Void, String, String>() {

    // This class will take care of actually making a location readable from lat and log.
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    // Here there will be the encode of the coordinates in readable text, of course in background.
    override fun doInBackground(vararg params: Void?): String {
        // Try to get the location as readable text from coordinates (Something can goes wrong).
        try {
            // Now here will happen the magic: I want only one address from this location.
            val addressList: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                // Getting the very first entry of the list.
                val address: Address = addressList[0]
                val sb = StringBuilder()
                // An Address object has multiple entries itself, so let's go through all of them.
                // Address has multiples attributes (mLocality, mCountryName, mCountryCode etc...).
                for (i in 0..address.maxAddressLineIndex) {
                    // Building the address that we'll display as a string, appending the entries.
                    sb.append(address.getAddressLine(i)).append(" ")
                }
                // Deleting the last space at the end of the string.
                sb.deleteCharAt(sb.length-1)
                // Our "string" is always of type StringBuilder, so we need to return it with toString().
                return sb.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    // We're not using this method as we don't need to do particular operations after the execution.
    override fun onPostExecute(resultString: String?) {
        if (resultString == null) {
            // If something goes wrong (location cannot be get), call onError().
            mAddressListener.onError()
        } else {
            // Otherwise call onAddressFound.
            mAddressListener.onAddressFound(resultString)
        }
        super.onPostExecute(resultString)
    }

    // We'll use this to set the mAddressListener of type of our interface created. It's a setter.
    fun setAddressListener(addressListener: AddressListener) {
        mAddressListener = addressListener
    }
    // This method just start the AsyncTask, otherwise all the stuff in this class will be useless.
    fun getAddress() {
        // Calling this method in fact starts the AsyncTask!
        execute()
    }

    // We'll create here our own listener.
    interface AddressListener{
        // What we want to execute when the address is found.
        fun onAddressFound(address: String?)
        // What shall happens if we get an error.
        fun onError()
    }
}