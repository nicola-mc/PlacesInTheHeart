package com.nicolamcornelio.placesintheheart.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.nicolamcornelio.placesintheheart.models.PlaceModel

//Creating the database logic, extending the SQLiteOpenHelper base class.
class DatabaseHandler(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1 // Database version
        private const val DATABASE_NAME = "PlacesDatabase" // Database name
        private const val TABLE_PLACE = "PlacesTable" // Table Name

        //All the Columns names
        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
    // Creating the DB with a DDL query.
    override fun onCreate(db: SQLiteDatabase?) {
        //Creating table with these attributes.
        val createTable = ("CREATE TABLE " + TABLE_PLACE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(createTable)
    }
    // Upgrading the DB if the structure of DB changes.
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PLACE")
        onCreate(db)
    }
    // A DML query that add a place to the DB.
    fun addPlace(place: PlaceModel): Long {
        // We need to write on the database.
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, place.title) // PlaceModelClass TITLE.
        contentValues.put(KEY_IMAGE, place.image) // PlaceModelClass IMAGE.
        contentValues.put(
                KEY_DESCRIPTION,
                place.description
        ) // PlaceModelClass DESCRIPTION.
        contentValues.put(KEY_DATE, place.date) // PlaceModelClass DATE.
        contentValues.put(KEY_LOCATION, place.location) // PlaceModelClass LOCATION.
        contentValues.put(KEY_LATITUDE, place.latitude) // PlaceModelClass LATITUDE.
        contentValues.put(KEY_LONGITUDE, place.longitude) // PlaceModelClass LONGITUDE.

        // Inserting Row by the DML query.
        val result = db.insert(TABLE_PLACE, null, contentValues)
        //2nd argument is String containing nullColumnHack  .
        // Closing database connection.
        db.close()

        return result
    }

    // A DML query that update an existing place to the DB.
    fun updatePlace(place: PlaceModel): Int {
        // We need to write on the database.
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, place.title) // PlaceModelClass TITLE.
        contentValues.put(KEY_IMAGE, place.image) // PlaceModelClass IMAGE.
        contentValues.put(
            KEY_DESCRIPTION,
            place.description
        ) // PlaceModelClass DESCRIPTION.
        contentValues.put(KEY_DATE, place.date) // PlaceModelClass DATE.
        contentValues.put(KEY_LOCATION, place.location) // PlaceModelClass LOCATION.
        contentValues.put(KEY_LATITUDE, place.latitude) // PlaceModelClass LATITUDE.
        contentValues.put(KEY_LONGITUDE, place.longitude) // PlaceModelClass LONGITUDE.

        // Updating Row by the DML query.
        val success = db.update(
            TABLE_PLACE,
            contentValues,
            KEY_ID + "=" + place.id, null)
        // Closing database connection.
        db.close()

        return success
    }
    // Note: we have two different methods (add/updatePlace) to avoid duplicate entries when
    // user wants to edit a place, because with only the add one there always be inserted a new row.

    // Get all the rows from the DB.
    fun getPlacesList(): ArrayList <PlaceModel>{
        val placeList = ArrayList<PlaceModel>()
        val selectedQuery = "SELECT * FROM $TABLE_PLACE"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectedQuery, null)

            // Run through the whole list of entries that there are.
            if(cursor.moveToFirst()) {
                do {
                    // Getting a place.
                    val place = PlaceModel(
                            cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                            cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                            cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                            cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                            cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                            cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    // Adding it to the list that we want to return.
                    placeList.add(place)
                }while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectedQuery)
            return ArrayList()
        }

        return placeList
    }

    // Deletes a row from the DB.
    fun deletePlace(place: PlaceModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_PLACE, KEY_ID + "=" + place.id, null)
        db.close()

        return success
    }
}