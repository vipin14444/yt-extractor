package com.raka.yt_extractor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.raka.ytube_extractor.exception.ExtractionException
import com.raka.ytube_extractor.exception.YoutubeRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExtractorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extractor)
        findViewById<Button>(R.id.btnCDN).setOnClickListener{
            getUrl()
        }
    }
    private fun getUrl() {
        val youtubeJExtractor = com.raka.ytube_extractor.YoutubeJExtractor()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = youtubeJExtractor.getUrlForAndroidEmbeddedPlayer(findViewById<EditText>(R.id.youtubeId).text.toString())
                withContext(Dispatchers.Main){
                    findViewById<TextView>(R.id.cdnUrl).text = url
                }
            } catch (e: ExtractionException) {
                // Something really bad happened, nothing we can do except just show some error notification to the user
            } catch (e: YoutubeRequestException) {
                // Possibly there are some connection problems, ask user to check the internet connection and then retry
            }
        }
    }
}