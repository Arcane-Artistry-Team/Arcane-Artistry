#ifndef ARCANE_ARTISTRY_NOISE_GLSL
#define ARCANE_ARTISTRY_NOISE_GLSL

// Pseudo random value in [0, 1) for a 2D position. Uses no sin, so it stays stable for large inputs.
float hash21(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

// Smooth value noise in [0, 1].
float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

// Fractal noise in roughly [0, 1], every octave is rotated to hide the grid.
const int FBM_OCTAVES = 5;
const mat2 FBM_ROTATION = mat2(1.6, 1.2, -1.2, 1.6);

float fbm2(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < FBM_OCTAVES; i++) {
        value += amplitude * noise2(p);
        p = FBM_ROTATION * p;
        amplitude *= 0.5;
    }
    return value / (1.0 - pow(0.5, float(FBM_OCTAVES)));
}

#endif
