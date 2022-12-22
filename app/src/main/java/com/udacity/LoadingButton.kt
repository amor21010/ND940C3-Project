package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates
private const val CIRCLE_offset = 100
private const val DIAMETER = 75f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var width = 0f
    private var height = 0f
    private var btnBackgroundColor = 0
    private var txtColor = 0

    private var valueAnimator = ValueAnimator()

    private val btnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var textMidDistance = 0f
    private var circleAngle = 0f
    private var progress = 0f


    private val buttonNonLoadingLabel = context.getString(R.string.button)
    private val buttonLoadingLabel = context.getString(R.string.loading)
    private var buttonLabel = buttonNonLoadingLabel


    var buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Clicked -> {
                isEnabled = false
            }

            ButtonState.Loading -> {
                buttonLabel = buttonLoadingLabel
                valueAnimator = ValueAnimator.ofFloat(0f, width)
                    .apply {
                        addUpdateListener {
                            val value = animatedValue as Float
                            progress = value
                            circleAngle = value * 360 / width
                            invalidate()
                        }
                        duration = 2000
                        repeatCount = ValueAnimator.INFINITE
                        start()
                    }
            }

            ButtonState.Completed -> {
                valueAnimator.cancel()
                circleAngle = 0f
                progress = 0f
                buttonLabel = buttonNonLoadingLabel
                invalidate()
                isEnabled = true
            }
        }
    }


    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            btnBackgroundColor = getColor(
                R.styleable.LoadingButton_buttonBackgroundColor,
                R.attr.colorPrimary
            )
            txtColor = getColor(
                R.styleable.LoadingButton_textColor,
                Color.WHITE
            )
        }

        txtPaint.apply {
            color = txtColor
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimension(R.dimen.default_text_size)
        }
        textMidDistance = (txtPaint.descent() + txtPaint.ascent()) / 2
        btnPaint.apply {
            color = context.getColor(R.color.colorPrimaryDark)
        }
        circlePaint.apply {
            color = context.getColor(R.color.colorAccent)
        }

    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        super.onDraw(canvas)
        if (canvas == null)
            return
        drawBtnBackground(canvas)
        drawBtnText(canvas)
        drawBtnCircle(canvas)
    }

    private fun drawBtnBackground(canvas: Canvas) {
        canvas.drawColor(btnBackgroundColor)
        canvas.drawRect(0f, 0f, progress, height.toFloat(), btnPaint)
    }

    private fun drawBtnCircle(canvas: Canvas) {
        canvas.save()
        canvas.translate(
            width - CIRCLE_offset - DIAMETER,
            (height - DIAMETER) / 2
        )
        canvas.drawArc(
            0f, 0f, DIAMETER, DIAMETER,
            0f, circleAngle, true, circlePaint
        )
        canvas.restore()
    }

    private fun drawBtnText(canvas: Canvas) {
        canvas.drawText(
            buttonLabel,
            width / 2f,
            height / 2f - textMidDistance,
            txtPaint
        )
    }
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        width = w.toFloat()
        height = h.toFloat()
        setMeasuredDimension(w, h)
    }

}