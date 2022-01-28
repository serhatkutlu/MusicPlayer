package com.msk.musicplayer.Data.Entities

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.msk.musicplayer.Other.Constants.SONG_COLLECTION
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MusicDatabase {

    private val firestore=FirebaseFirestore.getInstance()
    private val songCollection=firestore.collection(SONG_COLLECTION)

    suspend fun getAllSong():List<Song>{
        return try {
            songCollection.get().await().toObjects(Song::class.java)

        }catch (e:Exception){
            emptyList()
        }
    }
}