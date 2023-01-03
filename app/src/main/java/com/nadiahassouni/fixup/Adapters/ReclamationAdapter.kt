package com.nadiahassouni.fixup.Adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.models.Reclamation
import com.nadiahassouni.fixup.models.User
import com.nadiahassouni.fixup.ui.agent.AgentActivity
import com.nadiahassouni.fixup.ui.client.ClientActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReclamationAdapter (
    private val context: Context,
    private val reclamationsList: ArrayList<Reclamation>,
    private val activityName : String,
    private val activity : FragmentActivity )
    : RecyclerView.Adapter<ReclamationAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvDate: TextView = view.findViewById(R.id.tv_date)
        var tvName: TextView = view.findViewById(R.id.tv_name)
        var tvEmail: TextView = view.findViewById(R.id.tv_email)
        var tvProblem: TextView = view.findViewById(R.id.tv_problem)
        var tvWilaya: TextView = view.findViewById(R.id.tv_wilaya)
        var agentImageView: ImageView = view.findViewById(R.id.iv_profile_client)
        var btnSeeMore : Button = view.findViewById(R.id.btn_see_more)
        var btnDelete : ImageView = view.findViewById(R.id.iv_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.reclamation_rv_item , parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.tvDate.text = reclamationsList[position].date
        holder.tvProblem.text = reclamationsList[position].problemType
        holder.tvName.text = reclamationsList[position].title

        holder.btnDelete.setOnClickListener {
            val builder: AlertDialog.Builder = context.let {
                AlertDialog.Builder(it)
            }

            builder.setMessage("Voulez vous vraiment supprimer la reclamation ?")
                .setTitle("Supprimer une reclamation")

            builder.apply {
                setPositiveButton("Oui") { dialog, id ->
                    deleteReclamation(reclamationsList[position].id)

                        when(activityName){
                        "agent" ->{
                            activity.finish()
                            context.startActivity(Intent(context , AgentActivity::class.java))
                        }
                        "client" ->{
                            activity.finish()
                            context.startActivity(Intent(context , ClientActivity::class.java))
                        }
                    }

                }
                setNegativeButton("Non") { dialog, id ->
                    dialog.dismiss()
                }
            }
            val dialog: AlertDialog? = builder.create()

            dialog!!.show()
        }

            setClientInfo(reclamationsList[position].clientId , holder)

        holder.btnSeeMore.setOnClickListener {
            val reclamation = Bundle()
            reclamation.putSerializable("reclamation" , reclamationsList.get(position))

            Navigation.findNavController(holder.itemView).navigate(R.id.action_profileReclamationsFragment_to_reclamationFragment , reclamation)

        }
    }

    private fun setClientInfo(clientId: String, holder: ItemViewHolder) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val userCollectionRef = Firebase.firestore.collection("users").document(clientId)
            val querySnapshot = userCollectionRef.get().await()
            val user : User? = querySnapshot.toObject<User>()


            withContext(Dispatchers.Main){
                if(user!!.imageUrl != "")
                    Glide.with(context)
                        .load(Uri.parse(user.imageUrl))
                        .into(holder.agentImageView)

                holder.tvEmail.text = user.name + " " + user.surname
                holder.tvWilaya.text = user.wilaya

                }
        }catch (e : Exception){

        }
    }

    private fun deleteReclamation(id: String)= CoroutineScope(Dispatchers.IO).launch {
        try {
            val reclamationCollectionRef =
                Firebase.firestore.collection("reclamations").whereEqualTo("id", id )
            val snapshot =  reclamationCollectionRef.get().await()
            for (doc in snapshot.documents){
                doc.reference.delete()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "La reclamation a ete supprimee", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getItemCount()= reclamationsList.size

}