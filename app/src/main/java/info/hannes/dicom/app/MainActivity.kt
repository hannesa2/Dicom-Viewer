package info.hannes.dicom.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.Target
import com.github.amlcurran.showcaseview.targets.ViewTarget
import com.ipaulpro.afilechooser.utils.FileUtils
import info.hannes.dicom.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("imebra_lib")

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Instantiate the list of samples.
        instantiateList()
        binding.chooseFileButton.setOnClickListener { // Perform action on click
            Toast.makeText(this@MainActivity, "choose file", Toast.LENGTH_SHORT).show()
            showChooser()
        }
        val viewTarget: Target = ViewTarget(R.id.choose_file_button, this)
        ShowcaseView.Builder(this, true).setTarget(viewTarget).setContentTitle(R.string.title_single_shot)
            .setContentText(R.string.R_string_desc_single_shot).singleShot(42).build()

        binding.list.setOnItemClickListener { adapterView, _, position, l ->         // Launch the sample associated with this list position.
            val intent = Intent(this@MainActivity, mSamples!![position].activityClass)
            intent.putExtra("patient_name", mSamples!![position].title)
            startActivity(intent)
        }
    }

    private fun instantiateList() {
        val patients = Patients.getInstance().allPatients
        val samples: MutableList<Sample> = ArrayList()
        for (patient in patients.values) {
            samples.add(Sample(patient.name, MedicalTestListActivity::class.java))
        }
        mSamples = samples.toTypedArray()
        val listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, mSamples!!)
        binding.list.adapter = listAdapter
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE ->                 // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    // Get the URI of the selected file
                    val uri = data?.data
                    Log.i(TAG, "Uri = " + uri.toString())
                    try {
                        // Get the file path from the URI
                        val path = FileUtils.getPath(this, uri)
                        Toast.makeText(
                            this@MainActivity, "File Selected: $path", Toast.LENGTH_LONG
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