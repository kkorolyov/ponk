package dev.kkorolyov.ponk

import dev.kkorolyov.pancake.core.component.ActionQueue
import dev.kkorolyov.pancake.core.component.Bounds
import dev.kkorolyov.pancake.core.component.Transform
import dev.kkorolyov.pancake.core.component.movement.Damping
import dev.kkorolyov.pancake.core.component.movement.Force
import dev.kkorolyov.pancake.core.component.movement.Mass
import dev.kkorolyov.pancake.core.component.movement.Velocity
import dev.kkorolyov.pancake.core.component.movement.VelocityCap
import dev.kkorolyov.pancake.core.component.tag.Collidable
import dev.kkorolyov.pancake.core.component.tag.Correctable
import dev.kkorolyov.pancake.core.system.AccelerationSystem
import dev.kkorolyov.pancake.core.system.ActionSystem
import dev.kkorolyov.pancake.core.system.CappingSystem
import dev.kkorolyov.pancake.core.system.CollisionSystem
import dev.kkorolyov.pancake.core.system.CorrectionSystem
import dev.kkorolyov.pancake.core.system.DampingSystem
import dev.kkorolyov.pancake.core.system.IntersectionSystem
import dev.kkorolyov.pancake.core.system.MovementSystem
import dev.kkorolyov.pancake.core.system.cleanup.PhysicsCleanupSystem
import dev.kkorolyov.pancake.editor.Container
import dev.kkorolyov.pancake.editor.widget.Editor
import dev.kkorolyov.pancake.editor.widget.Window
import dev.kkorolyov.pancake.graphics.CameraQueue
import dev.kkorolyov.pancake.graphics.component.Lens
import dev.kkorolyov.pancake.graphics.component.Model
import dev.kkorolyov.pancake.graphics.ellipse
import dev.kkorolyov.pancake.graphics.gl.Font
import dev.kkorolyov.pancake.graphics.gl.resource.GLMesh
import dev.kkorolyov.pancake.graphics.gl.resource.GLProgram
import dev.kkorolyov.pancake.graphics.gl.resource.GLShader
import dev.kkorolyov.pancake.graphics.gl.resource.GLVertexBuffer
import dev.kkorolyov.pancake.graphics.gl.system.DrawSystem
import dev.kkorolyov.pancake.graphics.rectangle
import dev.kkorolyov.pancake.graphics.system.CameraSystem
import dev.kkorolyov.pancake.input.Compensated
import dev.kkorolyov.pancake.input.Reaction
import dev.kkorolyov.pancake.input.component.Input
import dev.kkorolyov.pancake.input.glfw.system.InputSystem
import dev.kkorolyov.pancake.input.glfw.toggle
import dev.kkorolyov.pancake.input.glfw.whenKey
import dev.kkorolyov.pancake.platform.Config
import dev.kkorolyov.pancake.platform.GameEngine
import dev.kkorolyov.pancake.platform.Pipeline
import dev.kkorolyov.pancake.platform.Resources
import dev.kkorolyov.pancake.platform.action.Action
import dev.kkorolyov.pancake.platform.math.Vector2
import dev.kkorolyov.pancake.platform.math.Vector3
import dev.kkorolyov.pancake.platform.registry.BasicParsers
import dev.kkorolyov.pancake.platform.registry.Registry
import dev.kkorolyov.pancake.platform.registry.ResourceConverters
import dev.kkorolyov.ponk.component.Follow
import dev.kkorolyov.ponk.component.Score
import dev.kkorolyov.ponk.system.FollowSystem
import dev.kkorolyov.ponk.system.ScoreSystem
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL46.*
import java.awt.Color

private const val editorSettings = "editor.ini"

private fun Lens.bindToWindow() {
	glfwSetFramebufferSizeCallback(window) { _, w, h ->
		size.x = w.toDouble()
		size.y = h.toDouble()
	}.use { }
}

