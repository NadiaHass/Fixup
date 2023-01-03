package com.nadiahassouni.fixup.ui.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.databinding.FragmentReclamationBinding
import com.nadiahassouni.fixup.models.Reclamation
import com.nadiahassouni.fixup.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ReclamationFragment : Fragment() {
    private var _binding: FragmentReclamationBinding? = null
    private val binding get() = _binding!!
    private  var phoneNum : String = ""
    private  var email : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReclamationBinding.inflate(inflater, container, false)

        val reclamation = arguments?.getSerializable("reclamation") as Reclamation

        getUser(reclamation.clientId)

        binding.ivEmail.setOnClickListener {
            val emailList = arrayOf(email)
            composeEmail(emailList , "Concernant la reclamation")
        }

        binding.ivPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNum))
            startActivity(intent)
        }

        binding.tvDescription.text = reclamation.description
        binding.tvProblem.text = reclamation.problemType
        binding.tvTitle.text = reclamation.title
        binding.tvDate.text = reclamation.date

        return binding.root
    }

    fun composeEmail(addresses: Array<String>, subject: String?) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            startActivity(intent)
    }

    private fun getUser(clientId: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userCollectionRef = Firebase.firestore.collection("users").document(clientId)
            val querySnapshot = userCollectionRef.get().await()
            val user : User? = querySnapshot.toObject<User>()


            withContext(Dispatchers.Main){
                if(user!!.imageUrl != "")
                    Glide.with(requireContext())
                        .load(user.imageUrl)
                        .into(binding.ivProfile)

                binding.tvGender.text = user.sexe
                binding.tvName.text = user.name
                binding.tvSurname.text = user.surname
                binding.tvWilaya.text = user.wilaya
                binding.tvPhone.text = user.tel

                phoneNum = user.tel
                email = user.email

            }
        }catch (e : Exception){

        }
    }
}
