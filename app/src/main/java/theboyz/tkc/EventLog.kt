package theboyz.tkc

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class EventLog {

    data class EventObject(
        val text: String,
        val time: LocalDateTime,
        val color: Int,
        )

    class TextViewVH(
        ctx: Context
    ): ViewHolder(TextView(ctx)) {
        init {
            val text: TextView = super.itemView as TextView
            text.textSize = 18f
            text.setTextColor(Color.argb(255,255,255,255))
        }
    }

    companion object {
        private const val TAG = "EventLog"

        var LogItems: ArrayList<EventObject> = ArrayList()
        var viewObject: RecyclerView? = null

        fun init(viewObject: RecyclerView) {
            EventLog.viewObject = viewObject

            viewObject.adapter = object: Adapter<TextViewVH>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewVH {
                    return TextViewVH(parent.context)
                }

                override fun onBindViewHolder(holder: TextViewVH, position: Int) {
                    val text: TextView = holder.itemView as TextView
                    val data = LogItems[position]
                    val formattedTime = "[" + data.time.format(
                        DateTimeFormatter.ofPattern(
                            "h:mm:ss a", Locale.US
                        )
                    ) + "] "

                    val str = SpannableString(formattedTime + data.text)
                    str.setSpan(ForegroundColorSpan(Color.argb(255,255,255,255)) , 0 , formattedTime.length , Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    str.setSpan(ForegroundColorSpan(data.color) , formattedTime.length , str.length , Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

                    text.text = str
                }

                override fun getItemCount(): Int {
                    return LogItems.size
                }
            }
        }

        fun log(text: String, color: Int){

            LogItems.add(
                EventObject(
                    text,
                    LocalDateTime.now(),
                    color
                )
            )

            if (!Looper.getMainLooper().isCurrentThread) { //not on the UI thread
                viewObject?.post { //run it on the UI thread
                    viewObject?.adapter?.notifyItemInserted(LogItems.size - 1)
                    Log.i(TAG, "log: $text")
                }
                return
            }

            viewObject?.adapter?.notifyItemInserted(LogItems.size - 1)
            Log.i(TAG, "log: $text")
        }

        fun i(text: String){
            log(text , Color.argb(255 , 0 , 255 , 0))
        }

        fun w(text: String){
            log(text , Color.argb(255 , 252, 140, 3))
        }

        fun e(text: String){
            log(text , Color.argb(255 , 252, 3, 3))
        }

        fun m(text: String){
            log(text , Color.argb(255 , 252, 255, 255))
        }
    }
}