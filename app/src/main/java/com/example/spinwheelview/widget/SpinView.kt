package com.example.spinwheelview.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class SpinView constructor(
    private val ctx: Context,
    attributeSet: AttributeSet?
) : RippleBackground(ctx, attributeSet) {

    companion object {
        private const val TAG = "SpinView"
        private const val RADIUS_OFF_SET = 120f
    }

    private val fingers: HashMap<Int, Pair< Pair<PointF, Int> , RippleView > > = HashMap()

    private val paintText = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = 100f
    }

    private val paintLine = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 20f
        textSize = 100f
        strokeCap = Paint.Cap.ROUND
    }

    private var type: SPIN_TYPE = SPIN_TYPE.COUPLE

//    private var animation: Animation? = null

    private var randomColor: Random = Random()

    // choose
    private var sizeWinner = 3
    private var setRandom = hashSetOf<Int>()

    // rank
    private var mapRank = hashMapOf<PointF, Int>()

    // couple
    private var sizeCouple = 3
    private var mapCouple = hashMapOf<Int, List<PointF>>()

    private var startTime = 0L
    private var isEnd = false

    private var mapAnimation: HashMap<Pair<Float, Float>, Animation?> = hashMapOf()

    init {

    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        fingers.forEach { (t, u) ->
            rippleParams = LayoutParams(
                40,
                40
            )
            rippleParams!!.setMargins(u.first.first.x.toInt(), u.first.first.y.toInt(), 0, 0)
            animatorSet = AnimatorSet()
            animatorSet!!.interpolator = AccelerateDecelerateInterpolator()
            animatorList = ArrayList()
            removeView(u.second)
            for (i in 0 until rippleAmount) {
                Log.d(TAG, "dispatchDraw: ${u.second}")
                val rippleView = u.second
                addView(rippleView, rippleParams)
                rippleViewList.add(rippleView)
                val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale)
                scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
                scaleXAnimator.repeatMode = ObjectAnimator.RESTART
                scaleXAnimator.startDelay = (i * rippleDelay).toLong()
                scaleXAnimator.setDuration(rippleDurationTime.toLong())
                animatorList!!.add(scaleXAnimator)
                val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale)
                scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
                scaleYAnimator.repeatMode = ObjectAnimator.RESTART
                scaleYAnimator.startDelay = (i * rippleDelay).toLong()
                scaleYAnimator.setDuration(rippleDurationTime.toLong())
                animatorList!!.add(scaleYAnimator)
                val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f)
                alphaAnimator.repeatCount = ObjectAnimator.INFINITE
                alphaAnimator.repeatMode = ObjectAnimator.RESTART
                alphaAnimator.startDelay = (i * rippleDelay).toLong()
                alphaAnimator.setDuration(rippleDurationTime.toLong())
                animatorList!!.add(alphaAnimator)
            }

            animatorSet!!.playTogether(animatorList)
            for (rippleView in rippleViewList) {
                rippleView.visibility = VISIBLE
            }
            animatorSet!!.start()
//            if (isEnd) {
//                when {
//                    type == SPIN_TYPE.CHOOSE -> {
//                        if (setRandom.contains(t)) {
//                            canvas.drawText("Winner", u.first.x, u.first.y, paintText)
//                        }
//                    }
//
//                    type == SPIN_TYPE.RANK -> {
//                        if (mapRank.contains(u.first)) {
//                            canvas.drawText(
//                                "Rank ${(mapRank[u.first] ?: 0) + 1}",
//                                u.first.x,
//                                u.first.y,
//                                paintText
//                            )
//                        }
//                    }
//                }
//            }
        }
