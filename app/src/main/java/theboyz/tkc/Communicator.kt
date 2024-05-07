package theboyz.tkc

import theboyz.tkc.comm.packet
import uni.proj.ec.Command
import uni.proj.ec.command

class Communicator {
    companion object {
        fun send(cmd: String) : Unit {
            if (Constants.Connection != null) {
                Constants.Connection.send(cmd.command.packet)
            }
        }

        fun send(cmd: Command) : Unit {
            if (Constants.Connection != null) {
                Constants.Connection.send(cmd.packet)
            }
        }
    }
}