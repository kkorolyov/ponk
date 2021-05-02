package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Input
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.media.Graphic
import dev.kkorolyov.pancake.core.component.movement.Damping
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.core.component.movement.Mass
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.movement.VelocityCap
import dev.kkorolyov.pancake.core.event.EntitiesCollided
import dev.kkorolyov.pancake.core.input.HandlerReader
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.EntityPool
import dev.kkorolyov.pancake.platform.event.EventLoop
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.math.Vectors
import dev.kkorolyov.pancake.platform.media.graphic.shape.Shape.Color
import dev.kkorolyov.pancake.platform.media.graphic.shape.Text
import dev.kkorolyov.pancake.platform.registry.DeferredConverterFactory
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceReader
import dev.kkorolyov.ponk.component.Follow
import dev.kkorolyov.ponk.component.Score

val actions = Resources.inStream("actions.yaml").use {
	Registry<String, Action>().apply {
		load(
			ResourceReader(DeferredConverterFactory.get(DeferredConverterFactory.ActionStrat::class.java)).fromYaml(
				it
			)
		)
	}
}

val paddleVelocityCap = VelocityCap(Vectors.create(20.0, 20.0, 20.0))
val paddleDamping = Damping(Vectors.create(0.0, 0.0, 0.0))
val paddleMass = Mass(1e-2)
val ballMass = Mass(1e-9)

val paddleSize: Vector3 = Vectors.create(1.0, 4.0, 0.0)
val ballSize: Vector3 = Vectors.create(1.0, 1.0, 0.0)
val goalSize: Vector3 = Vectors.create(2.0, 10.0, 0.0)
val netSize: Vector3 = Vectors.create(10.0, 2.0, 0.0)

val ballTransform = Transform(Vectors.create(0.0, 0.0, 0.0))

val paddleBounds = Bounds(paddleSize)
val ballBounds = Bounds(ballSize)
val goalBounds = Bounds(goalSize)
val netBounds = Bounds(netSize)

val paddleGraphic: Graphic = Graphic(Resources.RENDER_MEDIUM.box.apply {
	fill = Color.BLACK
	size.set(paddleSize)
})
val ballGraphic: Graphic = Graphic(Resources.RENDER_MEDIUM.box.apply {
	fill = Color.GRAY
	size.set(ballSize)
})
val goalGraphic: Graphic = Graphic(Resources.RENDER_MEDIUM.box.apply {
	fill = Color.BLUE
	size.set(goalSize)
})
val netGraphic: Graphic = Graphic(Resources.RENDER_MEDIUM.box.apply {
	fill = Color.RED
	size.set(netSize)
})

val playerScore: Text = Resources.RENDER_MEDIUM.text.apply {
	value = 0.toString()
	fill = Color.GREEN
}
val opponentScore: Text = Resources.RENDER_MEDIUM.text.apply {
	value = 0.toString()
	fill = Color.GREEN
}

val events: EventLoop.Broadcasting = EventLoop.Broadcasting()
val entities: EntityPool = EntityPool(events).apply {
	val player = create().apply {
		add(
			paddleGraphic,
			paddleBounds,
			Transform(Vectors.create(-4.0, 0.0, 0.0)),
			Velocity(Vectors.create(0.0, 0.0, 0.0)),
			paddleVelocityCap,
			paddleDamping,
			Force(Vectors.create(0.0, 0.0, 0.0)),
			paddleMass,
			ActionQueue()
		)
		Resources.inStream("input.yaml").use {
			add(Input(false, HandlerReader(actions).fromYaml(it)))
		}
	}

	val opponent = create().apply {
		add(
			paddleGraphic,
			paddleBounds,
			Transform(Vectors.create(4.0, 0.0, 0.0)),
			Velocity(Vectors.create(0.0, 0.0, 0.0)),
			paddleVelocityCap,
			paddleDamping,
			Force(Vectors.create(0.0, 0.0, 0.0)),
			paddleMass,
			Follow(ballTransform.position, 0.2)
		)
	}

	val ball = create().apply {
		add(
			ballGraphic,
			ballBounds,
			ballTransform,
			Velocity(Vectors.create(0.0, 0.0, 0.0)),
			VelocityCap(Vectors.create(20.0, 20.0, 0.0)),
			Force(Vectors.create(0.0, 0.0, 0.0)),
			ballMass,
			ActionQueue()
		)
		Resources.inStream("ballInput.yaml").use {
			add(Input(false, HandlerReader(actions).fromYaml(it)))
		}
	}

	val goalPlayer = create().apply {
		add(
			goalBounds,
			goalGraphic,
			Transform(Vectors.create(-6.0, 0.0, 0.0)),
			Score()
		)
	}
	val goalOpponent = create().apply {
		add(
			goalBounds,
			goalGraphic,
			Transform(Vectors.create(6.0, 0.0, 0.0)),
			Score()
		)
	}

	val top = create().apply {
		add(
			netBounds,
			netGraphic,
			Transform(Vectors.create(0.0, 6.0, 0.0))
		)
	}
	val bottom = create().apply {
		add(
			netBounds,
			netGraphic,
			Transform(Vectors.create(0.0, -6.0, 0.0))
		)
	}

	val playerScoreText = create().apply {
		add(
			Transform(Vectors.create(-4.0, -4.0, 1.0)),
			Graphic(playerScore)
		)
	}
	val opponentScoreText = create().apply {
		add(
			Transform(Vectors.create(4.0, -4.0, 1.0)),
			Graphic(opponentScore)
		)
	}
	val helpText = create().apply {
		add(
			Transform(Vectors.create(-0.5, -4.0, 1.0)),
			Graphic(Resources.RENDER_MEDIUM.text.apply {
				value = "Press SPACE to reset"
			})
		)
	}

	events.register(EntitiesCollided::class.java) {
		for (id in it.collided) {
			if (goalPlayer.id == id) {
				opponentScore.value = goalPlayer.get(Score::class.java).value.toString()
			} else if (goalOpponent.id == id) {
				playerScore.value = goalOpponent.get(Score::class.java).value.toString()
			}
		}
	}
}
