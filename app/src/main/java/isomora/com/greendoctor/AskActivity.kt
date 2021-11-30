package isomora.com.greendoctor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import isomora.com.greendoctor.models.Posts
import kotlinx.android.synthetic.main.activity_ask.*

private const val TAG = "AskActivity"
class AskActivity : AppCompatActivity() {

    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts : MutableList<Posts>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ask)
        val sightEmail = "guest@test.com"
        val sightPassword = "password"
        val auth = FirebaseAuth.getInstance()


        // Firebase Auth >>>

        auth.signInWithEmailAndPassword(sightEmail, sightPassword).addOnCompleteListener { task ->

            if (task.isSuccessful) {
                Toast.makeText(this, "Welcome to Ask Mode", Toast.LENGTH_SHORT).show()
            }
            else {
                Log.e(TAG, "signInWithEmail failed (Sight Mode)", task.exception)
                Toast.makeText(this, "Authentication failed (Sight Mode)", Toast.LENGTH_SHORT).show()
            }
        }


        posts = mutableListOf()

        adapter = PostsAdapter(this, posts)
        // Bind the adapter and Layout manager to the RV
        rvPosts.adapter = adapter
        rvPosts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        firestoreDb = FirebaseFirestore.getInstance()




        val postsReference = firestoreDb.collection("posts")
            .limit(15)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)
        postsReference.addSnapshotListener { snapshot, exception ->
            if(exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Posts::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.i(TAG, "Post ${post}")
            }
        }

        adapter.setOnItemClickListener(object : PostsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                //Toast.makeText(context, "You Clicked on item no. $position", Toast.LENGTH_SHORT).show()


                val intent = Intent(this@AskActivity, StoryActivity:: class.java)


                intent.putExtra("location", posts[position].location)
                intent.putExtra("imageUrl", posts[position].imageUrl)
                intent.putExtra("creationTime", DateUtils.getRelativeTimeSpanString(posts[position].creationTimeMs))
                startActivity(intent)
            }
        })

    }
}