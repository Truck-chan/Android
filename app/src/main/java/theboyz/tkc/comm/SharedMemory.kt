package theboyz.tkc.comm

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.lang.IllegalStateException

//it was a good attempt, but alas, I failed, Arduino is too slow for this
interface MemoryListener{
    fun onDataChanged(index: Int) : Unit
    fun onDisconnected() : Unit
    fun onConnected() : Unit
}
class SharedMemory(var socket: BluetoothSocket) {
    private val TAG = "SharedMemory"

    var memSize = 16
    var listener: MemoryListener? = null
    private var _helper: ConnectionHelper
    private var _mData       = Array(memSize) { ByteArray(4) } // a 128x4 matrix
    private var _mDataChange = ByteArray(memSize) { 0 } // a 128x4 matrix

    init {
        if (!socket.isConnected){
            throw IllegalStateException("Socket must be connected.")
        }

        _helper = ConnectionHelper(socket)
        _helper.Reciever = {
                data: ByteArray,
                _: Int ->
            val pos = data[0].toInt() and 0xff

            _mData[pos][0] = data[1]
            _mData[pos][1] = data[2]
            _mData[pos][2] = data[3]
            _mData[pos][3] = data[4] //not the best way I know, but Im too lazy to fix it xD

            Log.i(TAG, "data[0]: ${data[1]}")
            Log.i(TAG, "data[1]: ${data[2]}")
            Log.i(TAG, "data[3]: ${data[3]}")
            Log.i(TAG, "data[3]: ${data[4]}")

            listener?.onDataChanged(pos)
        }
    }

    operator fun get(i: Int) : Int{
        val k: Int
        val arr = _mData[i]
        k =     ((arr[0].toInt() and 0xff)       ) or
                ((arr[1].toInt() and 0xff) shl 8 ) or
                ((arr[2].toInt() and 0xff) shl 16) or
                ((arr[3].toInt() and 0xff) shl 24)
        return k
    }

    operator fun set(i: Int, v: Int) {
        if (get(i) == v){
            return
        }

        _mDataChange[i] = 1
        val arr = _mData[i]
        arr[0] = v.shr(0).toByte()
        arr[1] = v.shr(8).toByte()
        arr[2] = v.shr(16).toByte()
        arr[3] = v.shr(24).toByte()
//        _helper.send(Packet(byteArrayOf(
//            i.toByte(),
//            arr[0],
//            arr[1],
//            arr[2],
//            arr[3],
//            )))
    }

    fun refresh(){
        for (i in 0..<memSize){
            if (_mDataChange[i].toInt() == 1){
                val arr = _mData[i]
                _helper.send(Packet(byteArrayOf(
                    i.toByte(),
                    arr[0],
                    arr[1],
                    arr[2],
                    arr[3],
                )))
                _mDataChange[i] = 0
            }
        }
    }

    var helper : ConnectionHelper = _helper
}