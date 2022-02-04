package com.msk.musicplayer.ExoPlayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.msk.musicplayer.Other.Constants.NETWORK_ERROR
import com.msk.musicplayer.Other.Event
import com.msk.musicplayer.Other.Resource

class MusicServiceConnection(context: Context) {
    private val _isConnected=MutableLiveData<Event<Resource<Boolean?>>>()
     val isConnectes:LiveData<Event<Resource<Boolean?>>> =_isConnected


    private val _networkError=MutableLiveData<Event<Resource<Boolean?>>>()
    private val networkError:LiveData<Event<Resource<Boolean?>>> =_networkError

    private val _playbackState=MutableLiveData<PlaybackStateCompat?>()
     val playbackState:LiveData<PlaybackStateCompat?> =_playbackState


    private val _curPlaySong=MutableLiveData<MediaMetadataCompat?>()
     val curPlaySong:LiveData<MediaMetadataCompat?> =_curPlaySong

    lateinit var mediaControllerCompat: MediaControllerCompat

    private val mediaBrowserConnectionCallBack=MediaBrowserConnectionCallBack(context)

    private val mediaBrowser=MediaBrowserCompat(context
    , ComponentName(context,musicService::class.java),mediaBrowserConnectionCallBack,null
    ).apply {connect()  }

    val transportControls:MediaControllerCompat.TransportControls
        get()=mediaControllerCompat.transportControls

    fun suscribe(parentId:String,callBack:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId,callBack)
    }
    fun unsuscribe(parentId:String,callBack:MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId,callBack)
    }
    private inner class MediaControllerCallBack:MediaControllerCompat.Callback(){
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlaySong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when (event){
                NETWORK_ERROR->_networkError.postValue(
                    Event(
                        Resource.error("Couldn't connect to the server.Please check your internet",null)
                    )
                )
            }

        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallBack.onConnectionSuspended()
        }
    }
    private inner class MediaBrowserConnectionCallBack(
        private val context: Context
    ):MediaBrowserCompat.ConnectionCallback(){
        override fun onConnected() {
            mediaControllerCompat=MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallBack())
            }
            _isConnected.postValue(Event(Resource.succes(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(Resource.error("Connection was suspended",false)))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(Resource.error("Couldn't connect to media browser",false)))


        }
    }


}