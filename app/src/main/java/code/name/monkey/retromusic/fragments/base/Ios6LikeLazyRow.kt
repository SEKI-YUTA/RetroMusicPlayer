package code.name.monkey.retromusic.fragments.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import code.name.monkey.retromusic.compose.PreviewData
import code.name.monkey.retromusic.model.AlbumHorizontalPagerModel
import code.name.monkey.retromusic.util.theme.RetroTheme

@Composable
fun Ios6LikeLazyRow(
    pagerState: PagerState,
    dataSet: List<AlbumHorizontalPagerModel>,
    modifier: Modifier = Modifier,
    onClickAlbumCard: (albumId: Long) -> Unit
) {
    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(10),
    )

    // 1. 親の幅を取得するために BoxWithConstraints を使用
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val itemWidth = 220.dp

        // 2. アイテムを中央に寄せるためのパディングを計算
        // (画面幅 - アイテム幅) / 2 を左右に設定する
        val horizontalPadding = (screenWidth - itemWidth) / 2

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(itemWidth),
            contentPadding = PaddingValues(horizontal = horizontalPadding), // ここが肝！
            flingBehavior = fling,
            beyondViewportPageCount = 2, // 左右のアイテムが消えないように多めに描画
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AlbumJacket(
                    albumData = dataSet[page],
                    pageIndex = page,
                    pagerState = pagerState,
                    onClickAlbum = onClickAlbumCard
                )
            }
        }
    }
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
