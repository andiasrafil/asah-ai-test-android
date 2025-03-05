package com.andiasrafil.asahaitest.feature.main.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.andiasrafil.asahaitest.helpers.enums.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _player = MutableStateFlow<ExoPlayer?>(null)
    private val _uiState = MutableStateFlow(MainUIState())
    val uiState: StateFlow<MainUIState> = _uiState.asStateFlow()

    init {
        initializePlayer()
    }

    private fun pickRandomNumber(): Int = (1..20).random()

    private fun initializePlayer() {
        val exoPlayer = ExoPlayer.Builder(getApplication()).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> if (_uiState.value.isLooping) {
                            playNextRandomAudio()
                            updateIteration()
                        }
                        Player.STATE_READY -> _uiState.update { it.copy(isLoading = false) }
                        Player.STATE_BUFFERING -> _uiState.update { it.copy(isLoading = true) }
                        else -> Unit
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _uiState.update { it.copy(errorMessage = error.localizedMessage ?: "Playback error") }
                }
            })
        }
        _player.value = exoPlayer
    }

    fun onNextTap() {
        _uiState.update { it.copy(isLooping = true) }
        _player.value?.let { player ->
            val audioUrl = getRandomAudioFromSelectedVoice()
            if (audioUrl != null) {
                updateIteration()
                playAudio(audioUrl)
            } else {
                _uiState.update { it.copy(errorMessage = "No audio found for the selected voice.") }
            }
        }
    }

    private fun playNextRandomAudio() {
        getRandomAudioFromSelectedVoice()?.let { playAudio(it) }
    }

    private fun getRandomAudioFromSelectedVoice(): String? {
        return _uiState.value.selectedVoice?.getVoiceSample(pickRandomNumber())
    }

    fun onVoiceCardClick(voice: Voice) {
        _uiState.update { it.copy(selectedVoice = voice) }
        updateIteration()
        getRandomAudioFromSelectedVoice()?.let { playAudio(it) }
    }

    private fun playAudio(audioUrl: String) {
        try {
            _player.value?.let { player ->
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val mediaItem = MediaItem.fromUri(Uri.parse(audioUrl))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Failed to play audio: ${e.localizedMessage}") }
        }
    }

    fun updateTap() {
        _uiState.update { it.copy(isAllowToTap = true) }
    }

    private fun updateIteration() {
        _uiState.update {
            if (it.isAllowToTap) {
                it.copy(iteration = if (it.iteration == 999) 1 else it.iteration)
            } else {
                it
            }
        }
    }

    fun pause() {
        _player.value?.pause()
    }

    fun releasePlayer() {
        _player.value?.release()
        _player.value = null
    }

    override fun onCleared() {
        releasePlayer()
        super.onCleared()
    }
}
