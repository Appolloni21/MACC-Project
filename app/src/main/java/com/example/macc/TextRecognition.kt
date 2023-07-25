package com.example.macc

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.macc.databinding.TextRecognitionBinding
import com.example.macc.viewmodel.PriceViewModel
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.lang.Exception


class TextRecognition : Fragment() {

    // UI views
    private lateinit var inputImageBtn: MaterialButton
    private lateinit var recognizeTextBtn: MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var recognizedTextEt: EditText
    private lateinit var takeThePriceBtn : MaterialButton
    var textImport = "0"
    private val viewModel: PriceViewModel by activityViewModels()


    private companion object{
        //to handle the result of Camera/Gallery permissions in onRequestPermissionResults
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri: Uri? = null
    //arrays of permission required to pick image from Camera/gallery
    private lateinit var cameraPermissions : Array<String>
    private lateinit var storagePermissions: Array<String>
    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecognizer: TextRecognizer

    private var _binding: TextRecognitionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = TextRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //init UI views
        inputImageBtn = binding.inputImageBtn
        recognizeTextBtn = binding.recognizeTextBtn
        imageIv = binding.imageIv
        recognizedTextEt = binding.recognizedTextEt
        takeThePriceBtn = binding.takeThePrice

        //init arrays of permissions required for camera, Gallery
        cameraPermissions= arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init setup the progress dialog, show while text from image is being recognized
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init textrecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


        takeThePriceBtn.setOnClickListener {
            if(textImport == "0"){
                showToast("Recognize the test from image first...")
            }
            else{
                viewModel.selectedItem(textImport)
                showToast("The price has been saved, now you can load it")

            }
        }
        //handle click, show input image dialog
        inputImageBtn.setOnClickListener{
            //showInputImageDialog()
            if (checkCameraPermissions()){
                //pickImageCamera()
            }
            else{
                requestCameraPermissions()
            }
        }

        recognizeTextBtn.setOnClickListener {

            if(imageUri == null){
                showToast("Pick image first...")
            }
            else{
                recognizeTextFromImage()
            }
        }
    }
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.text_recognition)

        //init UI views
        inputImageBtn = findViewById(R.id.inputImageBtn)
        recognizeTextBtn = findViewById(R.id.recognizeTextBtn)
        imageIv = findViewById(R.id.imageIv)
        recognizedTextEt = findViewById(R.id.recognizedTextEt)
        takeThePriceBtn = findViewById(R.id.takeThePrice)

        //init arrays of permissions required for camera, Gallery
        cameraPermissions= arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        //init setup the progress dialog, show while text from image is being recognized
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init textrecognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


        takeThePriceBtn.setOnClickListener {
            if(textImport == "0"){
                showToast("Recognize the test from image first...")
            }
            else{
                viewModel.selectedItem(textImport)
                showToast("The price has been saved, now you can load it")

            }
        }
        //handle click, show input image dialog
        inputImageBtn.setOnClickListener{
            //showInputImageDialog()
            if (checkCameraPermissions()){
                //pickImageCamera()
            }
            else{
                requestCameraPermissions()
            }
        }

        recognizeTextBtn.setOnClickListener {

            if(imageUri == null){
                showToast("Pick image first...")
            }
            else{
                recognizeTextFromImage()
            }
        }
    }*/

    private fun recognizeTextFromImage() {
        progressDialog.setMessage("preparing image...")
        progressDialog.show()

        try {
            val inputImage = InputImage.fromFilePath(requireContext(), imageUri!!)

            progressDialog.setMessage("Recognizing test...")

            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener {text ->
                    progressDialog.dismiss()

                    val recognizedText = text.text
                    textImport = recognizedText

                    recognizedTextEt.setText(recognizedText)
                }
                .addOnFailureListener {e->

                    progressDialog.dismiss()
                    showToast("failed to recognize text due to ${e.message}")

                }
        }
        catch (e: Exception){
            progressDialog.dismiss()
            showToast("Failed to prepare image due to ${e.message}")
        }

    }

    private fun showInputImageDialog() {
        //init popup menu param 1 is context, param 2 is UI view where you want to show popup menu
        val popupMenu = PopupMenu(requireContext(), inputImageBtn)

        //Add items camera, gallery to popup menu, parm2 is menu id, param 3 is position of this menu item in menu items list, param 4 is title of the menu
        popupMenu.menu.add(Menu.NONE, 1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE, 2, 2, "GALLERY")

        //show pop up menu
        popupMenu.show()

        //handle popupmenu item clicks
        popupMenu.setOnMenuItemClickListener {menuItem ->
            //get item id that is clicked from popupmenu
            val id = menuItem.itemId
            if( id == 1){
                //pickImageCamera()
                //camera is clicked
                if (checkCameraPermissions()){
                    //pickImageCamera()
                }
                else{
                    requestCameraPermissions()
                }
            }
            else if(id == 2){
                //pickImageGallery()
                //gallery is clicked
                if(checkStoragePermission()){
                    pickImageGallery()
                }
                else{
                    requestStoragePermission()
                }
            }

            return@setOnMenuItemClickListener true

        }

    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->

            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                imageIv.setImageURI(imageUri)
            }
            else {
                showToast("Cancelled...")
            }
        }

    private fun pickImageCamera() {
        //get ready the image data to store in MediaStore
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")
        //image uri
        imageUri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        //intent to launch camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher=
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            //here we will receive the image, if taken from camera
            if(result.resultCode == Activity.RESULT_OK){
                //image is taken from camera
                //we already have the image in imageUri using function pickImageCamera
                imageIv.setImageURI(imageUri)
            }
            else{
                //cancelled
                showToast("Cancelled...!")
            }

        }


    private fun checkStoragePermission() : Boolean{
        //check if storage permission is allowed or not: return true if allowed, false if it is not
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermissions() : Boolean{
        //check uf camera and storage permission are allowed
        val cameraResult = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        //request storage permission for gallery image pick
        ActivityCompat.requestPermissions(requireContext() as Activity, storagePermissions, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermissions(){
        //request camera permissions for camera intent
        ActivityCompat.requestPermissions(requireContext() as Activity, cameraPermissions, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //handle permissions result
        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                // check if some action from permission dialog performed or not allow
                if(grantResults.isNotEmpty()){
                    //check if camera storage permissions granted, contains boolean resutls either true or false
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    //val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    //check if both permissions are granted or not
                    if(cameraAccepted) {
                        //both permission granted, launch camera
                        pickImageCamera()
                    }
                    else{
                        showToast("Camera permission are required...")
                    }
                }
            }
            STORAGE_REQUEST_CODE ->{
                //check if some action from permission dialog performed or not
                if(grantResults.isNotEmpty()){
                    //check if storage permission granted, contains boolean results either true or false
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    //check if storage permission is granted or not

                    if(storageAccepted){
                        //if granted, go picking from gallery
                        pickImageGallery()
                    }
                    else{
                        showToast("Storage permission is required...")
                    }
                }
            }
        }

    }

    private fun showToast(message: String){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}