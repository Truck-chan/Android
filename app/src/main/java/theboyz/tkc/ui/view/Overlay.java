package theboyz.tkc.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class Overlay extends View {

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

    public void refresh(){
        invalidate();
    }

    public void setRenderer(){

    }
}
