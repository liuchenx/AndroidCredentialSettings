package me.liuyichen.android.credential

import android.util.Log

/**
 * CertificateLogger
 *
 * @author Liu Yichen
 * @date 2018/5/5
 */


var CERTIFICATE_LOGGER : (tag : String, msg : String)->Int = {
    tag, msg ->
    Log.d(tag, msg)
}

fun Any.log(tag : String, msg : String) {
    CERTIFICATE_LOGGER(tag, msg)
}


