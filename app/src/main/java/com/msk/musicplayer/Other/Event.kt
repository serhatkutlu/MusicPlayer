package com.msk.musicplayer.Other

open class Event<out T>(private val data:T)
{
    var hasBeenHandled=false
        private set
    fun getcontentIfNotHandled():T?{
        return if (hasBeenHandled){
            null
        }else{
            hasBeenHandled=true
            data
        }
    }

}