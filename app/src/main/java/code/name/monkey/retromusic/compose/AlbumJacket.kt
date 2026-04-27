package code.name.monkey.retromusic.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.model.AlbumHorizontalPagerModel
import code.name.monkey.retromusic.util.color.MediaNotificationProcessor
import code.name.monkey.retromusic.util.theme.RetroTheme
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumJacket(
    albumData: AlbumHorizontalPagerModel,
    pageIndex: Int,
    pagerState: PagerState,
    onClickAlbum: (albumId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localInspection = LocalInspectionMode.current
    var jacketBitmap by remember {
        mutableStateOf<Bitmap?>(null)
    }
    val containerColor = remember {
        mutableStateOf<Color?>(null)
    }

    LaunchedEffect(albumData.jacketImageUri) {
        val bitmap = withContext(Dispatchers.IO) {
            try {
                Glide.with(context)
                    .asBitmap()
                    .load(albumData.jacketImageUri)
                    .submit()
                    .get()
            } catch (e: Exception) {
                null
            }
        }
        jacketBitmap = bitmap
    }

    LaunchedEffect(key1 = jacketBitmap) {
        jacketBitmap?.let { bitmap ->
            MediaNotificationProcessor(context).getPaletteAsync(
                {
                    containerColor.value = Color(it.backgroundColor)
                }, bitmap
            )
        }
    }

    Column(
        modifier = modifier
            .width(200.dp)
            .height(260.dp)
            .graphicsLayer {
                // 現在位置とこのアイテムの距離（-1.0 〜 1.0 ...）
                val pageOffset = (
                        (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                        )

                // 3. 回転の計算 (中心は0、左はプラス、右はマイナス)
                // iOS6風にするなら、少し急激に回転させるために coerceIn を調整
                val fraction = pageOffset.coerceIn(-1f, 1f)
                rotationY = fraction * 45f // 45度くらいにするとそれっぽい

                // 4. 透明度とスケールの調整（中央を強調）
                alpha = lerp(
                    start = 0.6f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )

                val scale = lerp(
                    start = 0.8f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                )
                scaleX = scale
                scaleY = scale

                // Z軸の奥行き感
                cameraDistance = 12f * density
            },
    ) {
        Column {
            Card(
                modifier = Modifier
                    .size(200.dp),
                onClick = {
                    onClickAlbum(albumData.albumId)
                }
            ) {
                if (localInspection) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(R.drawable.ic_image),
                        contentDescription = ""
                    )
                } else {
                    GlideImage(
                        model = jacketBitmap,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "",
                        loading = placeholder(R.drawable.default_album_art),
                        failure = placeholder(R.drawable.default_album_art)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                containerColor.value?.copy(alpha = 0.3f) ?: Color.Transparent,
                                containerColor.value ?: MaterialTheme.colorScheme.primaryContainer,
                            )
                        )
                    )
                    .padding(4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    albumData.albumName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                albumData.albumArtist?.let { albumArtist ->
                    Text(albumArtist, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview
@Composable
fun AlbumJacketPreview() {
    RetroTheme {
        AlbumJacket(
            albumData = PreviewData.sampleAlbumPagerModel,
            pageIndex = 0,
            pagerState = rememberPagerState(pageCount = { 1 }),
            onClickAlbum = {}
        )
    }
}
