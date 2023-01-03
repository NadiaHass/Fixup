package com.nadiahassouni.fixup.ui.client

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.Adapters.ReclamationAdapter
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentProfileReclamationsBinding
import com.nadiahassouni.fixup.models.Reclamation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileReclamationsFragment : Fragment() {
    private lateinit var mActivity: FragmentActivity
    private var _binding: FragmentProfileReclamationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reclamations: ArrayList<Reclamation>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        _binding = FragmentProfileReclamationsBinding.inflate(inflater, container, false)

        val agentId = arguments?.getString("agent")

        mActivity = activity!!

        binding.fabAddReclamation.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("agentId" , agentId)
            Navigation.findNavController(binding.root).navigate(R.id.action_profileReclamationsFragment_to_addReclamationFragment , bundle)
        }

        showProgressBar()
        getReclamations(agentId)

        return binding.root
    }

    private fun getReclamations(agentId: String?) = CoroutineScope(Dispatchers.IO).launch {
        var reclamation: Reclamation?

        try {
            val usersCollectionRef = Firebase.firestore.collection("reclamations")
            val querySnapshot = usersCollectionRef
                .whereEqualTo("state" , "valide")
                .whereEqualTo("agentId" , agentId )
                .whereEqualTo("clientId" , FirebaseAuth.getInstance().currentUser?.uid)
                .get().await()
            var list = ArrayList<Reclamation>()
            for (doc in querySnapshot.documents) {
                reclamation = doc.toObject<Reclamation>()
                list.add(reclamation!!)
            }
            withContext(Dispatchers.Main) {
                reclamations = list
                val adapter = ReclamationAdapter(requireContext(), reclamations, "client" , mActivity)
                adapter.notifyDataSetChanged()
                binding.rvReclamations.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvReclamations.adapter = adapter

                if(reclamations.size==0)
                    binding.ivEmpty.visibility = View.VISIBLE
                else
                    binding.ivEmpty.visibility = View.GONE
                hideProgressBar()
            }

        } catch (e: java.lang.Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
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