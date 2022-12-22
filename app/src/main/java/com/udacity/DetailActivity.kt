package com.udacity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.udacity.databinding.ActivityDetailBinding
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(toolbar)

        val status = intent.getIntExtra(DOWNLOAD_STATUS_KEY, -1)
        val fileName = intent.getStringExtra(FILE_NAME_KEY) ?: ""

        binding.content.statusText.apply {
            if (status < 1) {
                text = context.getString(R.string.status_fail)
                setTextColor(Color.RED)
            } else
                text = context.getString(R.string.status_success)
        }
        binding.content.fileNameText.text = fileName

        binding.content.okButton.setOnClickListener {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }
    }

}
