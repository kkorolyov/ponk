package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Damping
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.core.component.movement.Mass
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.movement.VelocityCap
import dev.kkorolyov.pancake.core.event.EntitiesCollided
import dev.kkorolyov.pancake.graphics.jfx.component.Graphic
import dev.kkorolyov.pancake.graphics.jfx.drawable.Rectangle
import dev.kkorolyov.pancake.graphics.jfx.drawable.Text
import dev.kkorolyov.pancake.input.jfx.Compensated
import dev.kkorolyov.pancake.input.jfx.Reaction
import dev.kkorolyov.pancake.input.jfx.component.Input
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.entity.EntityPool
import dev.kkorolyov.pancake.platform.event.EventLoop
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.math.Vectors
import dev.kkorolyov.pancake.platform.plugin.DeferredConverterFactory
import dev.kkorolyov.pancake.platform.plugin.Plugins
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceReader
import dev.kkorolyov.ponk.component.Follow
import dev.kkorolyov.ponk.component.Score
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color

val actions = Resources.inStream("actions.yaml").use {
	Registry<String, Action>().apply {
		load(
			ResourceReader(Plugins.deferredConverter(DeferredConverterFactory.ActionStrat::class.java)).fromYaml(
				it
			)
		)
	}
}

val paddleVelocityCap = VelocityCap(Vectors.create(0.0, 20.0, 20.0))
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

val paddleGraphic: Graphic = Graphic(Rectangle(paddleSize, Color.BLACK))
val ballGraphic: Graphic = Graphic(Rectangle(ballSize, Color.GRAY))
val goalGraphic: Graphic = Graphic(Rectangle(goalSize, Color.BLUE))
val netGraphic: Graphic = Graphic(Rectangle(netSize, Color.RED))

val events: EventLoop.Broadcasting = EventLoop.Broadcasting()

private fun makeScoreText(score: Int): Text = Text(score.toString(), 1.0, Color.GREEN)

val entities: EntityPool = EntityPool(events).apply {
	val player = create().apply {
		put(
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
			put(
				Input(
					Reaction.matchType(
						Reaction.whenCode(
							KeyCode.W to Reaction.keyToggle(Compensated(actions["forceUp"], actions["forceDown"])),
							KeyCode.S to Reaction.keyToggle(Compensated(actions["forceDown"], actions["forceUp"])),
						)
					)
				)
			)
		}
	}

	val opponent = create().apply {
		put(
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
		put(
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
			put(
				Input(
					Reaction.matchType(
						Reaction.whenCode(
							KeyCode.SPACE to Reaction.keyToggle(Compensated(actions["reset"], Action { }))
						)
					)
				)
			)
		}
	}

	val goalPlayer = create().apply {
		put(
			goalBounds,
			goalGraphic,
			Transform(Vectors.create(-6.0, 0.0, 0.0)),
			Score()
		)
	}
	val goalOpponent = create().apply {
		put(
			goalBounds,
			goalGraphic,
			Transform(Vectors.create(6.0, 0.0, 0.0)),
			Score()
		)
	}

	val top = create().apply {
		put(
			netBounds,
			netGraphic,
			Transform(Vectors.create(0.0, 6.0, 0.0))
		)
	}
	val bottom = create().apply {
		put(
			netBounds,
			netGraphic,
			Transform(Vectors.create(0.0, -6.0, 0.0))
		)
	}

	val playerScoreText = create().apply {
		put(
			Transform(Vectors.create(-4.0, -4.0, 1.0)),
			Graphic(makeScoreText(0))
		)
	}
	val opponentScoreText = create().apply {
		put(
			Transform(Vectors.create(4.0, -4.0, 1.0)),
			Graphic(makeScoreText(0))
		)
	}
	val helpText = create().apply {
		put(
			Transform(Vectors.create(-0.5, -4.0, 1.0)),
			Graphic(Text("Press SPACE to reset", 2.0, Color.BLACK))
		)
	}

	events.register(EntitiesCollided::class.java) {
		for (id in it.collided) {
			if (goalPlayer.id == id) {
				opponentScoreText.put(Graphic(makeScoreText(goalPlayer.get(Score::class.java).value)))
			} else if (goalOpponent.id == id) {
				playerScoreText.put(Graphic(makeScoreText(goalOpponent.get(Score::class.java).value)))
			}
		}
	}
}
