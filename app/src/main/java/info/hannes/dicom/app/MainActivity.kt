package info.hannes.dicom.app

import android.app.ListActivity
import android.os.Bundle
import android.widget.Toast
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.github.amlcurran.showcaseview.ShowcaseView
import android.widget.ArrayAdapter
import android.content.Intent
import android.content.ActivityNotFoundException
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.core.content.ContextCompat.startActivity
import com.github.amlcurran.showcaseview.targets.Target
import com.ipaulpro.afilechooser.utils.FileUtils
import info.hannes.github.AppUpdateHelper
import java.lang.Exception
import java.util.ArrayList

class MainActivity : ListActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("imebra_lib")
        setContentView(R.layout.activity_main)

        // Instantiate the list of samples.
        instantiateList()
        val button = findViewById<Button>(R.id.choose_file_button)
        button.setOnClickListener { // Perform action on click
            Toast.makeText(this@MainActivity, "choose file", Toast.LENGTH_SHORT).show()
            showChooser()
        }
        val viewTarget: Target = ViewTarget(R.id.choose_file_button, this)
        ShowcaseView.Builder(this, true)
            .setTarget(viewTarget)
            .setContentTitle(R.string.title_single_shot)
            .setContentText(R.string.R_string_desc_single_shot)
            .singleShot(42)
            .build()

        AppUpdateHelper.checkForNewVersion(
            this@MainActivity,
            BuildConfig.GIT_REPOSITORY,
            { msg -> Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
        )
    }

    private fun instantiateList() {
        val patients = Patients.getInstance().allPatients
        val samples: MutableList<Sample> = ArrayList()
        for (patient in patients.values) {
            samples.add(Sample(patient.name, MedicalTestListActivity::class.java))
        }
        mSamples = samples.toTypedArray()
        listAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            android.R.id.text1,
            mSamples!!
        )
    }

    override fun onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
        // Launch the sample associated with this list position.
        val intent = Intent(this@MainActivity, mSamples!![position].activityClass)
        intent.putExtra("patient_name", mSamples!![position].title)
        startActivity(intent)
    }

    private fun showChooser() {
        // Use the GET_CONTENT intent from the utility class
        val target = FileUtils.createGetContentIntent()
        // Create the chooser Intent
        val intent = Intent.createChooser(target, getString(R.string.chooser_title))
        try {
            startActivityForResult(intent, REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            // The reason for the existence of aFileChooser
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_CODE ->                 // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    // Get the URI of the selected file
                    val uri = data.data
                    Log.i(TAG, "Uri = " + uri.toString())
                    try {
                        // Get the file path from the URI
                        val path = FileUtils.getPath(this, uri)
                        Toast.makeText(
                            this@MainActivity,
                            "File Selected: $path", Toast.LENGTH_LONG
                        ).show()
                        Patients.getInstance().addPatient(path, this)
                        instantiateList()
                    } catch (e: Exception) {
                        Log.e("FileSelectorTest", "File select error", e)
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val REQUEST_CODE = 6384 // onActivityResult request
        private const val TAG = "FileChooserExample"

        /**
         * The collection of all samples in the app. This gets instantiated in [ ][.onCreate] because the [Sample] constructor needs access to [ ].
         */
        private var mSamples: Array<Sample>? = null
    }
}