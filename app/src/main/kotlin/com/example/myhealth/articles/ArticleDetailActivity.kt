package com.example.myhealth.articles

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.myhealth.R

class ArticleDetailActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        val title = intent.getStringExtra("title")
        val topImage = intent.getStringExtra("topImage")
        val date = intent.getStringExtra("date")
        val shortDescription = intent.getStringExtra("shortDescription")
        val text = intent.getStringExtra("text")

        val articleTitleTextView = findViewById<TextView>(R.id.articleTitleTextView)
        val articleDateTextView = findViewById<TextView>(R.id.articleDateTextView)
        val articleDescriptionTextView = findViewById<TextView>(R.id.articleDescriptionTextView)
        val articleTextView = findViewById<TextView>(R.id.articleTextTextView)
        val articleImageView = findViewById<ImageView>(R.id.articleImageView)

        articleTitleTextView.text = title
        articleDateTextView.text = date
        articleTextView.text = text
        articleDescriptionTextView.text = shortDescription
        Glide.with(this).load(topImage).into(articleImageView)
    }
}
