package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.platform.math.Vector3
import java.awt.Color

/**
 * Returns the `RGB` components of this color as a vector.
 */
fun Color.toVector(): Vector3 = Vector3.of(red.toDouble(), green.toDouble(), blue.toDouble()).apply {
	scale(1 / 255.0)
}
