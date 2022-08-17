#version 460 core

layout(location = 0) uniform mat4 transform;

layout(location = 0) in vec2 position;
layout(location = 1) in vec3 inColor;

out vec3 color;

void main() {
	gl_Position = transform * vec4(position, 0, 1);
	color = inColor;
}
