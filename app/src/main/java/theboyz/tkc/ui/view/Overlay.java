package theboyz.tkc.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import theboyz.tkc.ip.utils.GlobalParameters;
import theboyz.tkc.ui.vec2;

public class Overlay extends View {
    private static final String TAG = "Overlay";
    interface OverlayRenderer {
        void Render(Canvas canvas);
    }

    public Overlay(Context context) {
        super(context);
    }

    public Overlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Overlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Overlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    int focus = -1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        vec2 pos = new vec2(event.getX() , event.getY());
        vec2 image = selfToImageCoords(pos);
        //Log.i(TAG, "onTouchEvent: Position = " + pos.toString());
        //Log.i(TAG, "onTouchEvent: img      = " + image.toString());

        if (event.getAction() == MotionEvent.ACTION_DOWN){
            for (int i = 0;i < GlobalParameters.clipPoints.length;i++){
                vec2 point = GlobalParameters.clipPoints[i];
                vec2 screen = imageToSelfCoords(point);
                float distance = screen.distance(pos);
                //Log.i(TAG, "onTouchEvent: Distance: " + distance);
                if (distance < 45){
                    focus = i;
                    //Log.i(TAG, "onTouchEvent: Focus: " + i);
                    return true;
                }
            }
        }
        else if ((event.getAction() & MotionEvent.ACTION_MOVE) != 0){
            if (focus == -1) return false;
            if (image.x < 0) image.x = 0;
            if (image.x > 1) image.x = 1;
            if (image.y < 0) image.y = 0;
            if (image.y > 1) image.y = 1;

            GlobalParameters.clipPoints[focus] = image;
            invalidate();
            return true;
        }else if ((event.getAction() & MotionEvent.ACTION_UP) != 0){
            focus = -1;
            return true;
        }

        return false;
    }

    private vec2 boxRanges = new vec2();
    private int image_width;
    private int image_height;
    public void refresh(int width , int height){
        image_width = width;
        image_height = height;

        //calculate box area
        int mWidth = getWidth();
        int mHeight = getHeight();

        boxRanges.x = mWidth / 2.0f - width * ((float) mHeight / height) / 2.0f;
        boxRanges.y = mWidth / 2.0f + width * ((float) mHeight / height) / 2.0f;

        invalidate();
    }

    private vec2 imageToSelfCoords(vec2 r){
        return new vec2(
                r.x * image_width * ((float) getHeight() / image_height) + boxRanges.x,
                r.y * getHeight()
        );
    }

    private vec2 selfToImageCoords(vec2 s){
        return new vec2(
                (s.x - boxRanges.x) / (image_width * ((float) getHeight() / image_height)),
                s.y / getHeight()
        );
    }
    Paint borderPaint = new Paint();
    Paint pointsPaint = new Paint();

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        borderPaint.setColor(0xffffffff);
        borderPaint.setStrokeWidth(4);

        boolean enabled = isEnabled();

        if (enabled) {
            for (int i = 1; i < GlobalParameters.clipPoints.length + 1; i++) {
                vec2 start = imageToSelfCoords(GlobalParameters.clipPoints[i - 1]);
                vec2 end   = imageToSelfCoords(GlobalParameters.clipPoints[i % GlobalParameters.clipPoints.length]);
                canvas.drawLine(start.x , start.y , end.x , end.y , borderPaint);
            }
        }

        for (int i = 0;i < GlobalParameters.clipPoints.length;i++){
            vec2 vec = GlobalParameters.clipPoints[i];
            vec2 loc = imageToSelfCoords(vec);

            pointsPaint.setColor(0xff454545);
            if (focus == i){
                pointsPaint.setColor(0xff450000);
            }
            pointsPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(loc.x , loc.y , 30 , pointsPaint);
            pointsPaint.setColor(0xff808080);
            pointsPaint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(loc.x , loc.y , 45 , pointsPaint);
        }


    }
}
