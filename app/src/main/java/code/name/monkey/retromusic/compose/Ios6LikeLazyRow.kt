package code.name.monkey.retromusic.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import code.name.monkey.retromusic.model.AlbumHorizontalPagerModel
import code.name.monkey.retromusic.util.theme.RetroTheme

@Composable
fun Ios6LikeLazyRow(
    pagerState: PagerState,
    dataSet: List<AlbumHorizontalPagerModel>,
    onClickAlbumCard: (albumId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(10),
    )

    // ページごとの抽出色を保持するMap。初期値はテーマの背景色にしておくとスムーズです
    val defaultBg = MaterialTheme.colorScheme.background
    var albumColors by remember { mutableStateOf(mapOf<Int, Color>()) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val itemWidth = 220.dp
        val horizontalPadding = (screenWidth - itemWidth) / 2

        // 現在のページとその前後の色を取得（Mapにない場合はデフォルト色）
        val leftColor = albumColors[pagerState.currentPage - 1] ?: defaultBg
        val centerColor = albumColors[pagerState.currentPage] ?: defaultBg
        val rightColor = albumColors[pagerState.currentPage + 1] ?: defaultBg

        val animatedLeft by animateColorAsState(
            targetValue = leftColor,
            animationSpec = tween(durationMillis = 600),
            label = "LeftColor"
        )
        val animatedCenter by animateColorAsState(
            targetValue = centerColor,
            animationSpec = tween(durationMillis = 600),
            label = "CenterColor"
        )
        val animatedRight by animateColorAsState(
            targetValue = rightColor,
            animationSpec = tween(durationMillis = 600),
            label = "RightColor"
        )

        // 全体のコンテナ。ここには IntrinsicSize を指定しない
        Box(modifier = Modifier.fillMaxWidth()) {

            // --- 背景レイヤー ---
            // matchParentSize は「同じBox内の他の子要素(Pager)が決めたサイズ」に自身を合わせます
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen) // 合成用にオフスクリーン描画
                    .drawBehind {
                        // 1. 横方向の色グラデーションを描画
                        val colorBrush = Brush.horizontalGradient(
                            colors = listOf(animatedLeft, animatedCenter, animatedRight)
                        )
                        drawRect(brush = colorBrush)

                        // 2. 縦方向の透明度マスクを描画
                        // Color.Black (不透明) から Color.Transparent (透明) へ
                        val alphaBrush = Brush.verticalGradient(
                            0.0f to Color.Black,
                            0.8f to Color.Black,       // 60%くらいまではくっきり見せる
                            1.0f to Color.Transparent  // 下端で完全に消す
                        )
                        drawRect(
                            brush = alphaBrush,
                            blendMode = BlendMode.DstIn
                        )
                        // ジャケットの色をより鮮やかにするためにScreenで重ねる
                        drawRect(
                            brush = colorBrush,
                            blendMode = BlendMode.Screen,
                            alpha = 0.5f // 重ねる強さを調整
                        )
                    }
            )

            // --- コンテンツレイヤー (Pager) ---
            HorizontalPager(
                modifier = modifier.fillMaxWidth(),
                state = pagerState,
                pageSize = PageSize.Fixed(itemWidth),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                flingBehavior = fling,
                beyondViewportPageCount = 2,
            ) { page ->
                // 各ジャケットが色を解析し終えたらMapを更新するコールバック
                val onColorLoaded: (Color) -> Unit = { color ->
                    if (albumColors[page] != color) {
                        // Composeに状態変化を知らせるため、Mapごと更新
                        albumColors = albumColors.toMutableMap().apply { this[page] = color }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AlbumJacket(
                        setColorCallback = onColorLoaded,
                        albumData = dataSet[page],
                        pageIndex = page,
                        pagerState = pagerState,
                        onClickAlbum = onClickAlbumCard
                    )
                }
            }
        }
    }
}

fun List<Color>.downBrightness(percent: Float): List<Color> {
    // 0.0〜1.0の範囲外にならないよう念のため制限（coerceIn）
    val factor = percent.coerceIn(0f, 1f)

    return this.map { color ->
        color.copy(
            red = color.red * factor,
            green = color.green * factor,
            blue = color.blue * factor
            // alpha（透明度）はそのまま維持
        )
    }
}


fun Triple<*, *, *>.isAllSatisfied(): Boolean {
    return first != null && second != null && third != null
}

@Preview
@Composable
fun Ios6LikeLazyRowPreview() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    RetroTheme {
        Ios6LikeLazyRow(
            pagerState = pagerState,
            dataSet = listOf(
                PreviewData.sampleAlbumPagerModel,
                PreviewData.sampleAlbumPagerModel,
                PreviewData.sampleAlbumPagerModel
            ),
            onClickAlbumCard = {}
        )
    }
}
