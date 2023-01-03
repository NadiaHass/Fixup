package com.nadiahassouni.fixup.Adapters

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nadiahassouni.fixup.R
import com.nadiahassouni.fixup.models.User

class AgentAdapter(
private val context: Context,
private val agentsList: ArrayList<User>)
: RecyclerView.Adapter<AgentAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvDescription: TextView = view.findViewById(R.id.tv_description)
        var tvName: TextView = view.findViewById(R.id.tv_name)
        var agentImageView: ImageView = view.findViewById(R.id.iv_profile_agent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_agent_rv_item , parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.tvName.text = agentsList[position].name + " " + agentsList[position].surname
        holder.tvDescription.text = agentsList[position].description
        Glide.with(context)
            .load(Uri.parse(agentsList[position].imageUrl))
            .into(holder.agentImageView)

        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("agent" , agentsList.get(position).id)
                        Navigation.findNavController(holder.itemView).navigate(R.id.action_homeClientFragment_to_profileReclamationsFragment , bundle)

                }
            }

    override fun getItemCount()= agentsList.size

}