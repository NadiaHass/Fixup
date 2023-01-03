package com.nadiahassouni.fixup.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentUsersTypesBinding

class UsersTypesFragment : Fragment() {
    private var _binding : FragmentUsersTypesBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentUsersTypesBinding.inflate(inflater , container , false)

        binding.cvAgent.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("role" , "agent")
            Navigation.findNavController(binding.root).navigate(R.id.action_usersTypesFragment_to_signUpFragment , bundle)
        }

        binding.cvClient.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("role" , "client")
            Navigation.findNavController(binding.root).navigate(R.id.action_usersTypesFragment_to_signUpFragment , bundle)
        }

        return binding.root
    }
}