package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.GameLoop
import dev.kkorolyov.pancake.platform.plugin.Application
import dev.kkorolyov.pancake.platform.plugin.Plugins

fun main() {
	Plugins.application().execute(
		Application.Config(
			"Ponk",
			"pancake-icon.png",
			640.0,
			640.0
		),
		GameLoop(GameEngine(events, entities))
	)
}
