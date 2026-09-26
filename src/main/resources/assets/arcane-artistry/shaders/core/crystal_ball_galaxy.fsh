#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:dynamictransforms.glsl>

layout(location = 0) in vec2 uv;
layout(location = 1) in float time;

layout(location = 0) out vec4 fragColor;

const vec3 LAPIS = vec3(0.15, 0.38, 1.0);
const vec3 PURPLE = vec3(0.62, 0.22, 1.0);
const vec3 CORE = vec3(0.75, 0.68, 1.0);
const vec3 BASE = vec3(0.010, 0.010, 0.030);

const int STEPS = 60;
const float DEPTH = 3.15;
const float STEP_SIZE = DEPTH / float(STEPS);
const float STEP_WEIGHT = STEP_SIZE / 0.035;
const float BRIGHTNESS = 0.5;
const float ROTATION_SPEED = 0.025;

void main() {
    float len = length(uv);

    // The rotation angle grows towards the center, which twists the fractal into a spiral.
    float t = time * ROTATION_SPEED + ((0.25 + 0.05 * sin(time * ROTATION_SPEED)) / (len + 0.07)) * 2.2;
    float si = sin(t);
    float co = cos(t);
    mat2 rotation = mat2(co, si, -si, co);

    float v1 = 0.0;
    float v2 = 0.0;
    float v3 = 0.0;
    float s = 0.0;
    for (int i = 0; i < STEPS; i++) {
        vec3 p = s * vec3(uv, 0.0);
        p.xy *= rotation;
        p += vec3(0.22, 0.3, s - 1.5 - sin(time * 0.13) * 0.1);
        for (int j = 0; j < 8; j++) {
            p = abs(p) / dot(p, p) - 0.659;
        }
        float d = dot(p, p);
        v1 += d * 0.0015 * (1.8 + sin(len * 13.0 + 0.5 - time * 0.2));
        v2 += d * 0.0013 * (1.5 + sin(len * 14.5 + 1.2 - time * 0.3));
        v3 += length(p.xy * 10.0) * 0.0003;
        s += STEP_SIZE;
    }

    v1 *= smoothstep(0.7, 0.0, len) * STEP_WEIGHT;
    v2 *= smoothstep(0.5, 0.0, len) * STEP_WEIGHT;
    v3 *= smoothstep(0.9, 0.0, len) * STEP_WEIGHT;

    vec3 col = LAPIS * v2
        + PURPLE * v3 * (1.3 + sin(time * 0.2) * 0.3)
        + CORE * v1 * 0.3
        + CORE * smoothstep(0.2, 0.0, len) * 0.35
        + PURPLE * smoothstep(0.0, 0.6, v3) * 0.2;
    col = min(pow(abs(col), vec3(1.2)), 1.0);

    fragColor = vec4(BASE + col * BRIGHTNESS, 1.0) * ColorModulator;
}
