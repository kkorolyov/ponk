module dev.kkorolyov.ponk {
	requires kotlin.stdlib;

	// logging
	requires org.slf4j;
	requires org.apache.logging.log4j;

	requires javafx.media;
	requires javafx.graphics;

	requires dev.kkorolyov.pancake.platform;
	requires dev.kkorolyov.pancake.core;
	requires dev.kkorolyov.pancake.graphics.jfx;
	requires dev.kkorolyov.pancake.audio.jfx;
	requires dev.kkorolyov.pancake.input.jfx;
}
