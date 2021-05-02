package dev.kkorolyov.ponk.component

import dev.kkorolyov.pancake.platform.entity.Component

/**
 * Maintains a score value.
 */
class Score : Component {
	var value: Int = 0

	operator fun plusAssign(other: Int) {
		value += other
	}
}
