package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.audio.jfx.system.AudioSystem
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.DampingSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.graphics.jfx.AddCamera
import dev.kkorolyov.pancake.graphics.jfx.Camera
import dev.kkorolyov.pancake.graphics.jfx.system.DrawSystem
import dev.kkorolyov.pancake.input.jfx.system.InputSystem
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameLoop
import dev.kkorolyov.pancake.platform.math.Vectors
import dev.kkorolyov.ponk.system.FollowSystem
import dev.kkorolyov.ponk.system.ScoreSystem
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.layout.FlowPane

val pane = FlowPane().apply {
	onMouseClicked = EventHandler { requestFocus() }
}

val gameLoop = GameLoop(
	GameEngine(
		events,
		entities,
		listOf(
			ActionSystem(),
			InputSystem(listOf(pane)),
			AccelerationSystem(),
			CappingSystem(),
			MovementSystem(),
			DampingSystem(),
			CollisionSystem(),
			AudioSystem(),
			DrawSystem(pane),
			FollowSystem(),
			ScoreSystem()
		)
	)
)

fun main() {
	events.enqueue(AddCamera(Camera(Vectors.create(0.0, 0.0), Vectors.create(64.0, 64.0))))

	Platform.startup {
		App(Scene(pane), gameLoop::stop)
		pane.requestFocus()
	}
	Thread(gameLoop::start).start()
}
