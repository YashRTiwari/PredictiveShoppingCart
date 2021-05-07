package tech.yashtiwari.captureit

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import tech.yashtiwari.captureit.Database.Product
import tech.yashtiwari.captureit.Database.ProductViewModel
import tech.yashtiwari.captureit.GraphicUtils.GraphicOverlay
import tech.yashtiwari.captureit.GraphicUtils.GraphicOverlay.Graphic
import tech.yashtiwari.captureit.GraphicUtils.TextGraphic
import java.io.File
import java.io.IOException


class ImagePreviewFragment : Fragment() {

    var filepath : String? = null
    //lateinit var textView : TextView
    lateinit var mGraphicOverlay: GraphicOverlay
    var maxWidth = Resources.getSystem().displayMetrics.widthPixels.toDouble()
    var maxHeight = Resources.getSystem().displayMetrics.heightPixels.toDouble()
    lateinit var bitmap :Bitmap
    lateinit var rvProducts: RecyclerView
    private val compositeDisposable = CompositeDisposable()
    lateinit var rvAdapterProducts: RVProducts
    lateinit var ivImage : ImageView

    lateinit var productViewModal: ProductViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_image_preview, container, false)
        //textView = view.findViewById(R.id.bill) as TextView
        mGraphicOverlay = view.findViewById(R.id.graphicOverlay) as GraphicOverlay
        ivImage = view.findViewById(R.id.image) as ImageView
        Log.d("metrics", "${maxWidth}, ${maxHeight}")

        rvProducts =  view.findViewById(R.id.rvProducts) as RecyclerView
        productViewModal = ViewModelProviders.of(this).get(ProductViewModel::class.java)
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        rvProducts.setHasFixedSize(true);

        // use a linear layout manager
        var layoutManager = LinearLayoutManager(context);
        rvProducts.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)


        return view;
    }

    private fun attachObservableToBitmap(imageBitmap: Bitmap, rotation: Int) {
        Log.d("metrics", "2width: ${imageBitmap.width}, height: ${imageBitmap.height}")

        val bitmapObservable = Observable.just(imageBitmap)
        bitmapObservable.subscribeOn(Schedulers.io())
                .map<Bitmap> { scaleBitmap(imageBitmap, rotation) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : io.reactivex.Observer<Bitmap> {
                    override fun onSubscribe(d: Disposable) {
                        compositeDisposable.add(d)
                    }

                    @RequiresApi(Build.VERSION_CODES.N)
                    override fun onNext(bitmap: Bitmap) {

                        val layoutParams = mGraphicOverlay.getLayoutParams() as FrameLayout.LayoutParams
                        layoutParams.height = (bitmap.height.toDouble()  * (maxWidth / bitmap.width.toDouble() )).toInt()
                        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                        mGraphicOverlay.layoutParams = layoutParams

                        ivImage.layoutParams = layoutParams

                        Glide.with(this@ImagePreviewFragment)
                                .load(bitmap)
                                .fitCenter()
                                .into(ivImage);

                        val image: FirebaseVisionImage
                        Log.d("metrics", "2width: ${bitmap.width}, height: ${bitmap.height}")
                        image = FirebaseVisionImage.fromBitmap(bitmap)
                        recognizeTextCloud(image)
                    }

                    override fun onError(e: Throwable) {
                        Log.d("metrics", e.localizedMessage)
                        e.printStackTrace()
                    }

                    override fun onComplete() {
                    }
                })
    }

    fun scaleBitmap(bitmap: Bitmap, rotation: Int): Bitmap{

        var width = bitmap.width.toDouble()
        var height = bitmap.height.toDouble()

        var ratio = 0.0

        if ((maxWidth == width) and (maxHeight == height)) {
            return bitmap
        }

        if (width > maxWidth){
            ratio = (maxWidth/width).toDouble()
        } else {
            ratio = (width/maxWidth).toDouble()
        }

        width = maxWidth
        height = (ratio*height)

        if (height > maxHeight)
            height = maxHeight

        Log.d("metrics", "Scaled: ${width}, ${height}")



        val matrix = Matrix()

        matrix.postRotate(rotation.toFloat())

        var bm = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
        var rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);



        return rotatedBitmap
    }

    fun getCameraPhotoOrientation(context: Context, imagePath: String?): Int {
        var rotate = 0
        try {
            val imageFile = File(imagePath)
            val exif = ExifInterface(imageFile.getAbsolutePath())
            val orientation: Int = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
            Log.i("RotateImage", "Exif orientation: $orientation")
            Log.i("RotateImage", "Rotate value: $rotate")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rotate
    }

    private fun getBlocks(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Log.d("OCR_BLK", "getBlocks: null")
            return
        }
        for (i in blocks) {
            Log.d("OCR_BLK", i.text)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model= ViewModelProviders.of(activity!!).get(Communicator::class.java)

        model.message.observe(this, object : Observer<Any> {
            override fun onChanged(o: Any?) {
               filepath = o.toString()
                Toast.makeText(context, filepath, Toast.LENGTH_SHORT).show()

                if(filepath != null) {

                    try {

                        var rotation = getCameraPhotoOrientation(context!!, filepath)
                        val bmOptions = BitmapFactory.Options()
                        bitmap = BitmapFactory.decodeFile(filepath, bmOptions)
                        attachObservableToBitmap(bitmap, rotation)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        })

    }


    @RequiresApi(Build.VERSION_CODES.N)
    public fun recognizeTextCloud(image: FirebaseVisionImage) {

        val options = FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setModelType(FirebaseVisionCloudTextRecognizerOptions.DENSE_MODEL)
                .setLanguageHints(listOf("en", "hi"))
                .build()


        val detector = FirebaseVision.getInstance().getCloudTextRecognizer(options)

        //val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        val result = detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->

                    //getLines(firebaseVisionText)
                    //getBlocks(firebaseVisionText)
                    processTextBlock(firebaseVisionText)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_LONG).show()
                }

    }

    private fun getLines(texts: FirebaseVisionText) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Log.d("OCR", "getLines: NULL")
            return
        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines) {
                Log.d("OCR_LINES" , j.text)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun processTextBlock(result: FirebaseVisionText) {

        var blockT = ""
        var linesT = ""
        var wordsT = ""
        val resultText = result.text
        mGraphicOverlay.clear()
        var listOfBlocks = result.textBlocks
        if (listOfBlocks.size == 0){
            Toast.makeText(context, "No text found", Toast.LENGTH_LONG).show()
            return
        }

        var list : List<TextProcessor.P> = TextProcessor.processBill(result);
        rvAdapterProducts = RVProducts(list);
        rvProducts.setAdapter(rvAdapterProducts);

        for(x in list){
            var prd = Product(x.text)
            productViewModal.insert(prd)
        }
    }




}
