package isomora.com.greendoctor

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import isomora.com.greendoctor.models.Posts
import kotlinx.android.synthetic.main.item_post.view.*

class PostsAdapter (val context: Context, private val posts: List<Posts>) : RecyclerView.Adapter<PostsAdapter.Myviewholder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {

        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Myviewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return Myviewholder(view, mListener)
    }

    override fun onBindViewHolder(holder: Myviewholder, position: Int) {
        holder.bind(posts[position])


    }

    override fun getItemCount() = posts.size

    inner class Myviewholder(itemView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(post: Posts) {
            itemView.myheader.text = post.location
            Glide.with(context).load(post.imageUrl).into(itemView.rvimage)
            itemView.mydesc.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
        }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}