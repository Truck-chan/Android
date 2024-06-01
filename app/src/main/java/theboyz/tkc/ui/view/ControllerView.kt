package theboyz.tkc.ui.view


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import theboyz.tkc.R
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

interface OnOvalMoveListener {
    fun onOvalMove(x: Float, y: Float)
}

class ControllerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val outerPaint = Paint()
    private val innerPaint = Paint()
    private var ovalX = 0f
    private var ovalY = 0f
    private var ovalWidthPercent = 0.5f
    private var ovalHeightPercent = 0.5f
    private val handler = Handler(Looper.getMainLooper())
    private val moveBackToCenterRunnable = MoveBackToCenterRunnable()

    var onOvalMoveListener: OnOvalMoveListener? = null

    init {
        // Default outer oval color
        outerPaint.color = Color.GREEN
        outerPaint.style = Paint.Style.STROKE
        outerPaint.strokeWidth = 8f

        // Default inner oval color
        innerPaint.color = Color.GREEN
        innerPaint.style = Paint.Style.FILL

        // Read custom attributes
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ControllerView,
            0, 0
        ).apply {
            try {
                ovalWidthPercent = getFloat(R.styleable.ControllerView_width_percent_of_view, 0.5f)
                ovalHeightPercent = getFloat(R.styleable.ControllerView_height_percent_of_view, 0.5f)
                innerPaint.color = getColor(R.styleable.ControllerView_center_oval_color, Color.GREEN)
                outerPaint.color = getColor(R.styleable.ControllerView_outer_oval_color, Color.GREEN)
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft.toFloat()
        val paddingTop = paddingTop.toFloat()
        val paddingRight = paddingRight.toFloat()
        val paddingBottom = paddingBottom.toFloat()

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        // Draw the outer oval
        val outerOvalRadius = contentHeight / 2
        canvas.drawOval(paddingLeft, paddingTop, paddingLeft + contentWidth, paddingTop + contentHeight, outerPaint)

        // Calculate the size of the central oval
        val ovalWidth = contentWidth * ovalWidthPercent
        val ovalHeight = contentHeight * ovalHeightPercent

        // Ensure the central oval stays within bounds initially
        if (ovalX == 0f && ovalY == 0f) {
            ovalX = paddingLeft + contentWidth / 2
            ovalY = paddingTop + contentHeight / 2
        }

        // Draw the central oval
        canvas.drawOval(ovalX - ovalWidth / 2, ovalY - ovalHeight / 2, ovalX + ovalWidth / 2, ovalY + ovalHeight / 2, innerPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val paddingLeft = paddingLeft.toFloat()
        val paddingTop = paddingTop.toFloat()
        val paddingRight = paddingRight.toFloat()
        val paddingBottom = paddingBottom.toFloat()

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val ovalWidth = contentWidth * ovalWidthPercent
        val ovalHeight = contentHeight * ovalHeightPercent

        val centerX = paddingLeft + contentWidth / 2
        val centerY = paddingTop + contentHeight / 2

        val maxRadiusX = contentWidth / 2 - ovalWidth / 2
        val maxRadiusY = contentHeight / 2 - ovalHeight / 2

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Stop the movement to center if user touches again
                handler.removeCallbacks(moveBackToCenterRunnable)

                // Update the position of the central oval based on touch event
                ovalX = event.x
                ovalY = event.y

                // Calculate the distance from the center
                val dx = ovalX - centerX
                val dy = ovalY - centerY

                val distanceX = dx
                val distanceY = dy

                val distance = hypot(distanceX, distanceY)

                if (distance > maxRadiusX || distance > maxRadiusY) {
                    val angle = Math.atan2(dy.toDouble(), dx.toDouble())
                    ovalX = (centerX + maxRadiusX * Math.cos(angle)).toFloat()
                    ovalY = (centerY + maxRadiusY * Math.sin(angle)).toFloat()
                }
                // Redraw the view
                invalidate()

                // Trigger the listener with normalized values
                onOvalMoveListener?.onOvalMove(
                    (ovalX - (paddingLeft + contentWidth / 2)) / (contentWidth / 2 - ovalWidth / 2),
                    (ovalY - (paddingTop + contentHeight / 2)) / (contentHeight / 2 - ovalHeight / 2)
                )
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Start moving back to the center when touch is released
                handler.post(moveBackToCenterRunnable)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private inner class MoveBackToCenterRunnable : Runnable {
        override fun run() {
            val paddingLeft = paddingLeft.toFloat()
            val paddingTop = paddingTop.toFloat()
            val paddingRight = paddingRight.toFloat()
            val paddingBottom = paddingBottom.toFloat()

            val contentWidth = width - paddingLeft - paddingRight
            val contentHeight = height - paddingTop - paddingBottom

            val centerX = paddingLeft + contentWidth / 2
            val centerY = paddingTop + contentHeight / 2

            val dx = (centerX - ovalX) / 10
            val dy = (centerY - ovalY) / 10

            // Move closer to the center
            ovalX += dx
            ovalY += dy

            // Redraw the view
            invalidate()

            // Trigger the listener with normalized values
            onOvalMoveListener?.onOvalMove(
                (ovalX - (paddingLeft + contentWidth / 2)) / (contentWidth / 2),
                (ovalY - (paddingTop + contentHeight / 2)) / (contentHeight / 2)
            )

            // Continue moving until the oval is close enough to the center
            if (Math.abs(centerX - ovalX) > 1 || Math.abs(centerY - ovalY) > 1) {
                handler.postDelayed(this, 16) // Run again after 16ms (approx 60fps)
            } else {
                // Ensure it is exactly centered
                ovalX = centerX
                ovalY = centerY
                onOvalMoveListener?.onOvalMove(0f, 0f)
                invalidate()
            }
        }
    }
}