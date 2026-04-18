package com.temm.core.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import com.temm.R
import com.temm.core.extensions.gone
import com.temm.core.extensions.visible
import kotlin.random.Random

/**
 * Manages musical note icon overlays for touch feedback
 * Generates 2-5 random icons that fly slowly in all directions
 * Child-friendly animation: slow (3-4 seconds), gentle, easy to see and enjoy
 */
class NoteIconManager(
    private val context: Context,
    private val overlayContainer: FrameLayout
) {
    // Multiple spawn points — each is (offsetX, offsetY) from button center in dp
    private val _spawnPoints = mutableListOf<Pair<Float, Float>>()
    val spawnPoints: List<Pair<Float, Float>> get() = _spawnPoints

    fun addSpawnPoint(offsetX: Float, offsetY: Float) {
        _spawnPoints.add(Pair(offsetX, offsetY))
    }

    fun clearSpawnPoints() {
        _spawnPoints.clear()
    }

    // When true, spawn points are re-randomized on every tap
    var randomizeOnTap: Boolean = false
    private var randomCount: Int = 5
    private var randomXRange: ClosedFloatingPointRange<Float> = -100f..100f
    private var randomYRange: ClosedFloatingPointRange<Float> = -100f..100f

    // Generate `count` random spawn points within the given x/y dp range
    fun setRandomSpawnPoints(count: Int, xRange: ClosedFloatingPointRange<Float> = -100f..100f, yRange: ClosedFloatingPointRange<Float> = -100f..100f) {
        randomCount = count
        randomXRange = xRange
        randomYRange = yRange
        randomizeOnTap = true
        applyRandomSpawnPoints()
    }

    private fun applyRandomSpawnPoints() {
        _spawnPoints.clear()
        repeat(randomCount) {
            val x = randomXRange.start + Random.nextFloat() * (randomXRange.endInclusive - randomXRange.start)
            val y = randomYRange.start + Random.nextFloat() * (randomYRange.endInclusive - randomYRange.start)
            _spawnPoints.add(Pair(x, y))
        }
    }

    // Number of icons spawned per point (min, max) — e.g. Pair(2, 5)
    var iconCountRange: Pair<Int, Int> = Pair(1, 1)

    // Direction range in degrees — icons fly within [angleFrom, angleTo]
    // 0° = right, 90° = down, 180° = left, 270° = up
    // Default: full 360° spread
    var angleFrom: Float = 180f
    var angleTo: Float = 360f

    // Track all active icons currently animating
    private val activeIcons = mutableListOf<ImageView>()

    // Recycled icons ready for reuse
    private val iconPool = mutableListOf<ImageView>()

    // Icon size (responsive to screen density)
    private val iconSizePx = (48 * context.resources.displayMetrics.density).toInt()

    // Handler for auto-removal
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Show 2-5 random icons that burst from button center in all directions (360°)
     * Like fireworks spreading outward evenly
     */
    fun showIcon(pointerId: Int, button: ImageView) {
        // Check if we have too many active icons
        if (activeIcons.size >= MAX_ACTIVE_ICONS) return
        if (randomizeOnTap) applyRandomSpawnPoints()
        if (_spawnPoints.isEmpty()) return

        // Fire all spawn points at the same time
        spawnPoints.forEach { spawn ->
            val iconCount = Random.nextInt(iconCountRange.first, iconCountRange.second + 1)
            val range = if (angleTo > angleFrom) angleTo - angleFrom else 360f
            val angleSpread = range / iconCount
            val baseAngle = angleFrom + (Random.nextFloat() * angleSpread)
            repeat(iconCount) { index ->
                val randomOffset = (Random.nextFloat() * 10f - 5f)
                val angle = (baseAngle + (index * angleSpread) + randomOffset) % 360f
                createAndAnimateIcon(button, spawn, angle)
            }
        }
    }

    /**
     * Hide icon for a specific pointer (legacy method - now does nothing since icons auto-remove)
     */
    fun hideIcon(pointerId: Int) {
        // Icons now auto-remove after 2 seconds, so this method does nothing
    }

    /**
     * Hide all icons (e.g., when touch is cancelled)
     */
    fun hideAllIcons() {
        // Copy list to avoid concurrent modification
        val iconsToRemove = activeIcons.toList()
        iconsToRemove.forEach { icon ->
            icon.animate().cancel()
            removeIcon(icon)
        }
    }

    /**
     * Create and animate a single flying icon at specified angle
     */
    private fun createAndAnimateIcon(button: ImageView, spawn: Pair<Float, Float>, angle: Float) {
        // Get or create icon from pool
        val icon = if (iconPool.isNotEmpty()) {
            iconPool.removeAt(iconPool.size - 1)
        } else {
            createIcon()
        }

        // Add to active list
        activeIcons.add(icon)

        // Get button position relative to overlay container
        val buttonLocation = IntArray(2)
        val containerLocation = IntArray(2)
        button.getLocationOnScreen(buttonLocation)
        overlayContainer.getLocationOnScreen(containerLocation)

        val relativeX = buttonLocation[0] - containerLocation[0]
        val relativeY = buttonLocation[1] - containerLocation[1]

        val density = context.resources.displayMetrics.density
        val startX = relativeX + (button.width / 2f) - (iconSizePx / 2f) + (spawn.first * density)
        val startY = relativeY + (button.height / 2f) - (iconSizePx / 2f) + (spawn.second * density)

        // Use the provided angle
        val angleRadians = Math.toRadians(angle.toDouble())

        // Distance (dp -> px) so icons stay on screen
        val flyDistance = Random.nextInt(150, 251) * context.resources.displayMetrics.density

        // Calculate movement based on angle
        val deltaX = (flyDistance * Math.cos(angleRadians)).toFloat()
        val deltaY = (flyDistance * Math.sin(angleRadians)).toFloat()

        // IMPORTANT: cancel any previous animation & reset transforms (because we reuse from pool)
        icon.animate().cancel()
        icon.translationX = 0f
        icon.translationY = 0f
        icon.rotation = 0f
        icon.alpha = 1f
        icon.scaleX = 1f
        icon.scaleY = 1f

        // Set initial position
        icon.x = startX
        icon.y = startY
        icon.visible()
        icon.scaleX = 0.5f
        icon.scaleY = 0.5f
        icon.rotation = Random.nextFloat() * 360f

        Log.d(
            TAG,
            "Icon: angle=$angle°, distance=$flyDistance, deltaX=$deltaX, deltaY=$deltaY, start=($startX,$startY)"
        )

        // Slow flight for children (4-6 seconds)
        val flightDuration = Random.nextLong(4000, 6001)

        // KEY FIX: use translationXBy / translationYBy so it moves relative from start position
        icon.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .translationXBy(deltaX)
            .translationYBy(deltaY)
            .rotation(icon.rotation + Random.nextInt(-180, 181))
            .setDuration(flightDuration)
            .withEndAction { removeIcon(icon) }
            .start()

        // Fade out near the end
        val fadeStartTime = flightDuration - 1500
        handler.postDelayed({
            if (activeIcons.contains(icon)) {
                icon.animate()
                    .alpha(0f)
                    .setDuration(1500)
                    .start()
            }
        }, fadeStartTime)
    }

    /**
     * Remove icon and return to pool
     */
    private fun removeIcon(icon: ImageView) {
        activeIcons.remove(icon)
        icon.gone()

        // Reset all transformations for reuse
        icon.translationX = 0f
        icon.translationY = 0f
        icon.rotation = 0f
        icon.scaleX = 1f
        icon.scaleY = 1f

        if (iconPool.size < MAX_POOL_SIZE) {
            iconPool.add(icon)
        } else {
            overlayContainer.removeView(icon)
        }

        Log.d(TAG, "Icon removed, active count: ${activeIcons.size}")
    }

    /**
     * Create a new icon ImageView
     */
    private fun createIcon(): ImageView {
        return ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(iconSizePx, iconSizePx)
            setImageResource(R.drawable.ic_note)
            scaleType = ImageView.ScaleType.FIT_CENTER
            elevation = 10f // Ensure it renders above buttons
            gone()
            overlayContainer.addView(this)
        }
    }

    companion object {
        private const val TAG = "NoteIconManager"
        private const val MAX_POOL_SIZE = 50 // Pool up to 50 icons for reuse
        private const val MAX_ACTIVE_ICONS = 30 // Limit total active icons to prevent performance issues
    }
}
