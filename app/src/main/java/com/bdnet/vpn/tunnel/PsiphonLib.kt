package com.bdnet.vpn.tunnel

object PsiphonLib {

    init {
        try {
            System.loadLibrary("tunpsi")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }
    }

    external fun start(config: PsiphonConfig, fd: Int?)
    external fun stop()
    external fun isRunning(): Boolean
    external fun getServerList(): String
}
