/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.raid.miptandroid

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min


internal class ReticleOverlay(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context) : this(context, null, 0, 0)

    private val boxCornerRadius: Float =
        resources.getDimensionPixelOffset(R.dimen.barcode_reticle_corner_radius).toFloat()
    private val ripplePaint: Paint = Paint()
    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcodeReticleStroke)
        style = Style.STROKE
        strokeWidth = resources.getDimensionPixelOffset(R.dimen.barcode_reticle_stroke_width).toFloat()
    }
    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorOverlayBackground)
    }
    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val animator = ReticleAnimator()
    private val rippleSizeOffset: Int
    private val rippleStrokeWidth: Int
    private val rippleAlpha: Int
    private var boxRect: RectF

    init {
        ripplePaint.style = Style.STROKE
        ripplePaint.color = ContextCompat.getColor(context, R.color.reticleRipple)
        rippleSizeOffset = resources.getDimensionPixelOffset(R.dimen.barcode_reticle_ripple_size_offset)
        rippleStrokeWidth = resources.getDimensionPixelOffset(R.dimen.barcode_reticle_ripple_stroke_width)
        rippleAlpha = ripplePaint.alpha
        boxRect = getBarcodeReticleBox()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        boxRect = getBarcodeReticleBox()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) {
            animator.start()
        } else {
            animator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        eraserPaint.style = Style.FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = Style.STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint)
        ripplePaint.alpha = (rippleAlpha * animator.rippleAlphaScale).toInt()
        ripplePaint.strokeWidth = rippleStrokeWidth * animator.rippleStrokeWidthScale
        val offset = rippleSizeOffset * animator.rippleSizeScale
        val rippleRect = RectF(
            boxRect.left - offset,
            boxRect.top - offset,
            boxRect.right + offset,
            boxRect.bottom + offset
        )
        canvas.drawRoundRect(rippleRect, boxCornerRadius, boxCornerRadius, ripplePaint)
    }

    private fun getBarcodeReticleBox(): RectF {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()
        val size = min(overlayWidth * 0.8f, overlayHeight * 0.8f)
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        return RectF(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
    }
}
