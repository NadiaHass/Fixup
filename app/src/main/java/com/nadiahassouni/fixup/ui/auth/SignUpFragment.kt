package com.nadiahassouni.fixup.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentSignUpBinding
import com.nadiahassouni.fixup.models.User
import com.nadiahassouni.fixup.models.Wilayas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {
    private var _binding : FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val genderArrayList : ArrayList<String> = ArrayList()
    private val wilayaArrayList : ArrayList<String> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater , container , false)

        binding.btnSignUp.setOnClickListener {
            signUpNewUser()
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

    private fun signUpNewUser() {
        if(checkInputNotEmpty()){
            showProgressBar()
            addUserToFirebase(binding.etEmail.text.toString() , binding.etPassword.text.toString())
        }else{
            Toast.makeText(context , " Veillez complétez tous les champs !" , Toast.LENGTH_LONG).show()
        }
    }

    private fun addUserToFirebase(email : String, password : String) {
        try {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = getUser()
                        sendVerificationEmail()
                        storeUserProfileData(user)
                        hideProgressBar()
                        startActivity(Intent(requireContext() , AuthenticationActivity::class.java))
                        activity?.finish()
                    } else {
                        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        hideProgressBar()
                    }
                }
        }catch (e : Exception){
            hideProgressBar()
        }
    }

    private fun getUser(): User {
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val role = arguments?.getString("role").toString()
        val gender = binding.sexeSpinner.selectedItem.toString()
        if(gender == "Homme")
          return User(userId,
            binding.etName.text.toString(),
            binding.etSurname.text.toString(),
            gender ,
            "https://firebasestorage.googleapis.com/v0/b/fixup-da1a2.appspot.com/o/man.png?alt=media&token=3cecbb3d-258e-42c7-8902-d190ba9970ae" ,
            binding.etEmail.text.toString(),
            binding.etPhone.text.toString() ,
            binding.wilayaSpinner.selectedItem.toString() ,
            binding.etDescription.text.toString() ,
        role)
        else
            return User(userId,
                binding.etName.text.toString(),
                binding.etSurname.text.toString(),
                gender ,
                "https://firebasestorage.googleapis.com/v0/b/fixup-da1a2.appspot.com/o/woman.png?alt=media&token=07d0342e-7e8a-42b6-87b7-7e9a6ba07b2d" ,
                binding.etEmail.text.toString(),
                binding.etPhone.text.toString() ,
                binding.wilayaSpinner.selectedItem.toString() ,
                binding.etDescription.text.toString() ,
                role)
    }

    private fun storeUserProfileData(user: User) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userCollectionRef = Firebase.firestore.collection("users").document(user.id)
            userCollectionRef.set(user).await()
            FirebaseAuth.getInstance().signOut()
            navigateToSignInFragment()
        }catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context , e.message , Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendVerificationEmail() {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            ?.addOnCompleteListener(requireActivity()){
                if (it.isSuccessful){
                    Toast.makeText(context , "Email de verification est envoyee " , Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context , "Echec de l'envoi du l'email de verification" , Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun checkInputNotEmpty(): Boolean {
        return (binding.etName.text.toString() != "" &&
                binding.etSurname.text.toString() != "" &&
                binding.etEmail.text.toString() != "" &&
                binding.etPassword.text.toString() != "")
    }

    private fun navigateToSignInFragment() {
        Navigation.findNavController(binding.root).navigate(R.id.action_signInFragment_to_usersTypesFragment)
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignUp.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.btnSignUp.visibility = View.VISIBLE
    }
}