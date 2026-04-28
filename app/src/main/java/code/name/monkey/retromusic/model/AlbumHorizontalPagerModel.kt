package code.name.monkey.retromusic.model

import android.net.Uri

data class AlbumHorizontalPagerModel(
    val albumId: Long,
    val albumName: String,
    val albumArtist: String?,
    val jacketImageUri: Uri
)