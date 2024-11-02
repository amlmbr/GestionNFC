package ma.ensa.projet.splash

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class NFCArcsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.BLUE
    }

    private var animationProgress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1500
        interpolator = DecelerateInterpolator()
        addUpdateListener { animation ->
            animationProgress = animation.animatedValue as Float
            invalidate()
        }
    }

    fun startAnimation() {
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Calculate animation thresholds for each arc
        val largeArcThreshold = animationProgress
        val mediumArcThreshold = maxOf(0f, animationProgress - 0.33f) * 1.5f
        val smallArcThreshold = maxOf(0f, animationProgress - 0.66f) * 3f

        // Draw arcs from largest to smallest with delays
        if (largeArcThreshold > 0f) {
            drawArc(canvas, centerX, centerY, 200f * largeArcThreshold)
        }
        if (mediumArcThreshold > 0f) {
            drawArc(canvas, centerX, centerY, 150f * mediumArcThreshold)
        }
        if (smallArcThreshold > 0f) {
            drawArc(canvas, centerX, centerY, 100f * smallArcThreshold)
        }
    }

    private fun drawArc(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        canvas.drawArc(rect, 45f, 90f, false, paint)
    }
}
