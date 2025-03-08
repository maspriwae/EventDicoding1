package com.dicoding.eventdicoding.ui.detail

//import android.net.Uri
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.dicoding.eventdicoding.R
import com.dicoding.eventdicoding.databinding.ActivityDetailEventBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale


class DetailEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailEventBinding
    private lateinit var viewModel: DetailEventViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEventBinding.inflate(layoutInflater)
        val view = binding.root
        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        viewModel = ViewModelProvider(this)[DetailEventViewModel::class.java]
        eventId?.let { viewModel.getDetailEvents(it) }

        setEventData()
    }

    private fun setEventData() {
        viewModel.detailEvent.observe(this) { event ->

            val totalQuota = event.quota - event.registrants
            with(binding) {
                tvDetCategory.text = event.category
                tvDetOwnerName.text = event.ownerName
                tvDetDesc.text = HtmlCompat.fromHtml(event.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvDetName.text = event.name
                tvDetQuotaLeft.text = totalQuota.toString()
                tvDetCityName.text = event.cityName
                tvDetTime.text = dateFormat(event.beginTime, event.endTime)
                Glide.with(this@DetailEventActivity)
                    .load(event.mediaCover)
                    .into(binding.ivDetMediaCover)

                btnLink.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, event.link.toUri())
                    startActivity(intent)
                }

            }
        }

        viewModel.isLoading.observe(this) {
            showLoading(it)
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar2.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun dateFormat(beginTime: String, endTime: String): String {
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)
        try {
            val beginDate = input.parse(beginTime)
            val endDate = input.parse(endTime)
            return "${beginDate?.let { output.format(it) }} - ${endDate?.let {
                output.format(
                    it
                )
            }}"
        } catch (e: ParseException) {
            e.printStackTrace()
            return "$beginTime - $endTime"
        }
    }

    companion object {
        private const val EXTRA_EVENT_ID = "extra_event_id"

        fun start(context: Context, eventId: String) {
            val intent = Intent(context, DetailEventActivity::class.java)
            intent.putExtra(EXTRA_EVENT_ID, eventId)
            context.startActivity(intent)
        }
    }
}
