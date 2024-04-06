package theboyz.tkc.comm

import uni.proj.ec.Command

data class Packet(var data: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Packet

        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

val Byte.packet: Packet
    get() {
        return Packet(byteArrayOf(this))
    }

val String.packet: Packet
    get() {
        return Packet(this.toByteArray())
    }

val Command.packet: Packet
    get() {
        //TODO: fix ascii related thing ..
        if (cmd == null)
            return Packet(byteArrayOf())
        val bytes: ArrayList<Byte> = ArrayList()
        bytes.add(cmd.length.toByte())
        for (c in cmd){
            bytes.add(c.code.toByte())
        }

        bytes.add(arguments.size.toByte())
        for (i in arguments.indices){
            val arg: Command.CommandArgument = arguments[i] ?: return Packet(byteArrayOf())

            if (arg.name == null || arg.value == null){
                return Packet(byteArrayOf())
            }
            bytes.add(arg.name.length.toByte())
            when (arg.value) {
                is Float -> {
                    val bits = arg.value.toBits()
                    bytes.add(4)
                    bytes.add(bits.shr(0).and(0xff).toByte())
                    bytes.add(bits.shr(8).and(0xff).toByte())
                    bytes.add(bits.shr(16).and(0xff).toByte())
                    bytes.add(bits.shr(24).and(0xff).toByte())
                }

                is Int -> {
                    val bits = arg.value
                    bytes.add(4)
                    bytes.add(bits.shr(0).and(0xff).toByte())
                    bytes.add(bits.shr(8).and(0xff).toByte())
                    bytes.add(bits.shr(16).and(0xff).toByte())
                    bytes.add(bits.shr(24).and(0xff).toByte())
                }

                else -> {
                    val seq = arg.value.toString()
                    bytes.add(seq.length.toByte())
                    for (c in seq){
                        bytes.add(c.code.toByte())
                    }
                }
            }
        }

        return Packet(bytes.toByteArray())
    }