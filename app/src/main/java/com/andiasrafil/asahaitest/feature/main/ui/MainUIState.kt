package com.andiasrafil.asahaitest.feature.main.ui

import androidx.media3.exoplayer.ExoPlayer
import com.andiasrafil.asahaitest.helpers.enums.Voice

data class MainUIState(
    val selectedVoice: Voice? = null,
    val iteration: Int = 999,
    val isAllowToTap: Boolean = true,
    val exoPlayer: ExoPlayer? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLooping: Boolean = false
)