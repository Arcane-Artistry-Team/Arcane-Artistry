#ifndef ARCANE_ARTISTRY_GALAXY_GLSL
#define ARCANE_ARTISTRY_GALAXY_GLSL

// Spiral galaxy made of a volumetric Kali fractal. Returns three density layers that fade out towards the rim, for the calling shader
// to color:
//   x  wide rings, fades out at 0.7
//   y  inner rings, fades out at 0.5
//   z  soft haze, fades out at 0.9

const int GALAXY_STEPS = 60;
const float GALAXY_DEPTH = 3.15;
const float GALAXY_STEP_SIZE = GALAXY_DEPTH / float(GALAXY_STEPS);
const float GALAXY_STEP_WEIGHT = GALAXY_STEP_SIZE / 0.035;
const float GALAXY_ROTATION_SPEED = 0.025;

vec3 galaxyField(vec2 uv, float time) {
    float len = length(uv);

    // The rotation angle grows towards the center, which twists the fractal into a spiral.
    float t = time * GALAXY_ROTATION_SPEED
        + ((0.25 + 0.05 * sin(time * GALAXY_ROTATION_SPEED)) / (len + 0.07)) * 2.2;
    float si = sin(t);
    float co = cos(t);
    mat2 rotation = mat2(co, si, -si, co);

    float v1 = 0.0;
    float v2 = 0.0;
    float v3 = 0.0;
    float s = 0.0;
    for (int i = 0; i < GALAXY_STEPS; i++) {
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
        s += GALAXY_STEP_SIZE;
    }

    return vec3(
        v1 * smoothstep(0.7, 0.0, len),
        v2 * smoothstep(0.5, 0.0, len),
        v3 * smoothstep(0.9, 0.0, len)
    ) * GALAXY_STEP_WEIGHT;
}

#endif
