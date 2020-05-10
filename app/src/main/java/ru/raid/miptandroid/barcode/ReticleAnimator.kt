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

package ru.raid.miptandroid.barcode

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class ReticleAnimator {
    var rippleAlphaScale = 0f
        private set
    var rippleSizeScale = 0f
        private set
    var rippleStrokeWidthScale = 1f
        private set

    private val animatorSet: AnimatorSet

    init {
        val rippleFadeInAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(DURATION_RIPPLE_FADE_IN_MS)
        rippleFadeInAnimator.addUpdateListener { animation ->
            rippleAlphaScale = animation.animatedValue as Float
        }

        val rippleFadeOutAnimator = ValueAnimator.ofFloat(1f, 0f).setDuration(DURATION_RIPPLE_FADE_OUT_MS)
        rippleFadeOutAnimator.startDelay =
            START_DELAY_RIPPLE_FADE_OUT_MS
        rippleFadeOutAnimator.addUpdateListener { animation ->
            rippleAlphaScale = animation.animatedValue as Float
        }

        val rippleExpandAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(DURATION_RIPPLE_EXPAND_MS)
        rippleExpandAnimator.startDelay =
            START_DELAY_RIPPLE_EXPAND_MS
        rippleExpandAnimator.interpolator = FastOutSlowInInterpolator()
        rippleExpandAnimator.addUpdateListener { animation ->
            rippleSizeScale = animation.animatedValue as Float
        }

        val rippleStrokeWidthShrinkAnimator =
            ValueAnimator.ofFloat(1f, 0.5f).setDuration(DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS)
        rippleStrokeWidthShrinkAnimator.startDelay =
            START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS
        rippleStrokeWidthShrinkAnimator.interpolator = FastOutSlowInInterpolator()
        rippleStrokeWidthShrinkAnimator.addUpdateListener { animation ->
            rippleStrokeWidthScale = animation.animatedValue as Float
        }

        val fakeAnimatorForRestartDelay = ValueAnimator.ofInt(0, 0).setDuration(DURATION_RESTART_DORMANCY_MS)
        fakeAnimatorForRestartDelay.startDelay =
            START_DELAY_RESTART_DORMANCY_MS
        animatorSet = AnimatorSet()
        animatorSet.playTogether(
                rippleFadeInAnimator,
                rippleFadeOutAnimator,
                rippleExpandAnimator,
                rippleStrokeWidthShrinkAnimator,
                fakeAnimatorForRestartDelay
        )
    }

    fun start() {
        if (!animatorSet.isRunning) animatorSet.start()
    }

    fun cancel() {
        animatorSet.cancel()
        rippleAlphaScale = 0f
        rippleSizeScale = 0f
        rippleStrokeWidthScale = 1f
    }

    private companion object {
        const val DURATION_RIPPLE_FADE_IN_MS: Long = 333
        const val DURATION_RIPPLE_FADE_OUT_MS: Long = 500
        const val DURATION_RIPPLE_EXPAND_MS: Long = 833
        const val DURATION_RIPPLE_STROKE_WIDTH_SHRINK_MS: Long = 833
        const val DURATION_RESTART_DORMANCY_MS: Long = 1333
        const val START_DELAY_RIPPLE_FADE_OUT_MS: Long = 667
        const val START_DELAY_RIPPLE_EXPAND_MS: Long = 333
        const val START_DELAY_RIPPLE_STROKE_WIDTH_SHRINK_MS: Long = 333
        const val START_DELAY_RESTART_DORMANCY_MS: Long = 1167
    }
}
