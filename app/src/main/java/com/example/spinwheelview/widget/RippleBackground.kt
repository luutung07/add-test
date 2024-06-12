package com.example.spinwheelview.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.example.spinwheelview.R
import kotlin.math.min


open class RippleBackground : RelativeLayout {
    protected var rippleColor = 0
    protected var rippleStrokeWidth = 0f
    protected var rippleRadius = 0f
    protected var rippleDurationTime = 0
    protected var rippleAmount = 0
    protected var rippleDelay = 0
    protected var rippleScale = 0f
    protected var rippleType = 0
    protected var paint: Paint? = null
    var isRippleAnimationRunning: Boolean = false
        private set
    protected var animatorSet: AnimatorSet? = null
    protected var animatorList: ArrayList<Animator>? = null
    protected var rippleParams: LayoutParams? = null
    protected val rippleViewList = ArrayList<RippleView>()

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return

        requireNotNull(attrs) { "Attributes should be provided to this view," }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleBackground)
        rippleColor = typedArray.getColor(
            R.styleable.RippleBackground_rb_color,
            resources.getColor(R.color.rippelColor)
        )
        rippleStrokeWidth = typedArray.getDimension(
            R.styleable.RippleBackground_rb_strokeWidth,
            resources.getDimension(R.dimen.rippleStrokeWidth)
        )
        rippleRadius = typedArray.getDimension(
            R.styleable.RippleBackground_rb_radius,
            resources.getDimension(R.dimen.rippleRadius)
        )
        rippleDurationTime =
            typedArray.getInt(R.styleable.RippleBackground_rb_duration, DEFAULT_DURATION_TIME)
        rippleAmount =
            typedArray.getInt(R.styleable.RippleBackground_rb_rippleAmount, DEFAULT_RIPPLE_COUNT)
        rippleScale = typedArray.getFloat(R.styleable.RippleBackground_rb_scale, DEFAULT_SCALE)
        rippleType = typedArray.getInt(R.styleable.RippleBackground_rb_type, DEFAULT_FILL_TYPE)
        typedArray.recycle()

        rippleDelay = rippleDurationTime / rippleAmount

        paint = Paint()
        paint!!.isAntiAlias = true
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0f
            paint!!.style = Paint.Style.FILL
        } else paint!!.style = Paint.Style.STROKE
        paint!!.color = rippleColor


    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

    }



    inner class RippleView(context: Context?) : View(context) {
        init {
            this.visibility = INVISIBLE
        }

        override fun onDraw(canvas: Canvas) {
            val radius =
                (min(width.toDouble(), height.toDouble()) / 2).toInt()
            Log.d("TAG", "onDraw: $radius")
            canvas.drawCircle(
                radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth,
                paint!!
            )
        }
    }

    fun startRippleAnimation() {
        if (!isRippleAnimationRunning) {
            for (rippleView in rippleViewList) {
                rippleView.visibility = VISIBLE
            }
            animatorSet!!.start()
            isRippleAnimationRunning = true
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning) {
            animatorSet!!.end()
            isRippleAnimationRunning = false
        }
    }

    companion object {
        private const val DEFAULT_RIPPLE_COUNT = 6
        private const val DEFAULT_DURATION_TIME = 3000
        private const val DEFAULT_SCALE = 6.0f
        private const val DEFAULT_FILL_TYPE = 0
    }
}