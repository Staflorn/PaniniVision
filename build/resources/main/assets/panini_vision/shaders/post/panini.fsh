#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform PaniniConfig {
    vec4 Params;
    vec4 View;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    float paniniD = clamp(Params.x, 0.0, 1.0);
    vec2 sourceExtent = max(Params.yz, vec2(0.0001));

    // Scale the Panini lens from the actual perspective projection so the
    // horizontal FOV remains stable across FOV effects and aspect ratios.
    float cosLonEdge = inversesqrt(1.0 + sourceExtent.x * sourceExtent.x);
    float edgeScale = (paniniD + 1.0) / (paniniD + cosLonEdge);
    float fitScale = max(edgeScale * cosLonEdge, 0.0001);

    vec2 ndc = texCoord * 2.0 - 1.0;
    float cameraZoom = clamp(View.x, 0.5, 1.0);
    vec2 paniniPlane = ndc * sourceExtent * fitScale * cameraZoom;

    float dPlusOne = paniniD + 1.0;
    float normalizedPaniniX = paniniPlane.x / dPlusOne;
    float normalizedPaniniX2 = normalizedPaniniX * normalizedPaniniX;
    float discriminant = 1.0 + normalizedPaniniX2 * (1.0 - paniniD * paniniD);
    float cosLon = (-normalizedPaniniX2 * paniniD + sqrt(max(0.0, discriminant))) / (normalizedPaniniX2 + 1.0);
    float paniniScale = dPlusOne / (paniniD + cosLon);
    float horizontalDivisor = max(paniniScale * cosLon, 0.0001);
    float verticalCompression = clamp(Params.w, 0.0, 1.0);
    float verticalDivisor = max(paniniScale * mix(cosLon, 1.0, verticalCompression), 0.0001);

    vec2 sourceNdc = vec2(
        paniniPlane.x / (horizontalDivisor * sourceExtent.x),
        paniniPlane.y / (verticalDivisor * sourceExtent.y)
    );
    vec2 sourceUv = clamp(sourceNdc * 0.5 + 0.5, 0.0, 1.0);

    fragColor = texture(InSampler, sourceUv);
}
