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
    private var mBitmapBrush: Bitmap? = null
    private var mBitmapBrushDimensions: Vector2? = null

    private val mPositions: MutableList<Vector2> = ArrayList(100)

    private class Vector2(val x: Float, val y: Float)

    init {
        // load your brush here
//        mBitmapBrush = BitmapFactory.decodeResource(context.getResources(), R.drawable.background)
        mBitmapBrush = ContextCompat.getDrawable(context, R.drawable.background)?.toBitmap()
        mBitmapBrushDimensions = Vector2(
            mBitmapBrush?.width!!.toFloat(),
            mBitmapBrush?.height!!.toFloat()
        )

//        val drawable: Drawable = BitmapDrawable(resources, mBitmapBrush)
//        background = drawable
    }


    //
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

//        canvas.drawBitmap(mBitmapBrush!!, 0f, 0f, null)
//        canvas.drawBitmap(mBitmapBrush!!, 1000f, 1000f, null)
//        for (pos in mPositions) {
//            canvas.drawBitmap(mBitmapBrush!!, pos.x, pos.y, null)
//        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        setBackgroundColor(R.color.teal_200)
        val drawable: Drawable = BitmapDrawable(resources, mBitmapBrush)
        background = drawable
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