/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.ui.databinding

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import app.tivi.R
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.extensions.doOnApplyWindowInsets
import app.tivi.extensions.optSaturateOnLoad
import app.tivi.extensions.resolveThemeReferenceResId
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.ui.MaxLinesToggleClickListener
import app.tivi.ui.glide.GlideApp
import app.tivi.util.ScrimUtil
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import java.util.Objects
import kotlin.math.roundToInt

@BindingAdapter(
        "tmdbPosterPath",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadPoster(
    view: ImageView,
    path: String,
    urlProvider: TmdbImageUrlProvider,
    saturateOnLoad: Boolean?
) = loadImage(view, ShowTmdbImage(path = path, type = ImageType.POSTER, showId = 0), urlProvider, saturateOnLoad)

@BindingAdapter(
        "tmdbBackdropPath",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadBackdrop(
    view: ImageView,
    path: String,
    urlProvider: TmdbImageUrlProvider,
    saturateOnLoad: Boolean?
) = loadImage(view, ShowTmdbImage(path = path, type = ImageType.BACKDROP, showId = 0), urlProvider, saturateOnLoad)

@BindingAdapter(
        "image",
        "tmdbImageUrlProvider",
        "imageSaturateOnLoad",
        requireAll = false
)
fun loadImage(
    view: ImageView,
    image: TmdbImageEntity?,
    urlProvider: TmdbImageUrlProvider?,
    saturateOnLoad: Boolean?
) {
    val requestKey = Objects.hash(image, urlProvider)
    if (view.getTag(R.id.loading) == requestKey) {
        // We're already loading this image, ignore the call
        return
    }
    view.setTag(R.id.loading, requestKey)

    if (urlProvider != null && image != null) {
        view.setImageDrawable(null)

        view.doOnLayout {
            if (it.getTag(R.id.loading) != requestKey) {
                // The request key is different, exit now since there's we've probably be rebound to a different
                // item
                return@doOnLayout
            }

            fun toUrl(image: TmdbImageEntity, width: Int) = when (image.type) {
                ImageType.BACKDROP -> urlProvider.getBackdropUrl(image.path, width)
                ImageType.POSTER -> urlProvider.getPosterUrl(image.path, width)
            }

            GlideApp.with(it)
                    .optSaturateOnLoad(saturateOnLoad == null || saturateOnLoad)
                    .load(toUrl(image, view.width))
                    .thumbnail(GlideApp.with(view)
                            .optSaturateOnLoad(saturateOnLoad == null || saturateOnLoad)
                            .load(toUrl(image, 0)))
                    .into(view)
        }
    } else {
        GlideApp.with(view).clear(view)
        view.setImageDrawable(null)
    }
}

@BindingAdapter("visibleIfNotNull")
fun visibleIfNotNull(view: View, target: Any?) {
    view.isVisible = target != null
}

@BindingAdapter("visible")
fun visible(view: View, value: Boolean) {
    view.isVisible = value
}

@BindingAdapter("srcRes")
fun imageViewSrcRes(view: ImageView, drawableRes: Int) {
    if (drawableRes != 0) {
        view.setImageResource(drawableRes)
    } else {
        view.setImageDrawable(null)
    }
}

@BindingAdapter("maxLinesToggle")
fun maxLinesClickListener(view: TextView, oldCollapsedMaxLines: Int, newCollapsedMaxLines: Int) {
    if (oldCollapsedMaxLines != newCollapsedMaxLines) {
        // Default to collapsed
        view.maxLines = newCollapsedMaxLines
        // Now set click listener
        view.setOnClickListener(MaxLinesToggleClickListener(newCollapsedMaxLines))
    }
}

@BindingAdapter("backgroundScrim")
fun backgroundScrim(view: View, oldColor: Int, color: Int) {
    if (oldColor != color) {
        view.background = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
    }
}

@BindingAdapter("foregroundScrim")
fun foregroundScrim(view: View, oldColor: Int, color: Int) {
    if (oldColor != color) {
        view.foreground = ScrimUtil.makeCubicGradientScrimDrawable(color, 16, Gravity.BOTTOM)
    }
}

@BindingAdapter("materialBackdropBackgroundRadius")
fun materialBackdropBackground(view: View, oldRadius: Float, radius: Float) {
    if (oldRadius != radius) {
        view.background = MaterialShapeDrawable().apply {
            fillColor = ColorStateList.valueOf(Color.WHITE)
            shapeAppearanceModel.setTopLeftCorner(CornerFamily.ROUNDED, radius.toInt())
            shapeAppearanceModel.setTopRightCorner(CornerFamily.ROUNDED, radius.toInt())
        }
    }
}

@BindingAdapter("topCornerOutlineProvider")
fun topCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + radius.roundToInt(), radius)
            }
        }
    }
}

