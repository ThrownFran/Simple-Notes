package brillembourg.notes.simple.ui.extras

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout


open class RoundContraintLayout(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs) {

    private lateinit var mShadow: Paint
    private lateinit var rectF: RectF
    private val path = Path()
    private var cornerRadius = 10f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        resetPath()
    }

    override fun draw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.draw(canvas)
        canvas.restoreToCount(save)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    private fun resetPath() {
        path.reset()
        path.addRoundRect(
            rectF,
            cornerRadius.fromDpToPixel(context),
            cornerRadius.fromDpToPixel(context),
            Path.Direction.CW
        )
        path.close()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //TODO
//        setBackgroundColor(ResourcesCompat.getColor(resources, R.color.transparent, null))
    }

    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        val colors = intArrayOf(
//            Color.BLUE, Color.YELLOW, Color.RED,
//            Color.GREEN, Color.MAGENTA, Color.WHITE
//        )
//
//        val gradient1 = RadialGradient(
//            500f,
//            500f, 400f, colors, null,
//            Shader.TileMode.MIRROR
//        )
//
//        val radielGradientPaint = Paint()
//        radielGradientPaint.isDither = true
//        radielGradientPaint.shader = gradient1
//
//        canvas.drawPaint(radielGradientPaint)
    }
}