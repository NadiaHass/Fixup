package com.nadiahassouni.fixup.ui.client

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.databinding.FragmentAddReclamationBinding
import com.nadiahassouni.fixup.models.Reclamation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddReclamationFragment : Fragment() {
    private var _binding: FragmentAddReclamationBinding? = null
    private val binding get() = _binding!!
    private var reclamationId : String = ""
    private val problemsArrayList : ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddReclamationBinding.inflate(inflater, container, false)

        val agentId = arguments?.getString("agentId")
        val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())

        binding.btnAddReclamation.setOnClickListener {
            showProgressBar()
            val reclamation = Reclamation("" , FirebaseAuth.getInstance().currentUser?.uid!! , agentId!! , binding.etTitle.text.toString() ,
            binding.problemSpinner.selectedItem.toString(), binding.etDescription.text.toString() , sdf , "" )
            addReclamation(reclamation)
        }

        problemsArrayList.add("Telephone")
        problemsArrayList.add("Modem")
        problemsArrayList.add("Camera")
        problemsArrayList.add("Iptv")

        val adapter = ArrayAdapter<String>(requireContext() , R.layout.simple_spinner_dropdown_item , problemsArrayList )
        binding.problemSpinner.adapter = adapter

        return binding.root
    }

    private fun addReclamation(reclamation : Reclamation) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val reclamationCollectionRef = Firebase.firestore.collection("reclamations")
            reclamationId = reclamationCollectionRef.document().id
            reclamation.id = reclamationId
            reclamationCollectionRef.add(reclamation).await()
            withContext(Dispatchers.Main){
                Toast.makeText(context ,"La reclamation a ete ajoute" , Toast.LENGTH_LONG).show()
                hideProgressBar()
                binding.btnAddReclamation.visibility = View.VISIBLE

                startActivity(Intent(activity , ClientActivity::class.java))
                activity?.finish()

            }
        }catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context , e.message , Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}