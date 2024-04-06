package theboyz.tkc

import theboyz.tkc.comm.packet
import uni.proj.ec.command

class Communicator {
    companion object {
        fun send(cmd: String) : Unit {
            Constants.Connection.send(cmd.command.packet)
        }

    }
}