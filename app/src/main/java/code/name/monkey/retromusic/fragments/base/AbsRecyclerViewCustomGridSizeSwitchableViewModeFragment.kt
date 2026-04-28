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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import code.name.monkey.retromusic.EXTRA_ALBUM_ID
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.compose.Ios6LikeLazyRow
import code.name.monkey.retromusic.compose.SongsList
import code.name.monkey.retromusic.extensions.getTintedDrawable
import code.name.monkey.retromusic.model.Album
import code.name.monkey.retromusic.model.AlbumHorizontalPagerModel
import code.name.monkey.retromusic.util.MusicUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import code.name.monkey.retromusic.util.theme.RetroTheme

abstract class AbsRecyclerViewCustomGridSizeSwitchableViewModeFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
    AbsRecyclerViewCustomGridSizeFragment<A, LM>() {
    private var isOldAlbumView = false
    private var oldAlbumView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isOldAlbumView = PreferenceUtil.isOldAppleAlbumViewEnabled
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
                RetroTheme {
                    Scaffold {
                        val jacketUriList =
                            produceState(initialValue = emptyList()) {
                                libraryViewModel.getAlbums().observe(viewLifecycleOwner) { albums ->
                                    value = albums.map { album ->
                                        AlbumHorizontalPagerModel(
                                            albumId = album.id,
                                            albumName = album.title,
                                            albumArtist = album.albumArtist,
                                            jacketImageUri = MusicUtil.getMediaStoreAlbumCoverUri(
                                                album.id
                                            )
                                        )
                                    }
                                }
                            }
                        Box(
                            modifier = Modifier.padding(
                                bottom = (it.calculateTopPadding().value + it.calculateBottomPadding().value + dimensionResource(
                                    R.dimen.bottom_nav_mini_player_height
                                ).value).dp
                            )
                        ) {
                            val pagerState =
                                rememberPagerState(pageCount = { jacketUriList.value.size })
                            var currentShowingAlbum by remember {
                                mutableStateOf<Album?>(null)
                            }
                            LaunchedEffect(pagerState.settledPage, jacketUriList.value.size) {
                                if (jacketUriList.value.isEmpty()) return@LaunchedEffect
                                currentShowingAlbum = libraryViewModel.albumById(
                                    jacketUriList.value[pagerState.currentPage].albumId
                                )
                            }
                            Crossfade(
                                targetState = currentShowingAlbum,
                            ) { album ->
                                SongsList(
                                    album = album,
                                    contentPadding = PaddingValues(top = 308.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(292.dp)
                                    .background(Color.Black.copy(alpha = 0.1f))
                            )

                            Ios6LikeLazyRow(
                                modifier = Modifier
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.background,
                                                MaterialTheme.colorScheme.background,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                            ),
                                        ),
                                    )
                                    .fillMaxWidth()
                                    .height(292.dp),
                                pagerState = pagerState,
                                dataSet = jacketUriList.value,
                                onClickAlbumCard = { albumId ->
                                    if (oldAlbumView != null) FragmentNavigatorExtras(
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
            }
        }
    }

    private fun getRecyclerViewVisibility(): Int {
        return if (PreferenceUtil.isOldAppleAlbumViewEnabled) View.GONE else View.VISIBLE
    }

    private fun getOldAlbumViewVisibility(): Int {
        return if (PreferenceUtil.isOldAppleAlbumViewEnabled) View.VISIBLE else View.GONE
    }

    private fun updateViewVisibility() {
        _binding?.recyclerView?.visibility = getRecyclerViewVisibility()
        _binding?.oldAlbumView?.visibility = getOldAlbumViewVisibility()
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateMenu(menu, inflater)
        menu.findItem(R.id.action_toggle_album_view_mode)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.action_toggle_album_view_mode)?.let {
            updateToggleViewModeIcon(it, PreferenceUtil.isOldAppleAlbumViewEnabled)
        }
    }

    private fun updateToggleViewModeIcon(item: MenuItem, isOldAppleAlbumView: Boolean) {
        val icon = if (isOldAppleAlbumView) {
            R.drawable.ic_snap_list
        } else {
            R.drawable.ic_grid_view
        }
        val toolbarColor = ATHToolbarActivity.getToolbarBackgroundColor(toolbar)
        val tintColor = ToolbarContentTintHelper.toolbarContentColor(requireContext(), toolbarColor)
        item.icon = requireContext().getTintedDrawable(icon, tintColor)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_album_view_mode) {
            PreferenceUtil.isOldAppleAlbumViewEnabled = !PreferenceUtil.isOldAppleAlbumViewEnabled
            updateToggleViewModeIcon(item, PreferenceUtil.isOldAppleAlbumViewEnabled)
            updateViewVisibility()
            return true
        }
        return false
    }
}