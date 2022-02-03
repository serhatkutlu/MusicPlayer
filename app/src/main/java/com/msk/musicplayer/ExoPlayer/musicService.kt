package com.msk.musicplayer.ExoPlayer

import android.app.PendingIntent
import android.content.Intent
import android.media.MediaMetadata
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.msk.musicplayer.ExoPlayer.CallBack.MusicPlayerEventListener
import com.msk.musicplayer.ExoPlayer.CallBack.MusicPlayerNotificationListener
import com.msk.musicplayer.ExoPlayer.CallBack.MusicPlayerPreparer
import com.msk.musicplayer.FirebaseMusicSource
import com.msk.musicplayer.Other.Constants.MEDIA_ROOT_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
private const val MEDIA_SERVICE="mediaService"

@AndroidEntryPoint
class musicService:MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoplayer:SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private val serviceJob=Job()
    private val serviceScope= CoroutineScope(Dispatchers.Main+serviceJob)
    private lateinit var musicNotificationManager:MusicNotificationManager
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var mediaSessionConnector:MediaSessionConnector
    private lateinit var musicPlayerEventListener: MusicPlayerEventListener
    var isForegroundService=false

    private var isPlayerInitialized=false

    companion object{
        var curSongDuration=0L
            private set
    }
    private var curPlayingSong:MediaMetadataCompat?=null
    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent=packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0,it,0)

        }

        mediaSession= MediaSessionCompat(this, MEDIA_SERVICE).apply {
            setSessionActivity(activityIntent)
            isActive=true
        }
        sessionToken=mediaSession.sessionToken

        musicNotificationManager=MusicNotificationManager(this,
        mediaSession.sessionToken,
        MusicPlayerNotificationListener(this)){
            curSongDuration=exoplayer.duration
        }

        var ass=firebaseMusicSource.songs
        val musicPlaybackPreparer=MusicPlayerPreparer(firebaseMusicSource){
            curPlayingSong=it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }
        mediaSessionConnector= MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setPlayer(exoplayer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        musicPlayerEventListener=MusicPlayerEventListener(this)
        exoplayer.addListener(musicPlayerEventListener)

        musicNotificationManager.showNotification(exoplayer)
    }


    private inner class MusicQueueNavigator:TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
           return firebaseMusicSource.songs[windowIndex].description
        }

    }
    private fun preparePlayer(
        songs:List<MediaMetadataCompat>,
        itemtoplay:MediaMetadataCompat?,
        playnow:Boolean
    ){
        val currentSongIndex=if(curPlayingSong==null) 0 else songs.indexOf(itemtoplay)
        exoplayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoplayer.seekTo(currentSongIndex,0L)
        exoplayer.playWhenReady=playnow

    }
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        exoplayer.release()
        exoplayer.removeListener(musicPlayerEventListener)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoplayer.stop()
    }
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? = BrowserRoot(MEDIA_ROOT_ID,null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            MEDIA_ROOT_ID->{
                val resaultsSent=firebaseMusicSource.whenReady {
                    if(it){
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (isPlayerInitialized&&firebaseMusicSource.songs.isNotEmpty()){
                            preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                            isPlayerInitialized=true
                        }
                    }else{
                        result.sendResult(null)
                    }
                }
                if (!resaultsSent){
                    result.detach()
                }
            }
        }
    }
}