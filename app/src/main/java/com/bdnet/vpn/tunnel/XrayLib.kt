package com.bdnet.vpn.tunnel

object XrayLib {

    init {
        try {
            System.loadLibrary("xray")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun start(config: String, fd: Int?)
    external fun stop()
    external fun pause(): Boolean
    external fun resume(): Boolean
    external fun isRunning(): Boolean
    external fun getStats(): String
}
