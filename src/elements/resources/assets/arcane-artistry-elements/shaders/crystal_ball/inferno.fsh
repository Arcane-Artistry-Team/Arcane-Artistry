#version 330
#extension GL_ARB_separate_shader_objects : require

// Flickering flames rising from the bottom of the ball, colored with the fire meteor tail colors.

#define BRIGHTNESS 0.6
#define BASE_COLOR vec3(0.025, 0.006, 0.004)
#define TIME_SCALE 0.7

#include <arcane-artistry:effects/flames.glsl>
#include <arcane-artistry:crystal_ball/frame.glsl>

// Evenly spaced from cold to hot.
const int RAMP_SIZE = 6;
const vec3 RAMP[RAMP_SIZE] = vec3[](
    vec3(0.0),
    vec3(0.620, 0.165, 0.227), // 0x9E2A3A
    vec3(0.910, 0.333, 0.180), // 0xE8552E
    vec3(1.000, 0.647, 0.227), // 0xFFA53A
    vec3(1.000, 0.878, 0.400), // 0xFFE066
    vec3(1.000, 0.973, 0.878)  // 0xFFF8E0
);
const vec3 SPARK = vec3(1.000, 0.878, 0.400);

vec3 fireRamp(float heat) {
    float x = heat * float(RAMP_SIZE - 1);
    int i = min(int(x), RAMP_SIZE - 2);
    return mix(RAMP[i], RAMP[i + 1], x - float(i));
}

vec3 crystalBallColor(vec2 uv, float time) {
    vec2 f = flameField(uv, time);
    return fireRamp(f.x) + SPARK * f.y;
}
