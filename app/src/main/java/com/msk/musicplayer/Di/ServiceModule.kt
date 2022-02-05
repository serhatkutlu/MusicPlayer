package com.msk.musicplayer.Di

import android.content.Context

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.msk.musicplayer.Data.Entities.MusicDatabase
import com.msk.musicplayer.ExoPlayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    @Singleton
    @Provides
    fun provideMusiceServiceConnection(
    @ApplicationContext context:Context
    )=MusicServiceConnection(context)

    @ServiceScoped
    @Provides
    fun provideMusicDatabase()=MusicDatabase()

    @ServiceScoped
    @Provides
    fun provideAudioAttributes()=AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        AudioAttributes:AudioAttributes
    )=SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes,true)
        setHandleAudioBecomingNoisy(true)
    }

    @Provides
    @ServiceScoped
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    )=DefaultDataSourceFactory(context,Util.getUserAgent(context,"music Player"))

}