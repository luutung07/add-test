package com.example.spinwheelview.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.spinwheelview.R
import java.util.concurrent.ThreadLocalRandom


class SpinView constructor(
    private val ctx: Context,
    attributeSet: AttributeSet?
) : RippleBackground(ctx, attributeSet) {

    companion object {
        private const val TAG = "SpinView"
        private const val RADIUS_OFF_SET = 120f
    }

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

    // choose
    private var sizeWinner = 2
    private var setRandom = hashSetOf<Int>()

    // rank
    private var mapRank = hashMapOf<PointF, Int>()

    // couple
    private var sizeCouple = 2
    private var mapCouple = hashMapOf<Int, List<PointF>>()

    private var startTime = 0L
    private var isEnd = false

    private var mapViewAnimation: HashMap<Int, Pair<PointF, Pair<AnimatorSet?, List<RippleView>>>> =
        hashMapOf()

    init {

    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        mapViewAnimation.forEach { (k, v) ->
            if (isEnd) {
                when {
                    type == SPIN_TYPE.CHOOSE -> {
                        if (setRandom.contains(k)) {
                            canvas.drawText("Winner", v.first.x, v.first.y, paintText)
                        } else {
                            val data = v.second
                            data.first?.end()
                            data.second.forEach {
                                removeView(it)
                            }
                        }
                    }

                    type == SPIN_TYPE.RANK -> {
                        if (mapRank.contains(v.first)) {
                            val rankNumber = (mapRank[v.first] ?: 0) + 1

                            when (rankNumber) {
                                1 -> {
                                    val bitmap = BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_rank_top1
                                    )
                                    canvas.drawBitmap(
                                        bitmap,
                                        v.first.x - (bitmap.width / 2),
                                        v.first.y - ((DEFAULT_RIPPLE_COUNT - 3) * bitmap.height),
                                        null
                                    );
                                }

                                2 -> {
                                    val bitmap = BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_rank_top2
                                    )
                                    canvas.drawBitmap(
                                        bitmap,
                                        v.first.x - (bitmap.width / 2),
                                        v.first.y - ((DEFAULT_RIPPLE_COUNT - 2) * bitmap.height),
                                        null
                                    );
                                }

                                3 -> {
                                    val bitmap = BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_rank_top3
                                    )
                                    canvas.drawBitmap(
                                        bitmap,
                                        v.first.x - (bitmap.width / 2),
                                        v.first.y - ((DEFAULT_RIPPLE_COUNT - 2) * bitmap.height),
                                        null
                                    );
                                }
                            }

                            canvas.drawText(
                                "Rank $rankNumber",
                                v.first.x,
                                v.first.y,
                                paintText
                            )
                        }
                    }
                }
            }
        }

        if (isEnd && type == SPIN_TYPE.COUPLE) {
            Log.d(TAG, "dispatchDraw: checkasjdbnjasdbnjasbdasdadasdasd")
            mapCouple.forEach {
                Log.d(TAG, "dataCouple ======================================== ${it.key}")
                it.value.forEach {
                    Log.d(TAG, "dataCouple = [${it.x} - ${it.y}]")
                }
            }

            mapViewAnimation.forEach { t, u ->
                Log.d(TAG, "dataAnimatiom ========================= ${t}")
                Log.d(TAG, "dataAnimatiom: [${u.first.x} - ${u.first.y}]")
            }

            mapCouple.forEach { (t, u) ->
                val sameColor = mapColor[t] ?: Color.RED

//                mapViewAnimation.forEach { (k, v) ->
//                    v.second.second.forEach { view ->
//                        if (u.contains(v.first)) {
//                            view.setColor(sameColor)
//                            Log.d(TAG, "checkTrung ${u.find { it == v.first }} k = $k")
//                        } else {
//                            Log.d(TAG, "checkTrung KOKO: $k")
//                            view.setColor(mapColor[k] ?: Color.BLUE)
//                        }
//                    }
//                }

                val path = Path()
                path.moveTo(u[0].x, u[0].y)

                for (i in 1 until u.size) {
                    path.lineTo(u[i].x, u[i].y)
                }

                path.close()
                canvas.drawPath(path, paintLine.apply { color = sameColor })
            }

            mapViewAnimation.forEach { t, u ->
                Log.d(TAG, "getColor: ========================================")
                u.second.second.forEach {
                    Log.d(TAG, "getColor = ${it.getColor()}")
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.actionMasked
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d(TAG, "onTouchEvent: ACTION_DOWN")
                setActionDown(event)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                setActionUp(event)
                Log.d(TAG, "onTouchEvent: ACTION_UP")
            }

            MotionEvent.ACTION_MOVE -> {
                if (isEnd) {
//                    postDelayed({
//                        resetData()
//                    }, 1000)
                    return false
                } else {
                    setActionMove(event)
                }
                Log.d(TAG, "onTouchEvent: ACTION_MOVE")
            }

            MotionEvent.ACTION_CANCEL -> {
                Log.d(TAG, "onTouchEvent: ACTION_CANCEL")
                resetData()
            }
        }
        invalidate()
        return true
    }

    private fun resetData() {
        isEnd = false
        startTime = 0
        mapRank.clear()
        setRandom.clear()
        mapCouple.clear()
        mapColor.clear()
        mapViewAnimation.forEach { (t, u) ->
            u.second.first?.end()
            u.second.second.forEach {
                removeView(it)
            }
        }
        mapViewAnimation.clear()
    }

    @SuppressLint("Recycle")
    private fun addViewAndAnimation(index: Int, pointF: PointF) {
        val rippleParams = LayoutParams(
            SIZE_CIRCLE,
            SIZE_CIRCLE
        )

        rippleParams.leftMargin = pointF.x.toInt() - SIZE_CIRCLE / 2
        rippleParams.topMargin = pointF.y.toInt() - SIZE_CIRCLE / 2

        val animatorSet = AnimatorSet()
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        val animatorList = ArrayList<Animator>()
        val rippleViewList: MutableList<RippleView> = arrayListOf()

        for (i in 0 until rippleAmount) {
            val rippleView = RippleView(context)
            mapColor[index]?.let { rippleView.setColor(it) }
            addView(rippleView, rippleParams)
            rippleViewList.add(rippleView)

            val scaleXAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale)
            scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleXAnimator.repeatMode = ObjectAnimator.RESTART
            scaleXAnimator.startDelay = (i * rippleDelay).toLong()
            scaleXAnimator.setDuration(rippleDurationTime.toLong())
            animatorList.add(scaleXAnimator)

            val scaleYAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale)
            scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleYAnimator.repeatMode = ObjectAnimator.RESTART
            scaleYAnimator.startDelay = (i * rippleDelay).toLong()
            scaleYAnimator.setDuration(rippleDurationTime.toLong())
            animatorList.add(scaleYAnimator)

            val alphaAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f)
            alphaAnimator.repeatCount = ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = ObjectAnimator.RESTART
            alphaAnimator.startDelay = (i * rippleDelay).toLong()
            alphaAnimator.setDuration(rippleDurationTime.toLong())
            animatorList.add(alphaAnimator)
        }

        animatorSet.playTogether(animatorList)
        rippleViewList.forEach {
            it.visibility = View.VISIBLE
        }
        animatorSet.start()
        mapViewAnimation[index] = Pair(pointF, Pair(animatorSet, rippleViewList))
    }

    private fun setActionDown(event: MotionEvent) {
        val index = event.actionIndex
        val id = event.getPointerId(index)

        if (!mapViewAnimation.contains(id)) {
            mapColor[id] = Color.rgb(
                randomColor.nextInt(256),
                randomColor.nextInt(256),
                randomColor.nextInt(256)
            )
            addViewAndAnimation(id, PointF(event.getX(index), event.getY(index)))
        }
        when (type) {
            SPIN_TYPE.CHOOSE, SPIN_TYPE.COUPLE -> {
                if (mapViewAnimation.size == sizeWinner + 1) {
                    startTime = System.currentTimeMillis()
                }
            }

            SPIN_TYPE.RANK -> {
                if (mapViewAnimation.size == 2) {
                    startTime = System.currentTimeMillis()
                }
            }

            else -> {
                // nothing
            }
        }
    }

    private fun setActionUp(event: MotionEvent) {
        if (isEnd) {
            postDelayed({
                resetData()
                invalidate()
            }, 1000)
        } else {
            val index = event.actionIndex
            val id = event.getPointerId(index)
            if (mapViewAnimation.contains(id)) {
                val data = mapViewAnimation[id]!!.second
                data.first?.end()
                data.second.forEach {
                    removeView(it)
                }
            }
        }
    }

    private fun setActionMove(event: MotionEvent) {
        val count = event.pointerCount
        for (index in 0 until count) {
            val id = event.getPointerId(index)
            checkPointToRemoveView(PointF(event.getX(index), event.getY(index)), id)
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

    private fun checkPointToRemoveView(point: PointF, index: Int) {
        if (mapViewAnimation.contains(index)) {
            val animationData = mapViewAnimation[index]
            val currentPoint = animationData?.first
            currentPoint?.let {
                if (
                    point.x > it.x + OFF_LIMIT_REMOVE_VIEW ||
                    point.x < it.x - OFF_LIMIT_REMOVE_VIEW ||
                    point.y > it.y + OFF_LIMIT_REMOVE_VIEW ||
                    point.y < it.y - OFF_LIMIT_REMOVE_VIEW
                ) {
                    animationData.second.first?.end()
                    animationData.second.second.forEach { view ->
                        removeView(view)
                    }
                    mapViewAnimation.remove(index)
                    addViewAndAnimation(index, point)
                }
            }
        }
    }

    private fun typeChoose() {
        val randomBound = mapViewAnimation.keys.toHashSet()
        for (i in 0 until sizeWinner) {
            val randomValue = randomBound.random()
            setRandom.add(randomValue)
            randomBound.remove(randomValue)
        }
    }

    private fun typeRank() {
        val randomBound = mapViewAnimation.keys.toHashSet()
        mapViewAnimation.forEach { (t, u) ->
            val randomValue = randomBound.random()
            mapRank[u.first] = randomValue
            randomBound.remove(randomValue)
        }
        Log.d(TAG, "typeRank: ${mapRank.size}")
    }

    private fun typeCouple() {
        mapViewAnimation.forEach { (k, v) ->
            val valueRandom = ThreadLocalRandom.current().nextInt(sizeCouple)
            if (mapCouple.contains(valueRandom)) {
                val currentList = mapCouple[valueRandom]?.toMutableList() ?: arrayListOf()
                currentList.add(v.first)
                mapCouple[valueRandom] = currentList
            } else {
                val newList = arrayListOf<PointF>()
                newList.add(v.first)
                mapCouple[valueRandom] = newList
            }
        }
    }

    fun setType(type: SPIN_TYPE) {
        this@SpinView.type = type
    }
}