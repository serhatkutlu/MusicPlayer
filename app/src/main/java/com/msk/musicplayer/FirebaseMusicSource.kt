package com.msk.musicplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.msk.musicplayer.Data.Entities.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(private val musicDatabase: MusicDatabase) {

    var songs= emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData()= withContext(Dispatchers.IO){
        state=State.STATE_INITIALIZING
        val allsongs=musicDatabase.getAllSong()
        songs=allsongs.map{
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST,it.subtitle)
                .putString(METADATA_KEY_MEDIA_ID,it.mediaId)
                .putString(METADATA_KEY_TITLE,it.title)
                .putString(METADATA_KEY_DISPLAY_TITLE,it.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,it.image)
                .putString(METADATA_KEY_MEDIA_URI,it.url)
                .putString(METADATA_KEY_ALBUM_ART_URI,it.image)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,it.subtitle).build()
        }
        state=State.STATE_INITIALIZED
    }
    //this function makes songs list
    fun asMediaSource(dataSourceFactory:DefaultHttpDataSource.Factory):ConcatenatingMediaSource{
        val concatenatingMediaSource=ConcatenatingMediaSource()
        songs.forEach{
            val mediaSource=ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)

        }
        return concatenatingMediaSource
    }

    fun asMediaItems()=songs.map {
        val desc=MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setMediaId(it.description.mediaId)
            .setIconUri(it.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }
    private val onReadyListeners= mutableListOf<(Boolean)->Unit>()

    private var state:State=State.STATE_CREATED
    set(value) {
        if (value==State.STATE_INITIALIZED||value ==State.STATE_ERROR){
            synchronized(onReadyListeners){
                field=value
                onReadyListeners.forEach{
                    it(state==State.STATE_INITIALIZED)
                }
            }
        }else{
            field=value
        }
    }
    fun whenReady(action:(Boolean) -> Unit):Boolean{
        if (state==State.STATE_CREATED||state==State.STATE_INITIALIZING){
            onReadyListeners+=action
            return false
        }else{
            action(state==State.STATE_INITIALIZED)
            return true
        }
    }
}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}