@BindingAdapter("roundedCornerOutlineProvider")
fun roundedCornerOutlineProvider(view: View, oldRadius: Float, radius: Float) {
    view.clipToOutline = true
    if (oldRadius != radius) {
        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
    }
}

@BindingAdapter("textAppearanceAttr")
fun textAppearanceAttr(view: TextView, oldTextAppearanceStyleAttr: Int, textAppearanceStyleAttr: Int) {
    if (oldTextAppearanceStyleAttr != textAppearanceStyleAttr) {
        view.setTextAppearance(view.context.resolveThemeReferenceResId(textAppearanceStyleAttr))
    }
}

@BindingAdapter(
        "paddingLeftSystemWindowInsets",
        "paddingTopSystemWindowInsets",
        "paddingRightSystemWindowInsets",
        "paddingBottomSystemWindowInsets",
        "paddingLeftGestureInsets",
        "paddingTopGestureInsets",
        "paddingRightGestureInsets",
        "paddingBottomGestureInsets",
        requireAll = false
)
fun applySystemWindows(
    view: View,
    systemWindowLeft: Boolean,
    systemWindowTop: Boolean,
    systemWindowRight: Boolean,
    systemWindowBottom: Boolean,
    gestureInsetsLeft: Boolean,
    gestureInsetsTop: Boolean,
    gestureInsetsRight: Boolean,
    gestureInsetsBottom: Boolean
) {
    require(!((systemWindowLeft && gestureInsetsLeft) ||
            (systemWindowTop && gestureInsetsTop) ||
            (systemWindowRight && gestureInsetsRight) ||
            (systemWindowBottom && gestureInsetsBottom))) {
        "Invalid parameters. Can not request system window and gesture inset handling for the same dimension"
    }

    view.doOnApplyWindowInsets { v, insets, paddingState ->
        val left = when {
            gestureInsetsLeft -> insets.systemGestureInsets.left
            systemWindowLeft -> insets.systemWindowInsetLeft
            else -> 0
        }
        val top = when {
            gestureInsetsTop -> insets.systemGestureInsets.top
            systemWindowTop -> insets.systemWindowInsetTop
            else -> 0
        }
        val right = when {
            gestureInsetsRight -> insets.systemGestureInsets.right
            systemWindowRight -> insets.systemWindowInsetRight
            else -> 0
        }
        val bottom = when {
            gestureInsetsBottom -> insets.systemGestureInsets.bottom
            systemWindowBottom -> insets.systemWindowInsetBottom
            else -> 0
        }
        v.setPadding(
                paddingState.left + left,
                paddingState.top + top,
                paddingState.right + right,
                paddingState.bottom + bottom
        )
    }
}

@BindingAdapter("materialShapeElevationBackground")
fun materialShapeElevationBackground(view: View, oldValue: Boolean, value: Boolean) {
    if (oldValue != value && value) {
        val shapeDrawable = MaterialShapeDrawable.createWithElevationOverlay(view.context, view.elevation)
        view.background = shapeDrawable

        val vto = view.viewTreeObserver
        vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                shapeDrawable.z = view.z
                if (!view.isAttachedToWindow) {
                    if (vto.isAlive) {
                        vto.removeOnPreDrawListener(this)
                    } else {
                        view.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                }
                return true
            }
        })
    }
}
