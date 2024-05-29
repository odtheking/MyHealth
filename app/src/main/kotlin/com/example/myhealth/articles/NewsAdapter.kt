package com.example.myhealth.articles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import com.bumptech.glide.Glide

class NewsAdapter(
    private val newsList: List<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImageView: ImageView = itemView.findViewById(R.id.newsImageView)
        val newsTitleTextView: TextView = itemView.findViewById(R.id.newsTitleTextView)
        val newsDateTextView: TextView = itemView.findViewById(R.id.newsDateTextView)
        val newsDescriptionTextView: TextView = itemView.findViewById(R.id.newsDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.newsTitleTextView.text = newsItem.title
        holder.newsDateTextView.text = newsItem.date
        holder.newsDescriptionTextView.text = newsItem.shortDescription
        Glide.with(holder.newsImageView.context).load(newsItem.topImage).into(holder.newsImageView)

        holder.itemView.setOnClickListener {
            onItemClick(newsItem)
        }
    }

    override fun getItemCount() = newsList.size
}

