#version 150

#moj_import <neptune:common.glsl>

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 TexCoord;
in vec2 FragCoord;
flat in int QuadIndex;

layout(std140) uniform ImageParamsArray {
    vec4 params[3072];
};

out vec4 OutColor;

vec4 imageColor(vec2 coord, vec4 topLeft, vec4 topRight, vec4 bottomRight, vec4 bottomLeft) {
    return mix(mix(topLeft, topRight, coord.x), mix(bottomLeft, bottomRight, coord.x), coord.y);
}

// The textures have no mipmaps: a 96px icon drawn 16px wide would read only 4 of its texels and look jagged. When a
// screen pixel covers more than one texel, average a 4x4 grid of samples spread over the whole pixel (premultiplied,
// so transparent texels do not darken the edges).
vec4 sampleImage(vec2 uv) {
    vec2 dx = dFdx(uv);
    vec2 dy = dFdy(uv);
    vec2 texels = vec2(textureSize(Sampler0, 0));
    float footprint = max(length(dx * texels), length(dy * texels));
    if (footprint <= 1.25) {
        return texture(Sampler0, uv);
    }

    vec4 sum = vec4(0.0);
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            vec2 offset = (vec2(float(i), float(j)) + 0.5) / 4.0 - 0.5;
            vec4 texel = texture(Sampler0, uv + dx * offset.x + dy * offset.y);
            sum += vec4(texel.rgb * texel.a, texel.a);
        }
    }

    sum /= 16.0;
    return sum.a > 0.0001 ? vec4(sum.rgb / sum.a, sum.a) : vec4(0.0);
}

void main() {
    int base = QuadIndex * 6;
    vec4 radius = params[base];
    vec4 sizeSmooth = params[base + 1];
    vec4 topLeft = params[base + 2];
    vec4 topRight = params[base + 3];
    vec4 bottomRight = params[base + 4];
    vec4 bottomLeft = params[base + 5];

    vec2 coord = clamp(FragCoord, vec2(0.0), vec2(1.0));
    float shapeAlpha = ralpha(max(sizeSmooth.xy, vec2(1.0)), coord, radius, sizeSmooth.z);
    vec4 textureColor = sampleImage(TexCoord);
    vec4 color = textureColor * imageColor(coord, topLeft, topRight, bottomRight, bottomLeft);
    color.a *= shapeAlpha;

    if (color.a <= 0.001) {
        discard;
    }

    OutColor = color * ColorModulator;
}
