package code.name.monkey.retromusic.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import code.name.monkey.retromusic.helper.MusicPlayerRemote
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.theme.RetroTheme

@Composable
fun SongsList(
    album: Album?
) {
    LazyColumn(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        album?.let { album ->
            itemsIndexed(album.songs) { index, song ->
                SongCard(
                    onClick = {
                        MusicPlayerRemote.openQueue(album.songs, index, true)
                    },
                    song = song
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "total: ${
                            MusicUtil.getReadableDurationString(
                                MusicUtil.getTotalDuration(
                                    album.songs
                                )
                            )
                        }"
                    )
                }
            }
            item {
                Spacer(
                    modifier = Modifier.height(20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun SongsListPreview() {
    RetroTheme {
        SongsList(album = PreviewData.sampleAlbum)
    }
}
