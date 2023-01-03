package com.nadiahassouni.fixup.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.ui.agent.AgentActivity
import com.nadiahassouni.fixup.ui.client.ClientActivity
import com.nadiahassouni.fixup.databinding.FragmentSignInBinding
import com.nadiahassouni.fixup.models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        binding.btnSignIn.setOnClickListener {
            signInUser()
        }
        binding.tvCreateAccount.setOnClickListener {
            navigateToSignUpFragment()
        }
        return binding.root
    }

    private fun navigateToSignUpFragment() {
        Navigation.findNavController(binding.root)
            .navigate(R.id.action_signInFragment_to_usersTypesFragment)

    }

    private fun signInUser() {
        try {
            if (checkInputNotEmpty()) {
                showProgressBar()
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(binding.etEmail.text.toString(),
                        binding.etPassword.text.toString())
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            if (emailIsVerified()) {
                                navigateToHome()
                            } else {
                                sendVerificationEmail()
                                signOut()
                                Toast.makeText(context,
                                    "Verifiez votre compte pour pouvoir y acceder . Un email de verification est envoye",
                                    Toast.LENGTH_LONG).show()
                            }
                            hideProgressBar()
                        }
                    }
                    .addOnFailureListener(requireActivity()) {
                        Toast.makeText(context, "$it", Toast.LENGTH_LONG).show()
                        hideProgressBar()
                    }
            } else {
                Toast.makeText(context, " Veillez complétez tous les champs !", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            hideProgressBar()

        }

    }

    private fun navigateToHome() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userCollectionRef = FirebaseAuth.getInstance().currentUser?.uid?.let {
                    Firebase.firestore.collection("users")
                        .document(it)
                }
                val querySnapshot = userCollectionRef?.get()?.await()
                val userRole = querySnapshot?.toObject<User>()?.role
                withContext(Dispatchers.Main) {
                    when (userRole) {
                        "client" -> {
                            openClientActivity()
                        }
                        "agent" -> {
                            openAgentActivity()
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun sendVerificationEmail() {
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
    }

    private fun emailIsVerified(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.isEmailVerified == true
    }

    private fun checkInputNotEmpty(): Boolean {
        return (binding.etEmail.text.toString().isNotEmpty() &&
                binding.etPassword.text.toString().isNotEmpty())
    }

    private fun openAgentActivity() {
        val intent = Intent(requireActivity(), AgentActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun openClientActivity() {
        val intent = Intent(requireActivity(), ClientActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSignIn.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.btnSignIn.visibility = View.VISIBLE
    }
}