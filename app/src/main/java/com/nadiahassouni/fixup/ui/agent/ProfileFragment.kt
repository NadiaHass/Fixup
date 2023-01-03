package com.nadiahassouni.fixup.ui.agent

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.databinding.FragmentProfileBinding
import com.nadiahassouni.fixup.models.User
import com.nadiahassouni.fixup.ui.auth.AuthenticationActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val user = arguments?.getSerializable("userInfo") as User

        binding.tvName.text = user.name + " " + user.surname
        binding.tvDescription.text = user.description
        binding.tvWilaya.text = user.wilaya

        Glide.with(requireContext())
            .load(user.imageUrl)
            .into(binding.ivProfile)

        binding.layout2.setOnClickListener {

            val builder: AlertDialog.Builder? = activity?.let {
                AlertDialog.Builder(it)
            }

            builder!!.setMessage("Voulez vous vraiment se deconnecter ?")
                .setTitle("Se deconnecter")

            builder.apply {
                setPositiveButton("Oui") { dialog, id ->
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(activity , AuthenticationActivity::class.java))
                    activity?.finish()
                }
                setNegativeButton("Non") { dialog, id ->
                    dialog.dismiss()
                }
            }
            val dialog: AlertDialog? = builder.create()

            dialog!!.show()

        }

        binding.layout1.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("user" , user)
            Navigation.findNavController(binding.root).navigate(R.id.action_profileFragment2_to_updatePersonalInfoFragment , bundle)
        }

        return binding.root
    }

}