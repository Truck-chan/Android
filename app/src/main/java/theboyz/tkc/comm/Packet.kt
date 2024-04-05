package theboyz.tkc.comm

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