private val window = run {
	if (!glfwInit()) throw IllegalStateException("Cannot init GLFW")
	glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
	val window = glfwCreateWindow(Config.get().getProperty("width").toInt(), Config.get().getProperty("height").toInt(), Config.get().getProperty("title"), 0, 0)
	if (window == 0L) throw IllegalStateException("Cannot create window")

	glfwMakeContextCurrent(window)
	glfwSwapInterval(1)

	glfwShowWindow(window)

	GL.createCapabilities()

	glEnable(GL_BLEND)
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

	window
}
private val container = Container(window).apply {
	Resources.inStream(editorSettings)?.use(::load)
}
private val editor: Window by lazy {
	Window("Editor", Editor(gameEngine)).apply { visible = false }
}

private val program = GLProgram(
	GLShader(GLShader.Type.VERTEX, Resources.inStream("shaders/literal.vert")),
	GLShader(GLShader.Type.FRAGMENT, Resources.inStream("shaders/user.frag"))
)
private val fontProgram = GLProgram(
	GLShader(GLShader.Type.VERTEX, Resources.inStream("shaders/font.vert")),
	GLShader(GLShader.Type.FRAGMENT, Resources.inStream("shaders/font.frag"))
)
private val font = Font("roboto-mono.ttf", 32)

private val drawStart = Pipeline.run {
	glfwMakeContextCurrent(window)
	glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
}
private val drawEnd = Pipeline.run {
	glfwSwapBuffers(window)
}
private val drawEditor = Pipeline.run {
	container(editor)
}

private val cameraQueue = CameraQueue()

private val gameEngine = GameEngine()

val actions = Resources.inStream("actions.yaml").use {
	Registry<Action>().apply {
		putAll(BasicParsers.yaml().andThen(ResourceConverters.get(Action::class.java)).parse(it))
	}
}

val paddleOffset = 4.0
val goalOffset = 6.0

val paddleVelocityCap = VelocityCap(Vector3.of(0.0, 20.0, 20.0))
val paddleDamping = Damping(Vector3.of())
val paddleMass = Mass(1e-2)
val ballMass = Mass(1e-9)

val paddleSize: Vector3 = Vector3.of(1.0, 4.0)
val ballSize: Vector3 = Vector3.of(1.0, 1.0)
val goalSize: Vector3 = Vector3.of(2.0, 10.0)
val netSize: Vector3 = Vector3.of(10.0, 2.0)

val ballTransform = Transform(Vector3.of())

val paddleBounds = Bounds.box(paddleSize)
val ballBounds = Bounds.round(ballSize.x / 2)
val goalBounds = Bounds.box(goalSize)
val netBounds = Bounds.box(netSize)

val paddleGraphic = Model(
	program, GLMesh(
		GLVertexBuffer {
			val color = Color.GREEN.toVector()
			rectangle(paddleSize) { position, _ ->
				add(position, color)
			}
		},
		mode = GLMesh.Mode.TRIANGLE_FAN
	)
)
val ballGraphic = Model(
	program, GLMesh(
		GLVertexBuffer {
			val color = Color.CYAN.toVector()
			ellipse(ballSize) { position, _ ->
				add(position, color)
			}
		},
		mode = GLMesh.Mode.TRIANGLE_FAN
	)
)
val goalGraphic = Model(
	program, GLMesh(
		GLVertexBuffer {
			val color = Color.BLUE.toVector()
			rectangle(goalSize) { position, _ ->
				add(position, color)
			}
		},
		mode = GLMesh.Mode.TRIANGLE_FAN
	)
)
val netGraphic = Model(
	program, GLMesh(
		GLVertexBuffer {
			val color = Color.RED.toVector()
			rectangle(netSize) { position, _ ->
				add(position, color)
			}
		},
		mode = GLMesh.Mode.TRIANGLE_FAN
	)
)

val camera = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of()),
		Lens(
			Vector2.of(64.0, 64.0),
			Vector2.of(Config.get().getProperty("width").toDouble(), Config.get().getProperty("height").toDouble())
		).apply {
			bindToWindow()
		},
		ActionQueue(),
		Input(
			Reaction.matchType(
				whenKey(GLFW_KEY_F1 to toggle(Compensated(Action { editor.visible = !editor.visible }, Action.NOOP)))
			)
		)
	)
}

