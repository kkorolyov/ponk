package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.platform.Config
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class App(private val scene: Scene, private val onClose: () -> Unit) : Application() {
	init {
		start(Stage())
	}

	override fun start(primaryStage: Stage) {
		primaryStage.title = "Ponk"
		primaryStage.icons += Image(Config.get().getProperty("icon"))
		primaryStage.scene = scene

		primaryStage.width = Config.get().getProperty("width").toDouble()
		primaryStage.height = Config.get().getProperty("height").toDouble()

		primaryStage.onCloseRequest = EventHandler { onClose() }

		primaryStage.show()
	}
}
