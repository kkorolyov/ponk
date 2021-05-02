package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameLoop
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.application.Application.Config

fun main() {
	Resources.APPLICATION.execute(
		Config(
			"Ponk",
			"pancake-icon.png",
			640.0,
			640.0
		),
		GameLoop(GameEngine(events, entities))
	)
}