val ball = gameEngine.entities.create().apply {
	put(
		ballGraphic,
		ballBounds,
		ballTransform,
		Velocity(Vector3.of()),
		VelocityCap(Vector3.of(50.0, 50.0)),
		Force(Vector3.of()),
		ballMass,
		Correctable(-1),
		Collidable(),
		ActionQueue(),
		Input(
			Reaction.matchType(
				whenKey(
					GLFW_KEY_SPACE to toggle(Compensated(actions["reset"], Action.NOOP))
				)
			)
		)
	)
}

val player = gameEngine.entities.create().apply {
	put(
		paddleGraphic,
		paddleBounds,
		Transform(Vector3.of(-paddleOffset)),
		Velocity(Vector3.of()),
		paddleVelocityCap,
		paddleDamping,
		Force(Vector3.of()),
		paddleMass,
		ActionQueue(),
		Correctable(),
		Collidable(),
		Input(
			Reaction.matchType(
				whenKey(
					GLFW_KEY_W to toggle(Compensated(actions["forceUp"], actions["forceDown"])),
					GLFW_KEY_S to toggle(Compensated(actions["forceDown"], actions["forceUp"])),
				)
			)
		)
	)
}
val opponent = gameEngine.entities.create().apply {
	put(
		paddleGraphic,
		paddleBounds,
		Transform(Vector3.of(paddleOffset)),
		Velocity(Vector3.of()),
		paddleVelocityCap,
		paddleDamping,
		Force(Vector3.of()),
		paddleMass,
		Follow(ballTransform.position, 0.2),
		Correctable(),
		Collidable()
	)
}

val goalPlayer = gameEngine.entities.create().apply {
	put(
		goalBounds,
		goalGraphic,
		Transform(Vector3.of(-goalOffset)),
		Score()
	)
}
val goalOpponent = gameEngine.entities.create().apply {
	put(
		goalBounds,
		goalGraphic,
		Transform(Vector3.of(goalOffset)),
		Score()
	)
}

val top = gameEngine.entities.create().apply {
	put(
		netBounds,
		netGraphic,
		Transform(Vector3.of(0.0, goalOffset))
	)
}
val bottom = gameEngine.entities.create().apply {
	put(
		netBounds,
		netGraphic,
		Transform(Vector3.of(0.0, -goalOffset))
	)
}

val scorePlayer = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(-paddleOffset, -paddleOffset, 1.0)),
		Model(fontProgram, font("0"))
	)
}
val scoreOpponent = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(paddleOffset, -paddleOffset, 1.0)),
		Model(fontProgram, font("0"))
	)
}

val helpText = gameEngine.entities.create().apply {
	put(
		Transform(Vector3.of(-0.5, -4.0, 1.0)),
		Model(fontProgram, font("Press SPACE to reset"))
	)
}

fun main() {
	gameEngine.setPipelines(
		Pipeline(
			InputSystem(window),
			ActionSystem()
		),
		Pipeline(
			AccelerationSystem(),
			CappingSystem(),
			MovementSystem(),
			DampingSystem(),
			IntersectionSystem(),
			CorrectionSystem(),
			CollisionSystem(),
			FollowSystem(),
			ScoreSystem(
				mapOf(
					goalPlayer to scoreOpponent,
					goalOpponent to scorePlayer
				),
				font
			),
			PhysicsCleanupSystem()
		).withFrequency(144),
		Pipeline(
			CameraSystem(cameraQueue),
			drawStart,
			DrawSystem(cameraQueue),
			drawEditor,
			drawEnd
		)
	)


	glfwSetWindowCloseCallback(window) {
		gameEngine.stop()
	}.use { }

	gameEngine.start()

	Resources.outStream(editorSettings)?.use(container::close) ?: container.close()
}
