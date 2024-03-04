package theboyz.tkc.comm

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.lang.IllegalStateException

class SharedMemory(var socket: BluetoothSocket, var onDataChanged: (index: Int) -> Unit) {
    private val TAG = "SharedMemory"
    private var _helper: ConnectionHelper
    private var _mData = Array(128) { ByteArray(4) } // a 128x4 matrix

    init {
        if (!socket.isConnected){
            throw IllegalStateException("Socket must be connected.")
        }

        _helper = ConnectionHelper(socket) {
                data: ByteArray,
                _: Int ->
            val pos = data[0].toInt()
            if (pos > 127) { //top 127 are reserved for now
                Log.e(TAG, "OnPacket: received an index out of bounds")
                return@ConnectionHelper
            }

            _mData[pos][0] = data[1]
            _mData[pos][1] = data[2]
            _mData[pos][2] = data[3]
            _mData[pos][3] = data[4] //not the best way I know, but Im too lazy to fix it xD

            onDataChanged(pos)
        }
    }

    operator fun get(i: Int) : Int{
        val k: Int
        val arr = _mData[i]
        k = (arr[0].toInt()) or
                (arr[1].toInt() shr 8) or
                (arr[1].toInt() shr 16) or
                (arr[1].toInt() shr 24)
        return k
    }

    operator fun set(i: Int, v: Int) {
        val arr = _mData[i]
        arr[0] = v.shr(0).toByte()
        arr[1] = v.shr(8).toByte()
        arr[2] = v.shr(16).toByte()
        arr[3] = v.shr(24).toByte()


        _helper.send(Packet(byteArrayOf(
            i.toByte(),
            arr[0],
            arr[1],
            arr[2],
            arr[3]
        )))
    }


    fun refresh(){
        _helper.send((255).toByte().packet)
    }
}