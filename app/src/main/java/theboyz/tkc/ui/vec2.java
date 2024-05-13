package theboyz.tkc.ui;

import androidx.annotation.NonNull;

public class vec2{
    public float x;
    public float y;

    public vec2(){
        x = 0;
        y = 0;
    }

    public vec2(float x , float y){
        this.x = x;
        this.y = y;
    }


    public float distance(vec2 other){
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + x + " , " + y + "]";
    }
}
