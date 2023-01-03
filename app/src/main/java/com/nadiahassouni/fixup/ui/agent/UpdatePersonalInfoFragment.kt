package com.nadiahassouni.fixup.ui.agent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nadiahassouni.fixup.databinding.FragmentUpdatePersonalInfoBinding
import com.nadiahassouni.fixup.models.User
import com.nadiahassouni.fixup.models.Wilayas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdatePersonalInfoFragment : Fragment() {
    private lateinit var userId: String
    private var _binding : FragmentUpdatePersonalInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageReference : StorageReference
    private val genderArrayList : ArrayList<String> = ArrayList()
    private val wilayaArrayList : ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUpdatePersonalInfoBinding.inflate(inflater , container , false)

        storageReference = FirebaseStorage.getInstance().getReference("Pictures")
        userId = FirebaseAuth.getInstance().currentUser?.uid.toString()


        val user = arguments?.getSerializable("user") as User
        Glide.with(requireContext())
            .load(user.imageUrl)
            .into(binding.ivProfile)
        binding.etName.setText(user.name)
        binding.etSurname.setText(user.surname)
        binding.etDescription.setText(user.description)
        binding.etPhone.setText(user.tel)


        binding.ivAddImage.setOnClickListener {
            showProgressBar()
            openFileChooser()
        }

        binding.btnUpdate.setOnClickListener {
            showProgressBar()
            updateInfo()
        }

        genderArrayList.add("Homme")
        genderArrayList.add("Femme")

        val genderAdapter = ArrayAdapter<String>(requireContext() , android.R.layout.simple_spinner_dropdown_item , genderArrayList )
        binding.sexeSpinner.adapter = genderAdapter

        val wilayas = Wilayas().loadWilayas()
        for (wilaya in wilayas){
            wilayaArrayList.add(wilaya)
        }

        val wilayaAdapter = ArrayAdapter<String>(requireContext() , android.R.layout.simple_spinner_dropdown_item , wilayaArrayList )
        binding.wilayaSpinner.adapter = wilayaAdapter


        return binding.root
    }

    private fun updateInfo()  = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userCollectionRef = Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid!!)
            userCollectionRef.update(
                mapOf("name" to binding.etName.text.toString(),
                    "description" to binding.etDescription.text.toString() ,
                    "surname" to binding.etSurname.text.toString(),
                    "tel" to binding.etPhone.text.toString(),
                    "sexe" to binding.sexeSpinner.selectedItem.toString() ,
                    "wilaya" to binding.wilayaSpinner.selectedItem.toString()
                )
            )
            withContext(Dispatchers.Main){
                hideProgressBar()
                Toast.makeText(requireContext() , "Vos informations ont ete bien modifiee" , Toast.LENGTH_SHORT).show()
            }
        }catch(e : Exception){

        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK ){
            val data : Intent? = result.data
            if (data?.data != null) {
                val uriImage = data.data
                binding.ivProfile.setImageURI(uriImage)
                showProgressBar()
                uploadPictureToStorage(uriImage)
            }
        }
    }

    private fun uploadPictureToStorage(uriImage: Uri?) = CoroutineScope(Dispatchers.IO).launch {
        if(uriImage != null){
            val fileReference = FirebaseAuth.getInstance().currentUser?.let {
                storageReference.child(it.uid + getFileExtension(uriImage))
            }
            fileReference?.putFile(uriImage)?.addOnSuccessListener { l ->
                fileReference.downloadUrl.addOnSuccessListener {
                    val userCollectionRef = Firebase.firestore.collection("users").document(userId)
                    userCollectionRef.update(
                        mapOf( "imageUrl" to it
                        )
                    )
                }
            }
            withContext(Dispatchers.Main){
                hideProgressBar()
                Toast.makeText(requireContext() , "Votre photo de profil a ete bien modifiee" , Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileExtension(uriImage: Uri): String {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(uriImage))!!
    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}