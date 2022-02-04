package dev.kkorolyov.ponk.system

import dev.kkorolyov.pancake.core.event.EntitiesIntersected
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.utility.Limiter
import dev.kkorolyov.ponk.component.Score

/**
 * Increments [Score]s of intersected entities.
 */
class ScoreSystem : GameSystem(listOf(Score::class.java), Limiter.fromConfig(ScoreSystem::class.java)) {
	private val intersected: MutableSet<Int> = mutableSetOf()

	override fun attach() {
		register(EntitiesIntersected::class.java) {
			intersected.add(it.a.id)
			intersected.add(it.b.id)
		}
	}

	override fun after(dt: Long) {
		intersected.clear()
	}

	override fun update(entity: Entity, dt: Long) {
		if (entity.id in intersected) {
			entity.get(Score::class.java) += 1
		}
	}
}
