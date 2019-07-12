package com.qiyou.qcircleview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // test();

        /**
         * 1、  angle >=0     &&  angle <=90     cos sin 正值
         * 2、  angle >90     &&  angle <=180    cos 为负值  sin为正值
         * 3、  angle >=-90   &&  angle <0       cos 为正值  sin为负值
         * 4、  angke >=-180  &&  angle <-90     cos sin 负值
         */

    }

    /**
     * 把圆分成四份
     */
    private void test() {
        for (int i = -180; i <= -90; i += 1) {
            Log.e("测试",
                    i + "<==>" + Math.cos(Math.toRadians(i)) + "<==>" + Math.sin(Math.toRadians(i))
            );
        }
    }
}
