package com.nicolamcornelio.placesintheheart.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nicolamcornelio.placesintheheart.R
import com.nicolamcornelio.placesintheheart.database.DatabaseHandler
import com.nicolamcornelio.placesintheheart.models.PlaceModel
import com.nicolamcornelio.placesintheheart.utils.GetAddressFromLatLng
import kotlinx.android.synthetic.main.activity_add_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

// Our class activity has to inherits from OCL because we need to know when user wants to pick a date.
class AddPlaceActivity : AppCompatActivity(), View.OnClickListener {

    // We need an instance of class Calendar, in order to choose a dare.
    private var cal = Calendar.getInstance()
    // We use this attribute to pick the date from the calendar.
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    // The location of the saved image.
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    // Used for store the details of a place.
    private var mPlaceDetails: PlaceModel? = null
    // Used for the current location.
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        // Setting up and displaying the toolbar.
        setSupportActionBar(toolbar_add_place)
        // Add the back button.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Implement the listener for the back button.
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }
        // We initialize it in the requestNewLocationData() method.
        //mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initializing Places (Places API tool) if isn't initialized yet.
        if(!Places.isInitialized()) {
            Places.initialize(this@AddPlaceActivity,
                resources.getString(R.string.google_maps_api_key))
        }

        // Check if the intent has some info with this code.
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            // Getting the item.
            mPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as PlaceModel?
        }

        // Date picker is opened and we wait the user to choose a date.
        dateSetListener = DatePickerDialog.OnDateSetListener {
            // Setting the date according to what the user has chosen.
            _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            // When the user has picked the date, we'll update it on the TextField.
            updateDateInView()
        }
        // Calling this outside the OnDateSetListener will set a default date in the box (today).
        updateDateInView()
        // If mPlaceDetails is not null, then we're editing it and not creating a new entry.
        if(mPlaceDetails != null) {
            // So we can edit the place now, after the swipe!
            supportActionBar?.title = "Edit Place in the Heart"
            // Here we have the previous settings.
            et_title.setText(mPlaceDetails!!.title)
            et_description.setText(mPlaceDetails!!.description)
            et_date.setText(mPlaceDetails!!.date)
            et_location.setText(mPlaceDetails!!.location)
            mLatitude = mPlaceDetails!!.latitude
            mLongitude = mPlaceDetails!!.longitude

            // Getting also the image.
            saveImageToInternalStorage = Uri.parse(mPlaceDetails!!.image)
            iv_place_image.setImageURI(saveImageToInternalStorage)
            // Changing the button text of course.
            btn_save.text = "UPDATE"
        }

        // We need also this Listener because we have to "catch" when the user tap on the date box.
        et_date.setOnClickListener(this)
        // Check when user taps on "Add Image" in order to add an image to a place.
        tv_add_image.setOnClickListener(this)
        // Check when the user wants to save a place.
        btn_save.setOnClickListener(this)
        // Check when the user wants to select a location.
        et_location.setOnClickListener(this)
        // Check when the user wants to select the current location.
        tv_select_current_location.setOnClickListener(this)
    }
    // This method checks if permission for location is granted.
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // So this method tells us if we can get the current location of the user or not.
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")  // In order to suppress the perms check in line 152.
    // We suppressed this error because we call this method ONLY when user has grants permissions.
    // Here we'll request the new location of the user.
    private fun requestNewLocationData() {
        // Create a new location request.
        val mLocationRequest = LocationRequest()
        // Define the parameters of the location request:
        // I want that the location is highly accuracy.
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        // Even though the interval is set, we do this request ONLY when user taps the button.
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        // Now we can finally use the Fused Client to request the location updates.
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper())
    }
    // Now we need to implement the mLocationCallback.
    private val mLocationCallback = object: LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val myLastLocation: Location = locationResult!!.lastLocation
            mLatitude = myLastLocation.latitude
            Log.i("Current Latitude", "$mLatitude")
            mLongitude = myLastLocation.longitude
            Log.i("Current Longitude", "$mLongitude")
            // Here now that we have the current location is cords, let's show the address.
            // See GetAddressFromLatLng class.
            // Create an object of Get...FromLatLng and set the its listener to AddressListener.
            val addressTask = GetAddressFromLatLng(
                    this@AddPlaceActivity,
                    mLatitude,
                    mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    // Set the readable address in the view.
                    et_location.setText(address)
                }
                override fun onError() {
                    Log.e("Get Address:: ", "Something went wrong.")
                }
            })
            // It will execute the whole AsyncTask.
            addressTask.getAddress()
        }
    }

    // This is an overridden method is the click listener for what we have said above.
    override fun onClick(v: View?) {
        when(v!!.id) {
            // et_date is the id of the EditText in which there'll be the chosen date.
            R.id.et_date -> {
                DatePickerDialog(this@AddPlaceActivity, dateSetListener,
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            // tv_add_image is the id of the EditText "ADD IMAGE".
            R.id.tv_add_image -> {
                // Building the alert dialog that makes the user choose where to pick the image.
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pickerDialogItems = arrayOf("Select photo from gallery",
                        "Capture photo from camera")
                // Depending on the dialogue which was selected, I want to run some codes.
                pictureDialog.setItems(pickerDialogItems) {
                    // The underscore is not used (In Kotlin an unused variable is the underscore).
                    _, which ->
                    // We handle the permissions with DEXTER, a third library. See the manifest.
                    // Now see if the user wants to pick an image or take a photo with camera.
                    when(which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                // Showing this dialog.
                pictureDialog.show()
            }
            // btn_save is the id of the Button "SAVE".
            R.id.btn_save -> {
                when{
                    // Check if user has entered values in the boxes.
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this,
                                "Please enter a title.", Toast.LENGTH_SHORT).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this,
                                "Please enter a description.", Toast.LENGTH_SHORT).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this,
                                "Please enter a location.", Toast.LENGTH_SHORT).show()
                    }
                    // We don't need to check if the date is empty cause there'll be a default date.
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this,
                                "Please select an image.", Toast.LENGTH_SHORT).show()
                    }else -> {
                    // Else, save the place to the DB.
                    // The id is the PK. It will be always 0 cause it will autoincrement.
                    val placeModel = PlaceModel(
                            // The id here will set according if mPlaceDetails is null or not.
                            if(mPlaceDetails == null) 0 else mPlaceDetails!!.id,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        // We need an object of the our DatabaseHandler class.
                        val dbHandler = DatabaseHandler(this)

                        // This is needed to avoid duplicates items in the TV when editing a place.
                        // Same logic here: if is null, insert a row (we've a new place).
                        if (mPlaceDetails == null) {
                            // Calling the addPlace method and insert the place.
                            val addPlace = dbHandler.addPlace(placeModel)
                            // addPlace() returns a Long, the output value from the INSERT.
                            // Check if the value is greater than zero (e.i. there are no errors).
                            if (addPlace > 0) {
                                // I'll say to the MainActivity that the result is OK! All done!
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(
                                    this,
                                    "Place in the Heart successfully inserted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            // Otherwise we need to update a row, because we want to edit a place.
                        } else {
                            // Calling the addPlace method and update the place.
                            val updatePlace = dbHandler.updatePlace(placeModel)
                            // updatePlace() returns a Int, the output value from the UPDATE.
                            // Check if the value is greater than zero (e.i. there are no errors).
                            if (updatePlace > 0) {
                                // I'll say to the MainActivity that the result is OK! All done!
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(
                                    this,
                                    "Place in the Heart successfully updated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
            R.id.et_location -> {
                // Try to start an intent in order to start the location picker.
                try {
                    // These are the list of fields which we required is passed.
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    // Start the autocomplete intent with a unique request code.
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddPlaceActivity)
                    // This pops up an activity which is the places tool of Google.
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch(e: Exception) {
                    e.printStackTrace()
                }
            }
            R.id.tv_select_current_location -> {
                // If location isn't enabled, display a toast.
                if (!isLocationEnabled()) {
                    Toast.makeText(
                            this,
                            "Your location provider is turned off. Please turn it on.",
                            Toast.LENGTH_SHORT
                    ).show()

                    // Go to the settings in order to make the user grants this permission
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    // Gets the permissions with DEXTER.
                        //withActivity() is deprecated.
                    Dexter.withContext(this)
                            .withPermissions(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            .withListener(object : MultiplePermissionsListener {
                                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                    if (report!!.areAllPermissionsGranted()) {
                                        // Call the new request location function to get the latest
                                            // location.
                                                //At this point we have permissions. See line 140.
                                        requestNewLocationData()
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                        permissions: MutableList<PermissionRequest>?,
                                        token: PermissionToken?
                                ) {
                                    showRationalDialogForPermissions()
                                }
                            }).onSameThread()
                            .check()
                }
            }
        }
    }
    // We need to override this method for handling the result in startActivityForResult().
    // This method is called automatically when we call startActivityForResult().
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // If the result code is OK.
        if (resultCode == Activity.RESULT_OK) {
            // Check if it is the code for pick a photo from the gallery.
            if (requestCode == GALLERY) {
                // Check whether there is some data.
                if (data != null) {
                    // Then get the data.
                    val contentURI = data.data
                    try {
                        // Try to get the photo picked from the gallery (a bitmap).
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                            this.contentResolver, contentURI
                        )
                        // Saving the image internally.
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

                        // Setting the view in the activity with that image.
                        iv_place_image.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {  // Catch an eventual exception and notify the user.
                        e.printStackTrace()
                        Toast.makeText(this@AddPlaceActivity, "An error occurred" +
                                " while loading the image from gallery!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                // Check if it is the code for take a photo with camera.
            } else if (requestCode == CAMERA) {
                // Convert the data (type Intent) into a Bitmap (because we want to take a photo).
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                // Setting the view in the activity with that image.
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")

                iv_place_image.setImageBitmap(thumbnail)
                // Check if it is the code for choose a place with Google Places tool.
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                // Getting the place returned from the intent (the one the user has chosen).
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                // Setting the view in the activity with the address of the location.
                et_location.setText(place.address)
                // Setting also the attributes.
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera() {
        // We use DEXTER to ask for permissions that we need for this action.
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                // We need multiples permissions, READ/WRITE_EXTERNAL_STORAGE and also CAMERA now.
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // After all the permission are granted launch the gallery to select and image.
                    if (report!!.areAllPermissionsGranted()) {
                        // Start the intent: taking a photo with camera.
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        // Since we need a result (the image) we'll use startActivityForResult()
                        // for start the intent, instead of startActivity().
                        startActivityForResult(galleryIntent, CAMERA)
                    } else {
                        // Otherwise pop an alert dialog up, inviting the user to enable it.
                        showRationalDialogForPermissions()
                    }
                }
                // Run this code when user doesn't grant permissions.
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // This line is mandatory or the alert dialog will not work properly.
                    //  // It will keep asking for permissions when user denies them.
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }

    // This method is used to choose a photo from gallery.
    private fun choosePhotoFromGallery() {
        // We use DEXTER to ask for permissions that we need for this action.
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                // We need multiples permissions, READ/WRITE_EXTERNAL_STORAGE.
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // After all the permission are granted launch the gallery to select and image.
                    if (report!!.areAllPermissionsGranted()) {
                        // Start the intent: getting the image from gallery.
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        // Since we need a result (the image) we'll use startActivityForResult()
                        // for start the intent, instead of startActivity().
                        startActivityForResult(galleryIntent, GALLERY)
                    } else {
                        // Otherwise pop an alert dialog up, inviting the user to enable it.
                        showRationalDialogForPermissions()
                    }
                }
                // Run this code when user doesn't grant permissions.
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // This line is mandatory or the alert dialog will not work properly.
                    // It will keep asking for permissions when user denies them.
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }
    // Show to user a dialog that invite the user to grants permissions in the app settings.
    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions required for this feature. " +
                    "It can be enabled under Application Settings.")
            .setPositiveButton("GO TO SETTINGS"
            ) { _, _ ->
                try {
                    // Sending the user to the app's permissions manager.
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") {
                    dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    // This method updates the chosen date in the TextField.
    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }

    // It will return the location of the image that we're storing.
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        // The dir where we store these images will be accessible only by this app.
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        //Every image will have an unique id, an unique name so to speak.
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Compress and save the image into a file.
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
        // Return the path of the file.
        return Uri.parse(file.absolutePath)
    }

    // companion object is useful for constant and static variables. We'll use now for ops codes.
    companion object {
        // Code for start the intent in order to pick an image from gallery.
        private const val GALLERY = 1
        // Code for start the intent in order to take an image with camera.
        private const val CAMERA = 2
        // Here will store the photos taken in the app.
        private const val IMAGE_DIRECTORY = "PlacesImages"
        // Code for start the intent in order to pick a location.
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}