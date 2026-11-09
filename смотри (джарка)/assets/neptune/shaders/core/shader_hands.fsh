#version 150

// Sampler0: depth of the hand pass (hand pixels have depth < 1)
// Sampler1: color of the frame with the original hand
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

layout(std140) uniform ShaderHandsParams {
    vec4 Color1;   // rgb, opacity
    vec4 Color2;   // rgb, brightness
    vec4 Params;   // time, scale, mode, texture mix
    vec4 Effect;   // hue shift (turns), outline width px, glow strength, glow radius px
    vec4 Screen;   // 1/width, 1/height, aspect, outline enabled
};

in vec2 TexCoord;

out vec4 OutColor;

const float TAU = 6.28318530718;

// Modes, same order as ShaderHandsModule.SHADERS
const int RAINBOW = 0;
const int GRADIENT = 1;
const int GALAXY = 2;
const int FIRE = 3;
const int WATER = 4;
const int PLASMA = 5;
const int HOLOGRAM = 6;
const int MATRIX = 7;
const int LIGHTNING = 8;
const int AURORA = 9;
const int LAVA = 10;
const int NEON = 11;
const int GLASS = 12;
const int CHROME = 13;
const int GLITCH = 14;
const int CRYSTALS = 15;

float handAt(vec2 uv) {
    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) return 0.0;
    return 1.0 - step(1.0, texture(Sampler0, uv).r);
}

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hash1(float n) {
    return fract(sin(n) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec2(17.1, 9.2);
        amplitude *= 0.5;
    }
    return value;
}

// x: distance to nearest cell, y: distance to the cell border, z: cell id
vec3 voronoi(vec2 x, float t) {
    vec2 n = floor(x);
    vec2 f = fract(x);
    float d1 = 8.0;
    float d2 = 8.0;
    float id = 0.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 g = vec2(float(i), float(j));
            vec2 cell = n + g;
            vec2 o = 0.5 + 0.45 * sin(t + TAU * vec2(hash(cell), hash(cell + 19.19)));
            vec2 r = g + o - f;
            float d = dot(r, r);
            if (d < d1) {
                d2 = d1;
                d1 = d;
                id = hash(cell + 3.7);
            } else if (d < d2) {
                d2 = d;
            }
        }
    }
    return vec3(sqrt(d1), sqrt(d2) - sqrt(d1), id);
}

