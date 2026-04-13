/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package code.name.monkey.retromusic.fragments.base

import android.R.attr.fragment
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.retromusic.EXTRA_ALBUM_ID
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.extensions.findNavController
import code.name.monkey.retromusic.util.MusicUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlin.math.absoluteValue

abstract class AbsRecyclerViewCustomGridSizeSwitchableViewModeFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
    AbsRecyclerViewCustomGridSizeFragment<A, LM>() {
    private var isOldAlbumView = false
    private val IS_OLD_ALBUM_VIEW_KEY = "AbdRecyclerViewCustomGridSizeSwitchableViewModeFragment_isOldAlbumView"
    private var oldAlbumView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isOldAlbumView = savedInstanceState?.getBoolean(IS_OLD_ALBUM_VIEW_KEY) ?: false
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateViewVisibility()
        oldAlbumView = _binding?.oldAlbumView
        _binding?.oldAlbumView?.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(
                    viewLifecycleOwner.lifecycle
                )
            )
            setContent {
                val jacketUriList = produceState(initialValue = emptyList<Pair<Uri, Long>>()) {
                    libraryViewModel.getAlbums().value?.let { albums ->
                        val albumJacketUriList = albums.map{ album ->
                            MusicUtil.getMediaStoreAlbumCoverUri(album.id) to album.id
                        }
                        value = albumJacketUriList
                    }
                }
                Ios6LikeLazyRow(
                dataSet = jacketUriList.value,
                    onClickAlbumCard = { albumId ->
                        val extras = if(oldAlbumView != null) FragmentNavigatorExtras(
                            oldAlbumView!! to albumId.toString()
                        ) else null
                        findNavController().navigate(
                            R.id.albumDetailsFragment,
                            bundleOf(EXTRA_ALBUM_ID to albumId),
                            null,
                        )

                    }

                )

            }
        }
    }

    private fun getRecyclerViewVisibility(): Int {
       return if(isOldAlbumView) View.GONE else View.VISIBLE
    }

    private fun getOldAlbumViewVisibility(): Int {
        return if(isOldAlbumView) View.VISIBLE else View.GONE
    }

    private fun updateViewVisibility() {
        _binding?.recyclerView?.visibility = getRecyclerViewVisibility()
        _binding?.oldAlbumView?.visibility = getOldAlbumViewVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(IS_OLD_ALBUM_VIEW_KEY, isOldAlbumView)
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateMenu(menu, inflater)
        menu.findItem(R.id.action_toggle_album_view_mode)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_album_view_mode) {
            isOldAlbumView = true
            updateViewVisibility()
            return true
        }
        return false
    }
}


@Preview
@Composable
fun SamplePreview() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .background(Color.Red)) {
        Text("Hello from Compose", fontSize = 30.sp)
    }
}

@Composable
fun Ios6LikeLazyRow(
    dataSet: List<Pair<Uri, Long>>, modifier: Modifier = Modifier, onClickAlbumCard: (albumId: Long) -> Unit
) {
    val listData = remember { List(10) { "Item No.$it" } }
    val pagerState = rememberPagerState(pageCount = { dataSet.size  })

    // 1. 親の幅を取得するために BoxWithConstraints を使用
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val screenWidth = maxWidth
        val itemWidth = 220.dp

        // 2. アイテムを中央に寄せるためのパディングを計算
        // (画面幅 - アイテム幅) / 2 を左右に設定する
        val horizontalPadding = (screenWidth - itemWidth) / 2

        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(itemWidth),
            contentPadding = PaddingValues(horizontal = horizontalPadding), // ここが肝！
            beyondViewportPageCount = 2, // 左右のアイテムが消えないように多めに描画
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            Card (
                onClick = {
                    onClickAlbumCard(dataSet[page].second)
                }
            ){
                AlbumJacket(
                    uriStr = dataSet[page].first.toString(),
                    pageIndex = page,
                    pagerState = pagerState
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumJacket(
    uriStr: String,
    pageIndex: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .size(200.dp) // Card自体のサイズ
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            GlideImage(
                model = uriStr.toUri(),
                modifier = Modifier.fillMaxSize(),
                contentDescription = ""
            )
        }
    }
}
