package theboyz.tkc.comm

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.net.Socket
import java.util.Arrays
import java.util.LinkedList
import kotlin.concurrent.thread



class ConnectionHelper(sok: BluetoothSocket, bufferSize: Int = 1024, minReadSize: Int = 5, quantize: Boolean = true, receiver: (data: ByteArray , size: Int) -> Unit) {
    private val TAG = "ConnectionHelper"

    // should the helper wait for the other device to send a ready signal ?
    // if true, then what is the ready byte ?
    private var _waitForReady = 0xff

    private var _connected = true
    val isConnected get() = _connected

    private var _socket = sok
    private var _buffSize = bufferSize
    val bufferSize get() = _buffSize

    private var _minReadSize = minReadSize
    val minReadSize get() = _minReadSize

    private var _quantize = quantize
    private var _recv = receiver

    //worker variables
    private var _packets = LinkedList<Packet>()
    private var _sendQueue = LinkedList<Packet>()

    private var _QueueLock = Any()
    private fun worker(){ //worker thread
        var ready = false

        thread {
            //sender logic
            try {
                val output: OutputStream = _socket.outputStream
                    ?: throw NullPointerException("Failed to obtain output stream")

                while (_connected) {
                    synchronized(_QueueLock){
                        _packets.addAll(_sendQueue)
                        _sendQueue.clear()
                    }

                    if (_waitForReady == -1 || ready || !_quantize) {

                        if (_waitForReady != -1 && _quantize) {
                            output.write(_packets.size)
                            output.flush()
                        }

                        var packet = _packets.removeFirstOrNull()
                        while (packet != null) {
                            output.write(packet.data)
                            output.flush()
                            packet = _packets.removeFirstOrNull()
                        }

                        ready = false
                    }

                }

                output.close()
            } catch (e: Exception){
                Log.e(TAG, "worker: (Output) Exited with error", e)
            }

            _connected = false
        }

        thread {
            //receiver logic
            try {
                val buffer = ByteArray(_buffSize)
                val input: InputStream = _socket.inputStream
                    ?: throw NullPointerException("Failed to obtain output stream")

                var offset = 0
                while (_connected) {
                    offset += input.read(buffer , offset , if (_quantize) _minReadSize - offset else _buffSize - offset)

                    if (offset >= _minReadSize){
                        if (_quantize && _waitForReady != -1){
                            var r = true
                            for (i in 0..<_minReadSize){
                                if (buffer[i] != _waitForReady.toByte()){
                                    r = false
                                    break
                                }
                            }
                            if (r){
                                Thread.sleep(150) //give the HC-05 10 ms to breath
                                ready = true
                                offset = 0

                                continue
                            }
                        }
                        _recv(buffer, _minReadSize)
                        offset = 0
                    }
                }

                input.close()
            } catch (e: Exception){
                Log.e(TAG, "worker: (Input) Exited with error", e)
            }
            _connected = false
        }
    }

    fun send(p: Packet){
        synchronized(_QueueLock) {
            _sendQueue.add(p)
        }
    }

    init {
        if (!sok.isConnected){
            throw IllegalStateException("Socket must be connected.")
        }
        if (_minReadSize > _buffSize)
            throw IllegalArgumentException("Can't have the buffer size lower than the min read size.")
        if (quantize && _minReadSize == 0)
            throw IllegalArgumentException("Quantization requires a min read size to be defined.")
        worker()
    }
}