#ifndef ARCANE_ARTISTRY_CRYSTAL_BALL_FRAME_GLSL
#define ARCANE_ARTISTRY_CRYSTAL_BALL_FRAME_GLSL

// Frame for every crystal ball background shader. A shader optionally defines the settings below, includes this file and implements
// crystalBallColor. Pixelation, posterizing, the reveal circle and ColorModulator are applied here, so the shader only describes the look.
//   uv    position relative to the ball's center, snapped to the pixel grid; the rim is at a distance of 0.65
//   time  animation time, already multiplied by TIME_SCALE

#include <minecraft:dynamictransforms.glsl>
#include <arcane-artistry:utils/pixel_art.glsl>

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
#ifndef REVEAL_RIM_WIDTH
#define REVEAL_RIM_WIDTH 8.0 // GUI pixels of the glowing rim at the edge of the reveal circle
#endif

layout(location = 0) in vec2 rawUv;
layout(location = 1) in float rawTime;
layout(location = 2) in float guiPixel;
layout(location = 3) flat in ivec2 revealOrigin;
layout(location = 4) flat in int revealRadius;

layout(location = 0) out vec4 fragColor;

vec3 crystalBallColor(vec2 uv, float time);

void main() {
    vec2 uv = snapToPixel(rawUv, guiPixel * PIXEL_SIZE);
    float revealDistance = length(uv / guiPixel - vec2(revealOrigin));
    if (revealDistance > float(revealRadius)) {
        discard;
    }

    vec3 col = clamp(crystalBallColor(uv, rawTime * TIME_SCALE), 0.0, 1.0);
    float rim = smoothstep(float(revealRadius) - REVEAL_RIM_WIDTH, float(revealRadius), revealDistance);
    col = clamp(col + rim * (col * 1.5 + 0.25), 0.0, 1.0);
    col = posterize(col, COLOR_LEVELS);

    fragColor = vec4(BASE_COLOR + col * BRIGHTNESS, 1.0) * ColorModulator;
}

#endif
