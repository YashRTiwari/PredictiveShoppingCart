package tech.yashtiwari.captureit


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.io.File
import java.nio.ByteBuffer
import java.util.Collections.max
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CameraFragment : Fragment() {

    private var model: Communicator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        viewFinder = view.findViewById(R.id.view_finder)
        button = view.findViewById<ImageButton>(R.id.capture_button)

        // Request camera permissions
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                    activity!!, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Every time the provided texture view changes, recompute layout
//        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//            updateTransform()
//        }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model = ViewModelProviders.of(activity!!).get(Communicator::class.java)

    }


    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView
    private lateinit var button: ImageButton
    private val RATIO_4_3_VALUE = 4.0 / 3.0
    private val RATIO_16_9_VALUE = 16.0 / 9.0
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        //  Implement CameraX operations
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().build()


        // Build the viewfinder use case
        val preview = AutoFitPreviewBuilder.build(previewConfig, viewFinder)


        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                }.build()

        // Build the image capture use case and attach button click listener
        val imageCapture = ImageCapture(imageCaptureConfig)

        button.setOnClickListener {
            val file = File(activity?.externalMediaDirs!!.first(),
                    "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onError(
                                imageCaptureError: ImageCapture.ImageCaptureError,
                                message: String,
                                exc: Throwable?
                        ) {
                            val msg = "Photo capture failed: $message"
                            Log.e("CameraXApp", msg, exc)
                            viewFinder.post {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onImageSaved(file: File) {
                            val msg = "Photo capture succeeded: ${file.absolutePath}"
                            Log.d("CameraXApp", file.absolutePath)

                            viewFinder.post {
                                model!!.sendMsgCommunicator(file.absolutePath)

                                val fragment = ImagePreviewFragment()
                                val manager = activity!!.supportFragmentManager
                                val transaction = manager.beginTransaction()
                                transaction.replace(R.id.frame, fragment)
                                transaction.addToBackStack(null)
                                transaction.commit()

                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
        }

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }



    private fun aspectRatio(width: Int, height: Int): AspectRatio {
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }


    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(context,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                activity!!.baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


}
