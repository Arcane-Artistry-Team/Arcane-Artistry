#ifndef ARCANE_ARTISTRY_CRYSTAL_BALL_GLSL
#define ARCANE_ARTISTRY_CRYSTAL_BALL_GLSL

// Frame for every crystal ball background shader. A shader optionally defines the settings below, includes this file and implements
// crystalBallColor. Pixelation, posterizing and ColorModulator are applied here, so the shader only describes the look.
//   uv    position relative to the ball's center, snapped to the pixel grid; the rim is at a distance of 0.65
//   time  animation time, already multiplied by TIME_SCALE

#include <minecraft:dynamictransforms.glsl>

#ifndef PIXEL_SIZE
#define PIXEL_SIZE 2.0 // GUI pixels per shader pixel
#endif
#ifndef COLOR_LEVELS
#define COLOR_LEVELS 12.0 // brightness steps per channel, 0 disables posterizing
#endif
#ifndef TIME_SCALE
#define TIME_SCALE 0.4
#endif
#ifndef BRIGHTNESS
#define BRIGHTNESS 1.0
#endif
#ifndef BASE_COLOR
#define BASE_COLOR vec3(0.0)
#endif

layout(location = 0) in vec2 rawUv;
layout(location = 1) in float rawTime;
layout(location = 2) in float guiPixel;

layout(location = 0) out vec4 fragColor;

vec3 crystalBallColor(vec2 uv, float time);

void main() {
    float cell = guiPixel * PIXEL_SIZE;
    vec2 uv = (floor(rawUv / cell) + 0.5) * cell;

    vec3 col = clamp(crystalBallColor(uv, rawTime * TIME_SCALE), 0.0, 1.0);
    if (COLOR_LEVELS > 0.0) {
        col = floor(col * COLOR_LEVELS + 0.5) / COLOR_LEVELS;
    }

    fragColor = vec4(BASE_COLOR + col * BRIGHTNESS, 1.0) * ColorModulator;
}

#endif
