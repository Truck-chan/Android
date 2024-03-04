package theboyz.tkc.ui.component

import android.util.Log
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

private const val TAG = "UserPreview"

data class Avatar<T>(val name: String,val x: Float,val y: Float,val img: Int, val data: T);

@Composable
fun <T> UsersPreview(
    ripple: Boolean,
    pulse: Boolean = false,
    users: List<Avatar<T>>,
    avatarRadius: Float = 60f,
    centerRadius: Float = 60f,
    rippleSpacing: Float = 60f,
    rippleCount: Int = 5,
    rippleWidth: Float = 3f,
    rippleDuration: Int = 1000,
    rippleEasing: Easing = LinearEasing,
    fontStyle: TextStyle = TextStyle(
        color = Color.White,
        fontSize = TextUnit(24f, TextUnitType.Sp),
        fontWeight = FontWeight(400),
    ),
    onClick: (item: Int) -> Unit,
){
    val textMeasurer = rememberTextMeasurer();

    Box (modifier = Modifier.fillMaxSize()){
        val infiniteTransition = rememberInfiniteTransition(label = "ii")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                // Infinitely repeating a 1000ms tween animation using default easing curve.
                animation = tween(
                    if (pulse && !ripple) rippleDuration * rippleCount / 2 else rippleDuration,
                    easing = rippleEasing
                ),
                // After each iteration of the animation (i.e. every 1000ms), the animation will
                // start again from the [initialValue] defined above.
                // This is the default [RepeatMode]. See [RepeatMode.Reverse] below for an
                // alternative.
                repeatMode = RepeatMode.Restart
            ), label = ""
        )

        var width: Float = -1f
        var height: Float = -1f

        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        Log.i(TAG, "UsersPreview: ${it.x} + ${it.y}")
                        for (i in users.indices) {
                            val k = users[i];
                            val pX =
                                width / 2 + (k.x - 0.5f) * 2 * ((rippleCount - 1) * rippleSpacing + centerRadius)
                            val pY =
                                height / 2 + (k.y - 0.5f) * 2 * ((rippleCount - 1) * rippleSpacing + centerRadius)
                            if (sqrt(((pX - it.x) * (pX - it.x) + (pY - it.y) * (pY - it.y)).toDouble()) < avatarRadius) {
                                onClick(i)
                            }
                        }
                    }
                )
            }) {

            width = this.size.width;
            height = this.size.height;

            for (i in 0..<rippleCount) {
                drawCircle(
                    if (pulse && !ripple)
                        Color.Gray.copy(
                            alpha = if ((scale * rippleCount).toInt() == i) min(abs((scale * rippleCount - i - 0.5f) * 2 + 1) , 1f) else 1f,
                        )
                    else
                        Color.Gray.copy(
                            alpha = if (i < rippleCount - 1 || !ripple) 1f else 1f - scale,
                        ),
                    radius = centerRadius + rippleSpacing * i + rippleSpacing * (if (ripple) scale else 1.0f),
                    style = Stroke(
                        width = rippleWidth,
                    ),
                )
            }

            for (i in users){
                //draw the avatar
                val X = this.center.x + (i.x - 0.5f) * 2 * ((rippleCount-1) * rippleSpacing + centerRadius)
                val Y = this.center.y + (i.y - 0.5f) * 2 * ((rippleCount-1) * rippleSpacing + centerRadius)
                drawCircle(
                    Color.Red,
                    radius = avatarRadius,
                    center = Offset(X, Y),
                )

                val textSize = textMeasurer.measure(i.name , fontStyle).size

                drawText(
                    textMeasurer ,
                    text = i.name,
                    topLeft = Offset(X - textSize.width / 2f, Y + avatarRadius),
                    style = fontStyle,
                )

            }
        }
    }
}
