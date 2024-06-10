package com.example.spinwheelview.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.Random

class MultipleCircle constructor(
    ctx: Context,
    attributeSet: AttributeSet?
) : View(ctx, attributeSet) {

    companion object {
        private const val TAG = "MultipleCircle"
    }

    private val fingers: HashMap<Int, Pair<PointF, Int>> = HashMap()

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val paintText = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 100f
    }

    var rnd: Random = Random()

    var randomWinner = Random()

    var startTime = 0L
    var isEnd = false

    init {

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        fingers.forEach { t, u ->
            canvas.drawCircle(u.first.x, u.first.y, 200f, paint.apply {
                color = u.second
            })

            if (isEnd && t == 1) {
                canvas.drawText("Winner", u.first.x, u.first.y, paintText)
                Log.d(TAG, "draw: dasdadasdasd")
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.actionMasked
        Log.d(TAG, "onTouchEvent: $action $isEnd $startTime")
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            val index = event.actionIndex
            val id = event.getPointerId(index)
            val finger = fingers[id]
            if (finger == null) {
                fingers[id] = Pair(
                    PointF(event.getX(index), event.getY(index)),
                    Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                )
            }
            if (fingers.size == 2) {
                startTime = System.currentTimeMillis()
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            val index = event.actionIndex
            val id = event.getPointerId(index)
            val finger = fingers[id]
            if (finger != null) {
                Log.d(TAG, "finger: $isEnd")
                if (isEnd) {
                    postDelayed({
                        isEnd = false
                        startTime = 0
                        fingers.clear()
                        invalidate()
                    }, 300)
                } else {
                    fingers.remove(id)
                }
            }

        } else if (action == MotionEvent.ACTION_MOVE) {
//            if (isEnd) return false
            val count = event.pointerCount
            for (index in 0 until count) {
                var finger = fingers[event.getPointerId(index)]
                finger =
                    Pair(PointF(event.getX(index), event.getY(index)), finger?.second ?: Color.RED)
                fingers[event.getPointerId(index)] = finger
            }
            Log.d(
                TAG,
                "[start = $startTime] -- [current = ${System.currentTimeMillis()}] -- [minus = ${System.currentTimeMillis() - startTime}]"
            )
            if (startTime != 0L && System.currentTimeMillis() - startTime > 2000) {
                isEnd = true
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            startTime = 0
            fingers.clear()
            isEnd = false
        }

        // do something with fingers and invalidate();
        invalidate()
        return true
    }

    private fun checkBoundPoint(point: PointF) {

    }
}