module dev.kkorolyov.ponk {
	requires kotlin.stdlib;

	// logging
	requires org.slf4j;
	requires org.apache.logging.log4j;

	requires javafx.base;
	requires tornadofx;

	requires dev.kkorolyov.pancake.platform;
	requires dev.kkorolyov.pancake.core;
	requires dev.kkorolyov.pancake.graphics.jfx;
	requires dev.kkorolyov.pancake.audio.jfx;
	requires dev.kkorolyov.pancake.input.jfx;

	requires dev.kkorolyov.pancake.editor;
	requires dev.kkorolyov.pancake.editor.core;

	exports dev.kkorolyov.ponk to tornadofx;
}
