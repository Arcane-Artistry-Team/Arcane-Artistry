#version 330
#extension GL_ARB_separate_shader_objects : require

// Placeholder: the default galaxy with an ember palette.

#define BRIGHTNESS 0.5
#define BASE_COLOR vec3(0.030, 0.008, 0.005)
#define TIME_SCALE 0.6

#include <arcane-artistry:galaxy.glsl>
#include <arcane-artistry:crystal_ball.glsl>

const vec3 EMBER = vec3(1.0, 0.45, 0.08);
const vec3 CRIMSON = vec3(0.85, 0.12, 0.10);
const vec3 CORE = vec3(1.0, 0.90, 0.60);

vec3 crystalBallColor(vec2 uv, float time) {
    float len = length(uv);
    vec3 v = galaxyField(uv, time);

    vec3 col = EMBER * v.y
        + CRIMSON * v.z * (1.3 + sin(time * 0.2) * 0.3)
        + CORE * v.x * 0.3
        + CORE * smoothstep(0.2, 0.0, len) * 0.35
        + CRIMSON * smoothstep(0.0, 0.6, v.z) * 0.2;
    return pow(abs(col), vec3(1.2));
}
