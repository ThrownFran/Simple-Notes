package brillembourg.notes.simple.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import brillembourg.notes.simple.ui.extras.RoundContraintLayout


class CanvasBrushView(context: Context, attrs: AttributeSet) :
    RoundContraintLayout(context, attrs) {
    private var drawable: Drawable? = null
    private var mBitmapBrush: Bitmap? = null
    private var mBitmapBrushDimensions: Vector2? = null

    private val mPositions: MutableList<Vector2> = ArrayList(100)

    private class Vector2(val x: Float, val y: Float)

    init {
        // load your brush here
//        mBitmapBrush =
//            ContextCompat.getDrawable(context, R.drawable.blue_creyon_2)?.toBitmap()
//        drawable = BitmapDrawable(resources, mBitmapBrush)
//        mBitmapBrush?.compress(Bitmap.CompressFormat.PNG, 10,mBitmapBr);
//        mBitmapBrushDimensions = Vector2(
//            mBitmapBrush?.width!!.toFloat(),
//            mBitmapBrush?.height!!.toFloat()
//        )

    }



    //
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        background = drawable
//        val paint = Paint()
//        val rect = Rect(0, 0, width, height)
//        canvas.drawBitmap(mBitmapBrush!!,rect.centerX().toFloat(),rect.centerY().toFloat(),null)
//        for (pos in mPositions) {
//            canvas.drawBitmap(mBitmapBrush!!, pos.x, pos.y, null)
//        }
//        if(background == drawable) {
//            return
//        }

//        background = drawable
//        throw IllegalArgumentException("Helloo")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        mBitmapBrush =
//            Bitmap.createScaledBitmap(mBitmapBrush!!, measuredWidth, measuredHeight, false)
//        drawable = BitmapDrawable(resources, mBitmapBrush)
//        background = drawable
        val typedValue = TypedValue()
        context.theme
            .resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
        val color = typedValue.data
        setBackgroundColor(getColorWithAlpha(color, 0.3f))
    }

    private fun getColorWithAlpha(color: Int, ratio: Float): Int {
        return Color.argb(
            Math.round(Color.alpha(color) * ratio),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        val action = event.action
//        when (action) {
//            MotionEvent.ACTION_MOVE -> {
//                val posX = event.x
//                val posY = event.y
//                mPositions.clear()
//                mPositions.add(
//                    Vector2(
//                        posX - mBitmapBrushDimensions!!.x / 2,
//                        posY - mBitmapBrushDimensions!!.y / 2
//                    )
//                )
//                invalidate()
//            }
//        }
//        return true
//    }
}