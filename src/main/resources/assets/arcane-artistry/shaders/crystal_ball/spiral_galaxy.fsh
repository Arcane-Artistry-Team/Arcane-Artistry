#version 330
#extension GL_ARB_separate_shader_objects : require

// Spiral galaxy in lapis and purple. The default crystal ball background for staff types without their own shader.

#define BRIGHTNESS 0.5
#define BASE_COLOR vec3(0.010, 0.010, 0.030)

#include <arcane-artistry:effects/spiral_galaxy.glsl>
#include <arcane-artistry:crystal_ball/frame.glsl>

const vec3 LAPIS = vec3(0.15, 0.38, 1.0);
const vec3 PURPLE = vec3(0.62, 0.22, 1.0);
const vec3 CORE = vec3(0.75, 0.68, 1.0);

vec3 crystalBallColor(vec2 uv, float time) {
    float len = length(uv);
    vec3 v = galaxyField(uv, time);

    vec3 col = LAPIS * v.y
        + PURPLE * v.z * (1.3 + sin(time * 0.2) * 0.3)
        + CORE * v.x * 0.3
        + CORE * smoothstep(0.2, 0.0, len) * 0.35
        + PURPLE * smoothstep(0.0, 0.6, v.z) * 0.2;
    return pow(abs(col), vec3(1.2));
}
