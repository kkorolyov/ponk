package dev.kkorolyov.ponk.system

import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.platform.GameSystem
import dev.kkorolyov.pancake.platform.entity.Entity
import dev.kkorolyov.pancake.platform.entity.Signature
import dev.kkorolyov.pancake.platform.utility.Limiter
import dev.kkorolyov.ponk.component.Follow

/**
 * Applies following behavior to entities' [Force].
 */
class FollowSystem : GameSystem(
	Signature(Follow::class.java, Transform::class.java, Force::class.java),
	Limiter.fromConfig(FollowSystem::class.java)
) {
	override fun update(entity: Entity, dt: Long) {
		entity.get(Follow::class.java).apply(
			entity.get(Force::class.java).value,
			entity.get(Transform::class.java).position
		)
	}
}
