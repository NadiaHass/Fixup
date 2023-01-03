package com.nadiahassouni.fixup.ui.client

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.Adapters.AgentAdapter
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentHomeClientBinding
import com.nadiahassouni.fixup.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeClientFragment : Fragment() {
    private lateinit var agentsList: ArrayList<User>
    private lateinit var currentUser : User
    private var _binding: FragmentHomeClientBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeClientBinding.inflate(inflater, container, false)

        showProgressBar()
        getAgents()
        getProfilePicture()

        binding.ivProfile.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("userInfo" , currentUser)
            Navigation.findNavController(binding.root).navigate(R.id.action_homeClientFragment_to_profileFragment2 , bundle)
        }

        return binding.root
    }

    private fun getAgents() = CoroutineScope(Dispatchers.IO).launch {
        var agent: User?

        try {
            val usersCollectionRef = Firebase.firestore.collection("users")
            val querySnapshot = usersCollectionRef
                .whereEqualTo("role" , "agent")
                .get().await()
            var list = ArrayList<User>()
            for (doc in querySnapshot.documents) {
                agent = doc.toObject<User>()
                list.add(agent!!)
            }
            withContext(Dispatchers.Main) {
                agentsList = list
                val adapter = AgentAdapter(requireContext(), agentsList )
                adapter.notifyDataSetChanged()
                binding.rvAgentProfiles.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                binding.rvAgentProfiles.adapter = adapter
                hideProgressBar()
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