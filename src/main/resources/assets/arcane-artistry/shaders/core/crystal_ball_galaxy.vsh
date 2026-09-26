#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:projection.glsl>
#include <minecraft:dynamictransforms.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec2 UV3;

layout(location = 0) out vec2 uv;
layout(location = 1) out float time;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    uv = UV0;
    time = UV3.x;
}
