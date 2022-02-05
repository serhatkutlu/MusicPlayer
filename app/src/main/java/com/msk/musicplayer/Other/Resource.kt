package com.msk.musicplayer.Other

//this class specifies the state of the data
data class Resource<out T> (val status:Status,val data:T?,val message:String?){
companion object{
    fun <T> succes(data:T?)=Resource(Status.SUCCES,data,null)
    fun<T> error(message: String?,data:T?)=Resource(Status.ERROR,data,message)
    fun<T> loading(data: T?)=Resource(Status.LOADING,data,null)
}


}
enum class Status{
    SUCCES,
    ERROR,
    LOADING
}