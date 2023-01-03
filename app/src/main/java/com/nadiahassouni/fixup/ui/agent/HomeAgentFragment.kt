package com.nadiahassouni.fixup.ui.agent

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.Adapters.ReclamationAdapter
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentHomeAgentBinding
import com.nadiahassouni.fixup.models.Reclamation
import com.nadiahassouni.fixup.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeAgentFragment : Fragment() {
    private lateinit var mActivity: FragmentActivity
    private lateinit var reclamations: ArrayList<Reclamation>
    private lateinit var currentUser : User
    private var _binding: FragmentHomeAgentBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeAgentBinding.inflate(inflater, container, false)

        mActivity = activity!!

        showProgressBar()
        getReclamations()
        getProfilePicture()

        binding.ivProfile.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("userInfo" , currentUser)
            Navigation.findNavController(binding.root).navigate(R.id.action_homeClientFragment_to_profileFragment , bundle)
        }

        return binding.root
    }

    private fun getReclamations() = CoroutineScope(Dispatchers.IO).launch {
        var reclamation: Reclamation?

        try {
            val usersCollectionRef = Firebase.firestore.collection("reclamations")
            val querySnapshot = usersCollectionRef
                .whereEqualTo("state" , "valide")
                .whereEqualTo("agentId" , FirebaseAuth.getInstance().currentUser?.uid)
                .get().await()
            var list = ArrayList<Reclamation>()
            for (doc in querySnapshot.documents) {
                reclamation = doc.toObject<Reclamation>()
                list.add(reclamation!!)
            }
            withContext(Dispatchers.Main) {
                reclamations = list
                val adapter = ReclamationAdapter(requireContext(), reclamations , "agent" , mActivity)
                adapter.notifyDataSetChanged()
                binding.rvReclamations.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvReclamations.adapter = adapter
                hideProgressBar()

                if(reclamations.size==0)
                    binding.ivEmpty.visibility = View.VISIBLE
                else
                    binding.ivEmpty.visibility = View.GONE

            }

        } catch (e: java.lang.Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getProfilePicture()  = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userCollectionRef = Firebase.firestore.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid!!)
            val querySnapshot = userCollectionRef.get().await()
            val user : User? = querySnapshot.toObject<User>()
            withContext(Dispatchers.Main){
                if(user?.imageUrl != "")
                    Glide.with(requireContext())
                        .load(Uri.parse(user?.imageUrl))
                        .into(binding.ivProfile)

                currentUser = User(user?.id!! , user.name , user.surname , user.sexe , user.imageUrl , user.email ,
                    user.tel , user.wilaya , user.description , user.role)
            }
        }catch (e : Exception){

        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }


}