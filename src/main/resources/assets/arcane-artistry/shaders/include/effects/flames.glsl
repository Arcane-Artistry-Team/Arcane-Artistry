#ifndef ARCANE_ARTISTRY_FLAMES_GLSL
#define ARCANE_ARTISTRY_FLAMES_GLSL

// Flames rising from the bottom of the crystal ball, made of domain warped fractal noise, with sparks drifting upwards. Returns two
// fields for the calling shader to color:
//   x  heat in [0, 1], hottest at the bottom rim, flame tongues reach about the center
//   y  sparks in [0, 1]

#include <arcane-artistry:utils/noise.glsl>

const float FLAME_RIM = 0.65;
const vec2 FLAME_SCALE = vec2(3.0, 1.8); // stretched vertically, so the tongues are tall
const float FLAME_SPEED = 1.2;
const float FLAME_WARP = 0.6;
const float FLAME_HEIGHT = 1.0; // how fast the heat falls off towards the top

const float SPARK_CELL = 0.09;
const float SPARK_SPEED = 0.45;
const float SPARK_DENSITY = 0.12; // share of cells with a spark
const float SPARK_RADIUS = 0.005;

float flameHeat(vec2 uv, float time) {
    // +y is down, so sampling further down over time moves the noise up.
    vec2 q = uv * FLAME_SCALE + vec2(0.0, time * FLAME_SPEED);
    q += FLAME_WARP * vec2(fbm2(q * 0.8 + time * 0.3), fbm2(q * 0.8 + vec2(4.3, 1.7) - time * 0.3));
    float n = fbm2(q);

    float height = clamp((uv.y + FLAME_RIM) / (2.0 * FLAME_RIM), 0.0, 1.0); // 0 at the top, 1 at the bottom
    float heat = n * 1.7 - (1.0 - height) * FLAME_HEIGHT + 0.05;

    // Global flicker plus a slower one per column, so the flames don't pulse in unison.
    float flicker = 0.92 + 0.08 * sin(time * 7.3) * sin(time * 3.1 + 1.7);
    flicker *= 0.85 + 0.15 * noise2(vec2(uv.x * 2.5, time * 4.0));
    return clamp(heat * flicker, 0.0, 1.0);
}

float flameSparks(vec2 uv, float time) {
    vec2 p = uv + vec2(0.03 * sin(uv.y * 6.0 + time * 2.0), time * SPARK_SPEED);
    vec2 cell = floor(p / SPARK_CELL);
    float r = hash21(cell);
    if (r > SPARK_DENSITY) {
        return 0.0;
    }

    // Keep the spark away from the cell border, so it is never cut off.
    vec2 center = (cell + 0.25 + 0.5 * vec2(hash21(cell + 7.1), hash21(cell + 3.3))) * SPARK_CELL;
    float spark = smoothstep(SPARK_RADIUS, SPARK_RADIUS * 0.3, length(p - center));

    float height = clamp((uv.y + FLAME_RIM) / (2.0 * FLAME_RIM), 0.0, 1.0);
    float twinkle = 0.6 + 0.4 * sin(time * 9.0 + r * 80.0);
    return spark * twinkle * smoothstep(0.1, 0.7, height);
}

vec2 flameField(vec2 uv, float time) {
    return vec2(flameHeat(uv, time), flameSparks(uv, time));
}

#endif
