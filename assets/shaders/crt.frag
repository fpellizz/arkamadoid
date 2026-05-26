#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoord;
uniform sampler2D u_texture;
uniform vec2 u_resolution;

// stub CRT: scanlines + chromatic aberration leggera
// raffinare quando arrivano i mockup finali
void main() {
    vec2 uv = v_texCoord;

    // chromatic aberration
    float ca = 0.0015;
    vec3 col;
    col.r = texture2D(u_texture, uv + vec2(ca, 0.0)).r;
    col.g = texture2D(u_texture, uv).g;
    col.b = texture2D(u_texture, uv - vec2(ca, 0.0)).b;

    // scanlines
    float scan = 0.92 + 0.08 * sin(uv.y * u_resolution.y * 3.14159);
    col *= scan;

    gl_FragColor = vec4(col, 1.0);
}
