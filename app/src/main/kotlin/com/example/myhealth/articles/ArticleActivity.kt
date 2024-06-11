package com.example.myhealth.articles

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myhealth.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class NewsItem(
    val title: String,
    val topImage: String,
    val date: String,
    val shortDescription: String,
    val text: String
)


class ArticleActivity : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<NewsItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.article)

        newsRecyclerView = findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)
        newsAdapter = NewsAdapter(newsList) { newsItem ->
            val intent = Intent(this, ArticleDetailActivity::class.java).apply {
                putExtra("title", newsItem.title)
                putExtra("topImage", newsItem.topImage)
                putExtra("date", newsItem.date)
                putExtra("shortDescription", newsItem.shortDescription)
                putExtra("text", newsItem.text)
            }
            startActivity(intent)
        }
        newsRecyclerView.adapter = newsAdapter
        fetchNewsData()
    }

    private fun fetchNewsData() {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS) // Adjust as needed
            .connectTimeout(30, TimeUnit.SECONDS) // Adjust as needed
            .build()

        val request = Request.Builder()
            .url("https://newsnow.p.rapidapi.com/newsv2_top_news_cat")
            .post(
                """
                {
                    "category": "HEALTH",
                    "location": "",
                    "language": "en",
                    "page": 1
                }
            """.trimIndent().toRequestBody("application/json".toMediaType())
            )
            .addHeader("content-type", "application/json")
            .addHeader("X-RapidAPI-Key", "4269bafd66msh60e69fa4fec3bc4p17b3edjsnd14c34f6b464")
            .addHeader("X-RapidAPI-Host", "newsnow.p.rapidapi.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("MainActivity", "Error fetching news", e)
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val responseObject = JSONObject(responseString)
                    val newsArray = responseObject.getJSONArray("news")

                    for (i in 0 until newsArray.length()) {
                        val newsObject = newsArray.getJSONObject(i)
                        val title = newsObject.getString("title")
                        val topImage = newsObject.getString("top_image")
                        val date = newsObject.getString("date")
                        val shortDescription = newsObject.getString("short_description")
                        val text = newsObject.getString("text")

                        val newsItem = NewsItem(title, topImage, date, shortDescription, text)
                        newsList.add(newsItem)
                    }

                    runOnUiThread {
                        newsAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
}