//        if (isEnd && type == SPIN_TYPE.COUPLE) {
//            mapCouple.forEach { t, u ->
//                val path = Path()
//                path.moveTo(u[0].x, u[0].y)
//
//                for (i in 1 until u.size) {
//                    path.lineTo(u[i].x, u[i].y)
//                }
//
//                path.close()
//                canvas.drawPath(path, paintLine)
//            }
//        }
    }

    fun View?.removeSelf() {
        this ?: return
        val parentView = parent as? ViewGroup ?: return
        parentView.removeView(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                setActionDown(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                setActionUp(event)
            }

            MotionEvent.ACTION_MOVE -> {
                if (isEnd) return false
                setActionMove(event)
            }

            MotionEvent.ACTION_CANCEL -> {
                resetData()
            }
        }
        invalidate()
        return true
    }

    private fun resetData() {
        isEnd = false
        startTime = 0
        fingers.clear()
        mapRank.clear()
        setRandom.clear()
        mapCouple.clear()
        mapAnimation.clear()
        removeAllViews()
    }

    private fun setActionDown(event: MotionEvent) {
        val index = event.actionIndex
        val id = event.getPointerId(index)

        val finger = fingers[id]
        if (finger == null) {
            fingers[id] = Pair(
                Pair(PointF(event.getX(index), event.getY(index)),
                    Color.rgb(
                        randomColor.nextInt(256),
                        randomColor.nextInt(256),
                        randomColor.nextInt(256)
                    )),
                RippleView(ctx)
            )
        }
        when (type) {
            SPIN_TYPE.CHOOSE, SPIN_TYPE.COUPLE -> {
                if (fingers.size == sizeWinner + 1) {
                    startTime = System.currentTimeMillis()
                }
            }

            SPIN_TYPE.RANK -> {
                if (fingers.size == 2) {
                    startTime = System.currentTimeMillis()
                }
            }

            else -> {
                // nothing
            }
        }
    }

    private fun setActionUp(event: MotionEvent) {
        val index = event.actionIndex
        val id = event.getPointerId(index)
        val finger = fingers[id]
        if (finger != null) {
            if (isEnd) {
                postDelayed({
                    resetData()
                    invalidate()
                }, 500)
            } else {
                finger.second.removeSelf()
                fingers.remove(id)
            }
        }
    }

    private fun setActionMove(event: MotionEvent) {
        val count = event.pointerCount
        for (index in 0 until count) {
            var finger = fingers[event.getPointerId(index)]
            finger =
                Pair(
                    Pair(PointF(event.getX(index), event.getY(index)),
                        Color.rgb(
                            randomColor.nextInt(256),
                            randomColor.nextInt(256),
                            randomColor.nextInt(256)
                        )),
                    RippleView(ctx)
                )
            fingers[event.getPointerId(index)] = finger
        }

        if (startTime != 0L && System.currentTimeMillis() - startTime > 2000) {
            when {
                type == SPIN_TYPE.CHOOSE -> {
                    typeChoose()
                }

                type == SPIN_TYPE.COUPLE -> {
                    typeCouple()
                }

                type == SPIN_TYPE.RANK -> {
                    typeRank()
                }
            }

            isEnd = true
        }
    }

    private fun typeChoose() {
        val randomBound = fingers.keys.toHashSet()
        for (i in 0 until sizeWinner) {
            val randomValue = randomBound.random()
            setRandom.add(randomValue)
            randomBound.remove(randomValue)
        }
    }

    private fun typeRank() {
        val randomBound = fingers.keys.toHashSet()
        fingers.forEach { (t, u) ->
            val randomValue = randomBound.random()
//            mapRank[u.first] = randomValue
            randomBound.remove(randomValue)
        }
    }

    private fun typeCouple() {
        fingers.forEach { k, v ->
            val valueRandom = ThreadLocalRandom.current().nextInt(sizeCouple)
            if (mapCouple.contains(valueRandom)) {
                val currentList = mapCouple[valueRandom]?.toMutableList() ?: arrayListOf()
//                currentList.add(v.first)
                mapCouple[valueRandom] = currentList
            } else {
                val newList = arrayListOf<PointF>()
//                newList.add(v.first)
                mapCouple[valueRandom] = newList
            }
        }
    }

    fun setType(type: SPIN_TYPE){
        this@SpinView.type = type
    }
}