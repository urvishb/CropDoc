package isomora.com.greendoctor.models

import com.google.firebase.firestore.PropertyName

data class Posts(
    var location: String = "",
    // getter and setter is for serialization and deserialization for CamelCase to underScores
    @get:PropertyName("image_url") @set:PropertyName("image_url") var imageUrl: String = "",
    @get:PropertyName("creation_time_ms") @set:PropertyName("creation_time_ms") var creationTimeMs: Long = 0,
    var user: Users? = null
)