vec3 hsv2rgb(vec3 c) {
    vec3 p = abs(fract(c.xxx + vec3(1.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return c.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), c.y);
}

vec3 hueRotate(vec3 c, float turns) {
    float a = turns * TAU;
    const vec3 k = vec3(0.57735);
    float ca = cos(a);
    return c * ca + cross(k, c) * sin(a) + k * dot(k, c) * (1.0 - ca);
}

vec3 firePalette(float h) {
    h = clamp(h, 0.0, 1.2);
    return clamp(vec3(h * 1.8, h * h * 1.3, pow(h, 4.0) * 1.1), 0.0, 1.0);
}

vec3 sceneAt(vec2 uv) {
    return texture(Sampler1, clamp(uv, vec2(0.0), vec2(1.0))).rgb;
}

// rim: 0 deep inside the hand, 1 at the silhouette; n: rough normal built from the hand silhouette
vec3 shade(int mode, vec2 uv, float rim, vec3 n, out float cover, out bool usesScene) {
    float t = Params.x;
    float scale = max(Params.y, 0.05);
    vec2 p = (uv - 0.5) * vec2(Screen.z, 1.0) * 3.0 / scale;
    vec3 c1 = Color1.rgb;
    vec3 c2 = Color2.rgb;
    cover = 1.0;
    usesScene = false;

    if (mode == RAINBOW) {
        float hue = fract(p.x * 0.12 + p.y * 0.08 + t * 0.25);
        vec3 col = hsv2rgb(vec3(hue, 0.78, 1.0));
        float sheen = pow(max(0.0, sin(p.x * 0.9 - p.y * 0.4 - t * 2.2)), 24.0);
        return col + sheen * 0.45;
    }

    if (mode == GRADIENT) {
        float g = 0.5 + 0.5 * sin(p.x * 1.2 + p.y * 0.8 + t * 1.5);
        vec3 col = mix(c1, c2, smoothstep(0.0, 1.0, g));
        float sheen = pow(max(0.0, sin(p.x * 0.7 + p.y * 0.3 - t * 2.0)), 20.0);
        return col + sheen * 0.55;
    }

    if (mode == GALAXY) {
        vec2 q = p * 0.6;
        float n1 = fbm(q * 1.5 + vec2(t * 0.05, t * 0.03));
        float n2 = fbm(q * 3.0 - t * 0.04 + n1);
        vec3 col = mix(vec3(0.02, 0.0, 0.06), c1 * 0.9, smoothstep(0.35, 0.85, n1));
        col = mix(col, c2, smoothstep(0.5, 0.95, n2) * 0.8);
        float stars = 0.0;
        for (int layer = 0; layer < 3; layer++) {
            float fl = float(layer);
            vec2 g = p * (6.0 + fl * 5.0) + fl * 13.7;
            vec2 id = floor(g);
            vec2 f = fract(g) - 0.5;
            float h = hash(id);
            if (h > 0.88) {
                vec2 offset = (vec2(hash(id + 1.3), hash(id + 7.1)) - 0.5) * 0.6;
                float d = length(f - offset);
                float twinkle = 0.55 + 0.45 * sin(t * 3.0 + h * 60.0);
                stars += smoothstep(0.11, 0.0, d) * twinkle * (1.0 - fl * 0.25);
            }
        }
        return col + vec3(stars);
    }

    if (mode == FIRE) {
        vec2 q = p * vec2(1.0, 0.8);
        float n1 = fbm(vec2(q.x * 2.0, q.y * 2.0 - t * 2.5));
        float n2 = fbm(vec2(q.x * 4.0 + n1 * 1.5, q.y * 3.0 - t * 3.5));
        float heat = n1 * 0.65 + n2 * 0.55 + (1.0 - uv.y) * 0.35 - 0.25;
        return firePalette(heat);
    }

    if (mode == WATER) {
        vec2 q = mod(p * 1.4, TAU) - 250.0;
        vec2 i = q;
        float c = 1.0;
        float intensity = 0.005;
        for (int k = 0; k < 5; k++) {
            float tt = t * 0.6 * (1.0 - (3.5 / float(k + 1)));
            i = q + vec2(cos(tt - i.x) + sin(tt + i.y), sin(tt - i.y) + cos(tt + i.x));
            c += 1.0 / max(length(vec2(q.x / (sin(i.x + tt) / intensity), q.y / (cos(i.y + tt) / intensity))), 1e-3);
        }
        c /= 5.0;
        c = 1.17 - pow(c, 1.4);
        float caustic = pow(abs(c), 8.0);
        vec3 deep = mix(vec3(0.0, 0.22, 0.45), c1 * 0.6, 0.35);
        return clamp(deep + vec3(caustic) * vec3(0.75, 0.95, 1.0), 0.0, 1.5);
    }

    if (mode == PLASMA) {
        float v = sin(p.x * 2.0 + t);
        v += sin(p.y * 2.0 + t * 1.3);
        v += sin((p.x + p.y) * 1.5 + t * 0.7);
        v += sin(length(p * 2.0 + vec2(sin(t * 0.5), cos(t * 0.3)) * 2.0) * 2.0 - t);
        v *= 0.25;
        vec3 col = 0.5 + 0.5 * cos(TAU * (v * 0.5 + vec3(0.0, 0.33, 0.67)) + t * 0.3);
        return mix(col, mix(c1, c2, 0.5 + 0.5 * v), 0.45);
    }

    if (mode == HOLOGRAM) {
        float lines = 0.65 + 0.35 * sin(uv.y / Screen.y * 0.9 - t * 12.0);
        float band = 1.0 - smoothstep(0.0, 0.06, abs(fract(uv.y * 0.5 - t * 0.3) - 0.5));
        float flicker = 0.85 + 0.15 * noise(vec2(t * 15.0, 0.0));
        float detail = dot(sceneAt(uv), vec3(0.299, 0.587, 0.114));
        cover = clamp(0.5 + rim * 0.5 + band * 0.3, 0.0, 1.0);
        return c1 * (0.35 + rim * 1.6 + detail * 0.4) * lines * flicker + c2 * band * 0.8;
    }

    if (mode == MATRIX) {
        float cellSize = 12.0 * scale;
        vec2 pixel = uv / Screen.xy;
        vec2 cell = floor(pixel / cellSize);
        vec2 f = fract(pixel / cellSize);
        float columnSpeed = 2.0 + hash1(cell.x * 1.7) * 4.0;
        float trail = fract(cell.y * 0.04 + t * columnSpeed * 0.12 + hash1(cell.x * 3.1));
        float head = step(0.965, trail);
        vec2 sub = floor(f * vec2(3.0, 5.0));
        float bit = step(0.45, hash(cell * 7.0 + sub + floor(t * 4.0 + hash1(cell.x + cell.y * 31.0) * 10.0)));
        float glyph = bit * step(0.12, f.x) * step(f.x, 0.88) * step(0.08, f.y) * step(f.y, 0.92);
        vec3 green = vec3(0.1, 1.0, 0.4);
        return vec3(0.0, 0.06, 0.02) + green * glyph * pow(trail, 2.5) * 1.3 + vec3(0.8, 1.0, 0.85) * glyph * head;
    }

    if (mode == LIGHTNING) {
        vec3 col = mix(vec3(0.02, 0.02, 0.08), c1 * 0.15, 0.5);
        float bolt = 0.0;
        for (int k = 0; k < 3; k++) {
            float fk = float(k);
            float x = sin(t * 0.7 + fk * 2.1) * 1.4;
            float wobble = (fbm(vec2(p.y * 2.5 + fk * 7.0, t * 4.0 + fk * 10.0)) - 0.5) * 1.1;
            float branch = (fbm(vec2(p.y * 7.0 + fk * 3.0, t * 6.0 + fk)) - 0.5) * 0.35;
            float d = abs(p.x - x - wobble - branch);
            float flash = 0.35 + 0.65 * step(0.35, noise(vec2(t * 6.0, fk * 5.0)));
            bolt += min(0.01 / max(d, 0.002), 3.0) * flash;
        }
        return col + mix(c1, vec3(1.0), 0.35) * bolt + c2 * bolt * 0.25;
    }

    if (mode == AURORA) {
        vec2 q = p * 0.5;
        vec3 col = vec3(0.01, 0.02, 0.06);
        for (int k = 0; k < 4; k++) {
            float fk = float(k);
            float y0 = fk * 0.22 - 0.35 + 0.25 * sin(q.x * 1.5 + t * 0.4 + fk * 1.7) + 0.15 * fbm(vec2(q.x * 2.0 + t * 0.2, fk));
            float d = q.y - y0;
            float curtain = exp(-d * d * 30.0) * (0.5 + 0.5 * fbm(vec2(q.x * 6.0 + fk * 3.0, t * 0.5)));
            col += mix(vec3(0.1, 1.0, 0.5), vec3(0.6, 0.2, 1.0), fk / 3.0) * curtain;
        }
        return col * (0.75 + 0.25 * noise(vec2(q.x * 40.0, t)));
    }

    if (mode == LAVA) {
        vec3 v = voronoi(p * 1.6 + vec2(0.0, t * 0.1), t * 0.2);
        float crack = 1.0 - smoothstep(0.0, 0.08 + 0.05 * sin(t + v.z * TAU), v.y);
        float heat = fbm(p * 2.0 + t * 0.2);
        vec3 crust = vec3(0.08, 0.03, 0.02) * (0.6 + 0.8 * fbm(p * 6.0));
        vec3 glowColor = mix(vec3(1.0, 0.25, 0.0), vec3(1.0, 0.85, 0.2), heat);
        return mix(crust, glowColor * 1.5, crack) + glowColor * 0.25 * heat * heat;
    }

    if (mode == NEON) {
        vec3 col = mix(c1, c2, 0.5 + 0.5 * sin(uv.y * 6.0 + uv.x * 2.0 + t * 2.0));
        float strip = pow(max(0.0, sin(uv.y * 30.0 - t * 4.0)), 30.0);
        cover = clamp(0.2 + rim * 1.3 + strip * 0.4, 0.0, 1.0);
        return col * (pow(rim, 0.7) * 2.2 + 0.1) + col * strip;
    }

    if (mode == GLASS) {
        usesScene = true;
        vec2 wobble = (vec2(noise(p * 3.0 + t * 0.3), noise(p * 3.0 + 17.0 - t * 0.3)) - 0.5) * 0.03;
        vec2 offset = wobble + n.xy * 0.035;
        vec3 refracted = vec3(sceneAt(uv + offset * 1.15).r, sceneAt(uv + offset).g, sceneAt(uv + offset * 0.85).b);
        float fresnel = pow(rim, 1.5);
        float streak = pow(max(0.0, sin((p.x + p.y) * 2.0 - t * 1.5)), 40.0);
        return refracted * mix(vec3(1.0), c1, 0.15) + mix(vec3(1.0), c1, 0.4) * fresnel * 0.9 + vec3(streak * 0.7);
    }

    if (mode == CHROME) {
        vec3 bump = vec3((noise(p * 2.0 + t * 0.2) - 0.5) * 0.8, (noise(p * 2.0 + 5.3 - t * 0.2) - 0.5) * 0.8, 0.0);
        vec3 nn = normalize(n + bump);
        vec3 r = reflect(vec3(0.0, 0.0, -1.0), nn);
        vec3 env = r.y > 0.0
            ? mix(vec3(0.85, 0.9, 1.0), vec3(0.3, 0.45, 0.8), r.y)
            : mix(vec3(0.3, 0.25, 0.22), vec3(0.04), -r.y);
        env += 0.2 * sin(r.x * 8.0 + t);
        float spec = pow(max(0.0, dot(nn, normalize(vec3(0.4, 0.6, 1.0)))), 48.0);
        return env * mix(vec3(1.0), c1, 0.25) + vec3(spec);
    }

    if (mode == GLITCH) {
        usesScene = true;
        vec2 block = floor(uv * vec2(8.0, 24.0) / scale);
        float tick = floor(t * 8.0);
        float shift = hash(vec2(block.y, tick)) > 0.75 ? (hash(vec2(block.y * 3.0, tick)) - 0.5) * 0.08 : 0.0;
        float split = 0.004 + 0.004 * step(0.9, hash(vec2(tick, 1.0)));
        vec3 col = vec3(
            sceneAt(uv + vec2(shift + split, 0.0)).r,
            sceneAt(uv + vec2(shift, 0.0)).g,
            sceneAt(uv + vec2(shift - split, 0.0)).b
        );
        float bandSeed = hash(vec2(block.y * 7.0, floor(t * 4.0)));
        if (bandSeed > 0.86) {
            col = mix(col, bandSeed > 0.93 ? c1 : c2, 0.6);
        }
        float scan = 0.85 + 0.15 * sin(uv.y / Screen.y * 3.14159);
        return col * scan;
    }

    // CRYSTALS
    vec3 v = voronoi(p * 2.0, t * 0.15);
    vec3 facet = mix(c1, c2, v.z);
    float shine = 0.55 + 0.45 * sin(v.z * 20.0 + t * 1.5);
    float edges = 1.0 - smoothstep(0.0, 0.05, v.y);
    float sparkle = pow(max(0.0, sin(v.z * 40.0 + t * 4.0)), 30.0);
    return facet * shine + vec3(edges * 0.8 + sparkle * 0.8);
}

vec3 finish(vec3 col) {
    if (Effect.x > 0.0) {
        col = hueRotate(col, Effect.x);
    }
    return max(col, vec3(0.0)) * Color2.a;
}

void main() {
    vec2 uv = TexCoord;
    int mode = int(Params.z + 0.5);
    vec2 px = Screen.xy;

    if (handAt(uv) < 0.5) {
        if (Screen.w < 0.5) discard;

        // Outline: any hand pixel within the outline width
        float width = Effect.y;
        float edge = 0.0;
        float edgeSoft = 0.0;
        for (int i = 0; i < 16; i++) {
            float a = float(i) * (TAU / 16.0);
            vec2 dir = vec2(cos(a), sin(a)) * px;
            edge = max(edge, handAt(uv + dir * width));
            edgeSoft = max(edgeSoft, handAt(uv + dir * (width + 1.0)));
        }
        edge = max(edge, edgeSoft * 0.5);

        float glowAmount = 0.0;
        if (Effect.z > 0.0) {
            float total = 0.0;
            for (int ring = 1; ring <= 4; ring++) {
                float radius = Effect.w * float(ring) / 4.0 + width;
                float weight = 1.0 - float(ring - 1) / 4.0;
                for (int i = 0; i < 12; i++) {
                    float a = (float(i) + float(ring) * 0.5) * (TAU / 12.0);
                    glowAmount += handAt(uv + vec2(cos(a), sin(a)) * px * radius) * weight;
                    total += weight;
                }
            }
            glowAmount = pow(glowAmount / total, 0.7) * Effect.z;
        }

        float alpha = clamp(edge * 0.95 + glowAmount * (1.0 - edge), 0.0, 1.0);
        if (alpha < 0.003) discard;

        // Outline and glow take the effect's own color at this point, lifted to full brightness
        float unusedCover;
        bool usesScene;
        vec3 col = shade(mode, uv, 1.0, vec3(0.0, 0.0, 1.0), unusedCover, usesScene);
        if (usesScene) {
            col = mix(Color1.rgb, Color2.rgb, 0.5 + 0.5 * sin(uv.y * 6.0 + Params.x * 2.0));
        }
        col /= max(max(col.r, max(col.g, col.b)), 0.25);
        OutColor = vec4(finish(col), alpha);
        return;
    }

    // Distance to the silhouette and a rough normal from the surrounding hand coverage
    float coverage = 0.0;
    vec2 gradient = vec2(0.0);
    for (int i = 0; i < 12; i++) {
        float a = float(i) * (TAU / 12.0);
        vec2 dir = vec2(cos(a), sin(a));
        float near = handAt(uv + dir * px * 7.0);
        float far = handAt(uv + dir * px * 14.0);
        coverage += near + far;
        gradient += dir * (near + far);
    }
    float rim = clamp((1.0 - coverage / 24.0) * 2.0, 0.0, 1.0);
    vec3 normal = normalize(vec3(-gradient / 24.0 * 3.0, 1.0));

    float cover;
    bool usesScene;
    vec3 col = shade(mode, uv, rim, normal, cover, usesScene);

    if (!usesScene) {
        float detail = dot(sceneAt(uv), vec3(0.299, 0.587, 0.114));
        col = mix(col, col * (0.25 + detail * 1.5), Params.w);
    }

    float alpha = clamp(Color1.a * cover, 0.0, 1.0);
    if (alpha < 0.003) discard;
    OutColor = vec4(finish(col), alpha);
}
