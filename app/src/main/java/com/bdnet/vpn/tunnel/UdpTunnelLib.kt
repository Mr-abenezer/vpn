package com.bdnet.vpn.tunnel

object UdpTunnelLib {

    init {
        try {
            System.loadLibrary("udpq")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun start(host: String, port: Int, fd: Int?)
    external fun stop()
    external fun isRunning(): Boolean
}
