package com.bdnet.vpn.tunnel

object HttpTunnelLib {

    init {
        try {
            System.loadLibrary("fwpuncher")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun start(host: String, port: Int, payload: String?, fd: Int?)
    external fun stop()
    external fun isRunning(): Boolean
}
