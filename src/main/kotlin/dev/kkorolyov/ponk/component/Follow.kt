package dev.kkorolyov.ponk.component

import dev.kkorolyov.pancake.platform.entity.Component
import dev.kkorolyov.pancake.platform.math.Vector2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Applies force to follow a given vector.
 */
class Follow(private val target: Vector2, maxForce: Double) : Component {
	private val maxForce: Double = abs(maxForce)

	fun apply(force: Vector2, position: Vector2) {
		if (position.y < target.y) {
			force.y = max(force.y, maxForce)
		} else if (position.y > target.y) {
			force.y = min(force.y, -maxForce)
		}
	}
}
