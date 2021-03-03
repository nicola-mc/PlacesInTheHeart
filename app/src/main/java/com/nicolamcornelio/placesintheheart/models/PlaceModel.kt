package com.nicolamcornelio.placesintheheart.models

import android.os.Parcel
import android.os.Parcelable

// This class is a template that describes every single place that we save.
data class PlaceModel(
        val id: Int,
        val title: String?,
        val image: String?,      // We store the image as a link to what is saved the image.
        val description: String?,
        val date: String?,
        val location: String?,
        val latitude: Double,
        val longitude: Double
): Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readDouble(),
                parcel.readDouble()
        ) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeInt(id)
                parcel.writeString(title)
                parcel.writeString(image)
                parcel.writeString(description)
                parcel.writeString(date)
                parcel.writeString(location)
                parcel.writeDouble(latitude)
                parcel.writeDouble(longitude)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<PlaceModel> {
                override fun createFromParcel(parcel: Parcel): PlaceModel {
                        return PlaceModel(parcel)
                }

                override fun newArray(size: Int): Array<PlaceModel?> {
                        return arrayOfNulls(size)
                }
        }
}
// Why we need to implements this Serializable interface, with our data class? Because when we need
// to start an intent to go to the details of a place, in intent.putExtra() method we have to
// pass some specific data type, and in order to pass an object of class PlaceModel, the object
// has to be of type Serializable! Basically what Serializable does, it will bring it into a format
// that we can pass from one class to another.
//
// UPDATE: I changed Serializable TO Parcelable because Parcelable is way faster then Serializable!