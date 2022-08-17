module dev.kkorolyov.ponk {
	requires kotlin.stdlib;
	requires kotlin.stdlib.jdk7;
	requires java.desktop;

	// logging
	requires org.slf4j;
	requires org.apache.logging.log4j;

	requires org.lwjgl;
	requires org.lwjgl.natives;
	requires org.lwjgl.glfw;
	requires org.lwjgl.glfw.natives;
	requires org.lwjgl.opengl.natives;
	requires org.lwjgl.openal.natives;
	requires org.lwjgl.stb.natives;

	requires dev.kkorolyov.pancake.platform;
	requires dev.kkorolyov.pancake.core;
	requires dev.kkorolyov.pancake.graphics.gl;
	requires dev.kkorolyov.pancake.audio.al;
	requires dev.kkorolyov.pancake.input.glfw;

	requires dev.kkorolyov.pancake.editor;
	requires dev.kkorolyov.pancake.editor.core;

	opens shaders;
}
