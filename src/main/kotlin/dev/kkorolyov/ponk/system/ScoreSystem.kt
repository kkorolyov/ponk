package dev.kkorolyov.ponk.system

import dev.kkorolyov.pancake.core.event.EntitiesCollided
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.entity.Signature
import dev.kkorolyov.pancake.platform.utility.Limiter
import dev.kkorolyov.ponk.component.Score

/**
 * Increments [Score]s of collided entities.
 */
class ScoreSystem : GameSystem(Signature(Score::class.java), Limiter.fromConfig(ScoreSystem::class.java)) {
	private val collided: MutableSet<Int> = mutableSetOf()

	override fun attach() {
		register(EntitiesCollided::class.java) {
			it.collided.forEach(collided::add)
		}
	}

	override fun after(dt: Long) {
		collided.clear()
	}

	override fun update(entity: Entity, dt: Long) {
		if (entity.id in collided) {
			entity.get(Score::class.java) += 1
		}
	}
}
