package code.name.monkey.retromusic.compose

import androidx.core.net.toUri
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.AlbumHorizontalPagerModel
import code.name.monkey.retromusic.model.Song

object PreviewData {
    val sampleSong = Song(
        id = 1L,
        title = "Bohemian Rhapsody",
        trackNumber = 1,
        year = 1975,
        duration = 354000L,
        data = "",
        dateModified = 0L,
        albumId = 1L,
        albumName = "A Night at the Opera",
        artistId = 1L,
        artistName = "Queen",
        composer = "Freddie Mercury",
        albumArtist = "Queen"
    )

    val sampleSong2 = Song(
        id = 2L,
        title = "Don't Stop Me Now",
        trackNumber = 2,
        year = 1978,
        duration = 209000L,
        data = "",
        dateModified = 0L,
        albumId = 1L,
        albumName = "Jazz",
        artistId = 1L,
        artistName = "Queen",
        composer = "Freddie Mercury",
        albumArtist = "Queen"
    )

    val sampleAlbum = Album(
        id = 1L,
        songs = listOf(sampleSong, sampleSong2)
    )

    val sampleAlbumPagerModel = AlbumHorizontalPagerModel(
        albumId = 1L,
        albumName = "A Night at the Opera",
        albumArtist = "Queen",
        jacketImageUri = "android.resource://code.name.monkey.retromusic/drawable/ic_image".toUri()
    )
}
