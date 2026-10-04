#version 330
#extension GL_ARB_separate_shader_objects : require

// Shared vertex shader for every crystal ball background. It is the interface between the Java side and the fragment shaders, so it
// only passes data through:
//   uv    position relative to the ball's center, the rim is at a distance of 0.65
//   time  seconds since the screen opened, plus an offset
//   pixel size of one GUI pixel in uv units
//   revealOrigin  center of the reveal circle in GUI pixels, relative to the ball's center
//   revealRadius  radius of the reveal circle in GUI pixels

#include <minecraft:projection.glsl>
#include <minecraft:dynamictransforms.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec2 UV3;
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;

layout(location = 0) out vec2 uv;
layout(location = 1) out float time;
layout(location = 2) out float pixel;
layout(location = 3) flat out ivec2 revealOrigin;
layout(location = 4) flat out int revealRadius;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    uv = UV0;
    time = UV3.x;
    pixel = UV3.y;
    revealOrigin = UV1;
    revealRadius = UV2.x;
}
