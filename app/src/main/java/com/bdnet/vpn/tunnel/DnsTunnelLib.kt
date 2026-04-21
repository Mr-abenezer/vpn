package com.bdnet.vpn.tunnel

object DnsTunnelLib {

    init {
        try {
            System.loadLibrary("xdnstt")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun start(host: String, port: Int, fd: Int?)
    external fun stop()
    external fun isRunning(): Boolean
}
