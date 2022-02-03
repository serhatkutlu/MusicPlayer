package com.msk.musicplayer.ExoPlayer.CallBack

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.msk.musicplayer.FirebaseMusicSource

class MusicPlayerPreparer (
    private val firebaseMusicSource: FirebaseMusicSource,
    private val playerPrepared:(MediaMetadataCompat?)-> Unit
        ):MediaSessionConnector.PlaybackPreparer
{
    override fun onCommand(
        player: Player,
        controlDispatcher: ControlDispatcher,
        command: String,
        extras: Bundle?,
        cb: ResultReceiver?
    ): Boolean {
        return false
    }

    override fun getSupportedPrepareActions(): Long {
        return PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID
    }

    override fun onPrepare(playWhenReady: Boolean) {
    }

    override fun onPrepareFromMediaId(mediaId: String, playWhenReady: Boolean, extras: Bundle?) {
        firebaseMusicSource.whenReady {
            val itemToPlay=firebaseMusicSource.songs.find { mediaId==it.description.mediaId }
            playerPrepared(itemToPlay)
        }
    }

    override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {

    }

    override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) {

    }

}