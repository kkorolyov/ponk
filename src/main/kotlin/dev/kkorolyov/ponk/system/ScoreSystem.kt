package dev.kkorolyov.ponk.system

import dev.kkorolyov.pancake.core.component.event.Intersected
import dev.kkorolyov.pancake.graphics.component.Model
import dev.kkorolyov.pancake.graphics.gl.Font
import dev.kkorolyov.pancake.graphics.resource.Mesh
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.ponk.component.Score

/**
 * Increments [Score]s of intersected entities.
 */
class ScoreSystem(private val displays: Map<Entity, Entity>, private val font: Font) : GameSystem(Score::class.java, Intersected::class.java) {
	override fun update(entity: Entity, dt: Long) {
		val score = entity[Score::class.java]

		score += 1

		displays[entity]?.let {
			it[Model::class.java].apply {
				meshes.forEach(Mesh::close)
				setMeshes(font(score.value.toString()))
			}
		}
	}
}
