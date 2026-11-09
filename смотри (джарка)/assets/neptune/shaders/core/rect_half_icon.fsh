#version 150

#moj_import <neptune:common.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 TexCoord;
in vec4 VertexColor;
in vec2 LocalPos;
flat in int RectIndex;

layout(std140) uniform RectHalfIconParamsArray {
    vec4 params[768];
};

out vec4 OutColor;

// Coverage of the icon averaged over the screen pixel: without mipmaps a large icon drawn small turns jagged.
float sampleAlpha(vec2 uv, vec2 dx, vec2 dy) {
    vec2 texels = vec2(textureSize(Sampler0, 0));
    if (max(length(dx * texels), length(dy * texels)) <= 1.25) {
        return texture(Sampler0, uv).a;
    }

    float sum = 0.0;
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            vec2 offset = (vec2(float(i), float(j)) + 0.5) / 4.0 - 0.5;
            sum += texture(Sampler0, uv + dx * offset.x + dy * offset.y).a;
        }
    }

    return sum / 16.0;
}

void main() {
    // Derivatives are taken before any discard.
    vec2 uvDx = dFdx(TexCoord);
    vec2 uvDy = dFdy(TexCoord);
    int base = RectIndex * 3;
    vec4 rect = params[base];
    vec4 radius = params[base + 1];
    vec4 smoothness = params[base + 2];
    vec2 size = max(rect.zw, vec2(1.0));

    if (LocalPos.x < 0.0 || LocalPos.y < 0.0 || LocalPos.x > size.x || LocalPos.y > size.y) {
        discard;
    }

    vec2 coord = LocalPos / size;
    float shapeAlpha = ralpha(size, coord, max(radius, vec4(0.0)), max(smoothness.x, 0.001));
    float alpha = sampleAlpha(TexCoord, uvDx, uvDy) * VertexColor.a * shapeAlpha;

    if (alpha <= 0.001) {
        discard;
    }

    OutColor = vec4(VertexColor.rgb, alpha) * ColorModulator;
}
