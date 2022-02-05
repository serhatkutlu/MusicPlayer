package com.msk.musicplayer.Ui

import android.media.MediaMetadata.METADATA_KEY_MEDIA_ID
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msk.musicplayer.Data.Entities.Song
import com.msk.musicplayer.ExoPlayer.MusicServiceConnection
import com.msk.musicplayer.ExoPlayer.isPlayEnabled
import com.msk.musicplayer.ExoPlayer.isPlaying
import com.msk.musicplayer.ExoPlayer.isPrepared
import com.msk.musicplayer.Other.Constants.MEDIA_ROOT_ID
import com.msk.musicplayer.Other.Resource

import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MainViewModel  constructor(
    private val musicServiceConnection: MusicServiceConnection
):ViewModel(){
    private val _mediaItems=MutableLiveData<Resource<List<Song>>>()
    val mediaItems:LiveData<Resource<List<Song>>> =_mediaItems

    val isConnected=musicServiceConnection.isConnectes
    val networkError=musicServiceConnection.networkError
    val curPlayingSong=musicServiceConnection.curPlaySong
    val playbackState=musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.suscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
            val items=children.map {
                Song(
                    it.mediaId!!,
                    it.description.title.toString(),
                    it.description.subtitle.toString(),
                    it.description.mediaUri.toString(),
                    it.description.iconUri.toString(),
                )
            }
                _mediaItems.postValue(Resource.succes(items))
            }
        })




    }
    fun SkipNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }
    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }
    fun seekto(pos:Long){
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem:Song,toggle:Boolean=false){
        val isPrepared=playbackState.value?.isPrepared?:false
        if(isPrepared &&mediaItem.mediaId==curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let {
                when{
                    it.isPlaying->if(toggle)musicServiceConnection.transportControls.pause()
                    it.isPlayEnabled-> musicServiceConnection.transportControls.play()
                    else->Unit
                }
            }
        }
        else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId,null)
        }
    }
    override fun onCleared() {
        musicServiceConnection.unsuscribe(MEDIA_ROOT_ID,object :MediaBrowserCompat.SubscriptionCallback(){})
    }
}