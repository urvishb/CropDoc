package isomora.com.greendoctor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_story.*
import java.util.*

class StoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        // hide status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_story)

        progressView.animate()
            .scaleX(1F)
            .setDuration(5000)
            .start()

        val eventLocation : TextView = findViewById(R.id.eventLocation)
        val eventTime : TextView = findViewById(R.id.eventTime)
        val storyImage : ImageView = findViewById(R.id.storyImage)

        val bundle : Bundle? = intent.extras
        val itemLocation = bundle!!.getString("location")
        val itemTime = bundle.getString("creationTime")
        val storyUrl = bundle.getString("imageUrl")



        eventLocation.text = itemLocation
        eventTime.text = itemTime
        Glide.with(this).load(storyUrl).into(storyImage)



        Timer().schedule(object : TimerTask() {
            override fun run() {
                finish()
            }
        }, 5000)
    }
}