#ifndef ARCANE_ARTISTRY_PIXEL_ART_GLSL
#define ARCANE_ARTISTRY_PIXEL_ART_GLSL

// Snaps a position to the center of its cell in a grid of the given size, so every fragment in a cell gets the same value.
vec2 snapToPixel(vec2 position, float cellSize) {
    return (floor(position / cellSize) + 0.5) * cellSize;
}

// Rounds every channel to the nearest of the given number of steps, 0 disables it.
vec3 posterize(vec3 color, float levels) {
    if (levels <= 0.0) {
        return color;
    }
    return floor(color * levels + 0.5) / levels;
}

#endif
