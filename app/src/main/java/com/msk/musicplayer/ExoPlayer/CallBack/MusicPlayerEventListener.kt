package com.msk.musicplayer.ExoPlayer.CallBack

import android.widget.Toast
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.msk.musicplayer.ExoPlayer.musicService
import java.util.*

class MusicPlayerEventListener(private val MusicService: musicService): Player.EventListener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState==Player.STATE_READY&&!playWhenReady){
            MusicService.stopForeground(false)

        }
    }


    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(MusicService,"An unknown error occured",Toast.LENGTH_LONG).show()
    }
}