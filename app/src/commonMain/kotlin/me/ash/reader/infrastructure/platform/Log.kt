package me.ash.reader.infrastructure.platform

object Log {
    fun d(tag: String, msg: String) = println("DEBUG: [$tag] $msg")
    fun i(tag: String, msg: String) = println("INFO: [$tag] $msg")
    fun w(tag: String, msg: String) = println("WARN: [$tag] $msg")
    fun e(tag: String, msg: String, tr: Throwable? = null) {
        println("ERROR: [$tag] $msg")
        tr?.printStackTrace()
    }
}
