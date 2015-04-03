# Legacy OpenGL Shadow Mapping
This program shows the usage of shadow mapping in pre-shader OpenGL.

Program uses either features from OpenGL 1.4 or ARB_shadow and
ARB_depth_texture extensions.


Control keys
------------

Key           | Description
--------------|-----------------------------
Escape        | Exits the program
1             | Changes light to point light
2             | Changes light to directional light
Up Arrow      | Increases height/direction of light
Down Arrow    | Decreases height/direction of light
Left Arrow    | Rotates light to left
Right Arrow   | Rotates light to right


Shadow texture
--------------

Shadow texture is a specially created OpenGL texture. First, it uses
depth image format. This is a feature introduced in OpenGL 1.4
but is also available in ARB_depth_texture extension. In our case, it's
GL_DEPTH_COMPONENT24, which matches depth buffer setting in framebuffer.
Second, it has enabled shadow compare mode. Shadow compare mode is also
available since OpenGL 1.4, but also in ARB_shadow extension. It is a set
of two texture parameters. GL_TEXTURE_COMPARE_MODE is changed to
GL_COMPARE_R_TO_TEXTURE, and GL_TEXTURE_COMPARE_FUNC is changed to
GL_LEQUAL. ARB extension constants have _ARB appended to them.
When texture is sampled, it's value is compared to R texture coordinate
(third one in STRQ). If current depth is less than value in shadow texture,
resulting color is white (1,1,1,1). In other case, resulting color is
black (0,0,0,1).


Rendering
---------

In first stage, shadow map is created by rendering scene from the position
of light. Because legacy OpenGL doesn't allow non power of two textures,
viewport is set to highest available square texture. In most resolutions
it will be 512x512. At the end, depth buffer is copied to depth texture
using glCopyTexSubImage2D(). It is important to note that both depth buffer
and depth texture should use the same precision (in this program: 24 bits)
because otherwise conversion will be performed and performance will degrade
considerably. In most games, you might want to limit number of objects that
cast shadow, usually all dynamic entities within range. In many cases, world
(terrain) itself doesn't need to be rendered.

In second stage, scene is rendered as normal using properly setup modelview
and projection matrices. Shadow texture is setup to use texture coordinate
generation in eye space. Also, texture matrix is setup to project
coordinates just like light was projected. There's a small change though.
World was generated with coordinates (-1,1), but texture coordinates use
range (0,1). For this reason small bias is added, basically doing this:
coordinates' = 0.5f * (coordinates - 1.0f). Comparison mode ensures that
actual depth is compared with the one in shadow map, giving us shadow effect.

Shadowing can be easily disabled by not doing first stage and not
setting up texture unit to use shadowing. Shadows can have different
quality set by changing resolution of shadow texture.
