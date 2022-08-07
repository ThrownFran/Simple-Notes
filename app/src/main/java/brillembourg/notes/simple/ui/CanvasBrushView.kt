package brillembourg.notes.simple.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import brillembourg.notes.simple.R
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
        mBitmapBrush =
            ContextCompat.getDrawable(context, R.drawable.blue_creyon_2)?.toBitmap()
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
        mBitmapBrush =
            Bitmap.createScaledBitmap(mBitmapBrush!!, measuredWidth, measuredHeight, false)
        drawable = BitmapDrawable(resources, mBitmapBrush)
        background = drawable
//        val typedValue = TypedValue()
//        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true)
//        val color = typedValue.data
//        setBackgroundColor(color)
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