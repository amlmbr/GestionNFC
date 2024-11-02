package ma.ensa.projet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.projet.R
import ma.ensa.projet.beans.NFCTag
import java.text.SimpleDateFormat
import java.util.Locale

class NFCHistoryAdapter : ListAdapter<NFCTag, NFCHistoryAdapter.ViewHolder>(NFCTagDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nfc_tag, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentTextView: TextView = itemView.findViewById(R.id.contentTextView)
        private val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(tag: NFCTag) {
            contentTextView.text = tag.content
            typeTextView.text = tag.type.name
            timestampTextView.text = dateFormat.format(tag.timestamp)
        }
    }
}

class NFCTagDiffCallback : DiffUtil.ItemCallback<NFCTag>() {
    override fun areItemsTheSame(oldItem: NFCTag, newItem: NFCTag): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: NFCTag, newItem: NFCTag): Boolean {
        return oldItem == newItem
    }

}
