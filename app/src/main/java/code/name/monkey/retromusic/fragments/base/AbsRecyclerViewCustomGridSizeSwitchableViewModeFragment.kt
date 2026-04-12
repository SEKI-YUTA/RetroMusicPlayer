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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.util.RetroUtil
import code.name.monkey.retromusic.util.logD
import com.google.android.material.transition.MaterialFade

abstract class AbsRecyclerViewCustomGridSizeSwitchableViewModeFragment <A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager> :
        AbsRecyclerViewCustomGridSizeFragment<A, LM>() {
    private lateinit var oldAlbumView: ComposeView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        oldAlbumView = _binding?.oldAlbumView as ComposeView
       _binding?.oldAlbumView?.apply {
           setContent {
               Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color.Red )) {
                   Text("Hello from Compose", fontSize = 30.sp)
               }
           }
       }
        return view
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateMenu(menu, inflater)
        menu.findItem(R.id.action_toggle_album_view_mode).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_toggle_album_view_mode) {
            oldAlbumView.visibility = View.VISIBLE
            return true
        }
        return false
    }
}