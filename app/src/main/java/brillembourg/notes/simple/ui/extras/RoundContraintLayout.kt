package brillembourg.notes.simple.ui.extras

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class RoundContraintLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

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
}