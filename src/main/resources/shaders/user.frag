#version 460 core

in vec3 color;

out vec4 result;

void main() {
	result = vec4(color, 1);
}
