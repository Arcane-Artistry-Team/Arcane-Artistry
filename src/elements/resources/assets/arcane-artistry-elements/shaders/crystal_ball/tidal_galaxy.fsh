#version 330
#extension GL_ARB_separate_shader_objects : require

// Placeholder: the spiral galaxy with a teal palette.

#define BRIGHTNESS 0.5
#define BASE_COLOR vec3(0.005, 0.015, 0.030)
#define TIME_SCALE 0.3

#include <arcane-artistry:effects/spiral_galaxy.glsl>
#include <arcane-artistry:crystal_ball/frame.glsl>

const vec3 TEAL = vec3(0.10, 0.75, 0.85);
const vec3 DEEP = vec3(0.10, 0.30, 0.95);
const vec3 CORE = vec3(0.75, 0.95, 1.0);

vec3 crystalBallColor(vec2 uv, float time) {
    float len = length(uv);
    vec3 v = galaxyField(uv, time);

    vec3 col = TEAL * v.y
        + DEEP * v.z * (1.3 + sin(time * 0.2) * 0.3)
        + CORE * v.x * 0.3
        + CORE * smoothstep(0.2, 0.0, len) * 0.35
        + DEEP * smoothstep(0.0, 0.6, v.z) * 0.2;
    return pow(abs(col), vec3(1.2));
}
