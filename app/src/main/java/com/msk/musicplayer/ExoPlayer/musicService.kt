package com.msk.musicplayer.ExoPlayer

import android.app.PendingIntent
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.msk.musicplayer.ExoPlayer.CallBack.MusicPlayerNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject
private const val MEDİA_SERVİCE="mediaService"

@AndroidEntryPoint
class musicService:MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoplayer:SimpleExoPlayer

    private val serviceJob=Job()
    private val serviceScope= CoroutineScope(Dispatchers.Main+serviceJob)
    private lateinit var musicNotificationManager:MusicNotificationManager
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var mediaSessionConnector:MediaSessionConnector
    var isForegroundService=false

    override fun onCreate() {
        super.onCreate()
        val activityIntent=packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)

        }

        mediaSession= MediaSessionCompat(this, MEDİA_SERVİCE).apply {
            setSessionActivity(activityIntent)
            isActive=true
        }
        sessionToken=mediaSession.sessionToken

        musicNotificationManager=MusicNotificationManager(this,
        mediaSession.sessionToken,
        MusicPlayerNotificationListener(this)){

        }
        mediaSessionConnector= MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoplayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}