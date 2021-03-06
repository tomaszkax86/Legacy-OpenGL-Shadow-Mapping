package program;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.ARBDepthTexture.*;
import static org.lwjgl.opengl.ARBShadow.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import org.lwjgl.opengl.PixelFormat;
import static org.lwjgl.util.glu.GLU.*;
import program.geometry.Material;
import program.geometry.Model;
import program.geometry.Vertex;

public class Program implements Runnable
{
    // screen resolution
    private final int SCREEN_WIDTH = 800;
    private final int SCREEN_HEIGHT = 600;
    private final float ASPECT_RATIO = (float) SCREEN_WIDTH / (float) SCREEN_HEIGHT;

    private boolean exit = false;

    private boolean gl14 = false;
    private boolean ARB_shadow = false;
    private boolean ARB_depth_texture = false;

    // rendering state
    private final HashMap<String, Integer> textures = new HashMap<>();
    private final HashSet<Material> materials = new HashSet<>();
    private final FloatBuffer floats = BufferUtils.createFloatBuffer(4);
    private Model model;

    private int depthTexture;
    private int depthSize = 1;
    private final float depthVision = 10.0f;
    private final float polygonScale = 2.0f;
    private final float polygonOffset = 1.0f;

    // light parameters
    private boolean pointLight = true;
    private final float[] light = new float[]{ 1.0f, 5.0f, 1.0f, 1.0f };
    private float lightAngle = 0.0f;
    private float lightHeight = 5.0f;

    private final float[] lightPlaneX = new float[] { 1.0f, 0.0f, 0.0f, 0.0f };
    private final float[] lightPlaneY = new float[] { 0.0f, 1.0f, 0.0f, 0.0f };
    private final float[] lightPlaneZ = new float[] { 0.0f, 0.0f, 1.0f, 0.0f };
    private final float[] lightPlaneW = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };


    @Override
    public void run()
    {
        create();

        while(!exit)
        {
            update();
            render();
        }

        destroy();
    }

    private void create()
    {
        try
        {
            Display.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT));
            Display.create(new PixelFormat(8, 24, 8));      // important: 24-bit depth buffer
        }
        catch(LWJGLException ex)
        {
            throw new RuntimeException(ex);
        }

        // determine GL version and/or extensions
        String ver = glGetString(GL_VERSION);
        int index = ver.indexOf(' ');
        if(index != -1) ver = ver.substring(0, index);

        String[] parts = ver.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);

        if(major > 1 || minor > 3) gl14 = true;

        String extensions = glGetString(GL_EXTENSIONS);
        ARB_shadow = extensions.contains("ARB_shadow");
        ARB_depth_texture = extensions.contains("ARB_depth_texture");

        if(gl14)
        {
            System.out.println("OpenGL 1.4 or newer, shadowing available");
        }
        else if(ARB_shadow && ARB_depth_texture)
        {
            System.out.println("ARB_shadow + ARB_depth_texture, "
                    + "shadowing available");
        }
        else
        {
            System.out.println("No shadowing extensions available");
        }

        // loading model from .obj file
        model = Model.load(new File("Scene.obj"));
        int count = model.size();

        for (int i = 0; i < count; i += 3)
        {
            Material material = model.get(i).getMaterial();

            materials.add(material);
        }

        // find maximum available shadow texture size
        int len = Math.min(SCREEN_WIDTH, SCREEN_HEIGHT);

        for(int i=4; i<16; i++)
        {
            if(1 << i <= len)
                depthSize = 1 << i;
            else
                break;
        }

        // create shadow texture
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);

        // important: match depth texture precision with depth buffer precision!
        // OpenGL 1.4+
        if(gl14)
        {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24,
                depthSize, depthSize, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        }
        else if(ARB_depth_texture)
        {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24_ARB,
                depthSize, depthSize, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        if(gl14)
        {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_R_TO_TEXTURE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        }
        else if(ARB_shadow)
        {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE_ARB, GL_COMPARE_R_TO_TEXTURE_ARB);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC_ARB, GL_LEQUAL);
        }

        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void destroy()
    {
        glDeleteTextures(depthTexture);

        Display.destroy();
    }

    private void update()
    {
        // exit window button
        if(Display.isCloseRequested()) exit = true;

        // check keys
        if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) exit = true;
        if(Keyboard.isKeyDown(Keyboard.KEY_1)) pointLight = true;
        if(Keyboard.isKeyDown(Keyboard.KEY_2)) pointLight = false;
        if(Keyboard.isKeyDown(Keyboard.KEY_UP)) lightHeight += 0.1f;
        if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) lightHeight -= 0.1f;
        if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) lightAngle -= 0.02f;
        if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) lightAngle += 0.02f;

        // update light values
        if(pointLight)
        {
            light[0] = 5.0f * (float) Math.cos(lightAngle);
            light[1] = lightHeight;
            light[2] = 5.0f * (float) Math.sin(lightAngle);
            light[3] = 1.0f;
        }
        else
        {
            light[0] = 0.3f * (float) Math.cos(lightAngle);
            light[1] = 0.1f * lightHeight;
            light[2] = 0.3f * (float) Math.sin(lightAngle);
            light[3] = 0.0f;

            // normalize direction vector
            float len = 1.0f / (float) Math.sqrt(light[0] * light[0]
                    + light[1] * light[1] + light[2] * light[2]);

            light[0] *= len;
            light[1] *= len;
            light[2] *= len;
        }
    }

    private void render()
    {
        renderShadowMap();
        renderScene();
        renderLight();

        Display.sync(60);
        Display.update();
    }

    private void renderShadowMap()
    {
        // designate part of available screen for shadow map
        glViewport(0, 0, depthSize, depthSize);
        glColorMask(false, false, false, false);
        glClear(GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glDisable(GL_CULL_FACE);
        //glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT_FACE);

        if(pointLight)
        {
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(90.0f, 1.0f, 1.0f, 50.0f);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            gluLookAt(light[0], light[1], light[2],
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }
        else
        {
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(-depthVision, depthVision,
                    -depthVision, depthVision,
                    -depthVision, depthVision);

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
            gluLookAt(10 * light[0], 10 * light[1], 10 * light[2],
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }

        glDisable(GL_LIGHTING);

        // correct problem with shadow acne
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(polygonScale, polygonOffset);

        int count = model.size();

        glBegin(GL_TRIANGLES);

        for (int i = 0; i < count; i++)
        {
            Vertex vertex = model.get(i);

            glVertex3f(vertex.getX(), vertex.getY(), vertex.getZ());
        }

        glEnd();

        glDisable(GL_POLYGON_OFFSET_FILL);

        // copy portion of depth buffer to shadow texture
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glCopyTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 0, 0, depthSize, depthSize);
    }

    private void renderScene()
    {
        // render scene
        glColorMask(true, true, true, true);
        glViewport(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(60.0f, ASPECT_RATIO, 0.1f, 100.0f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        gluLookAt(10.0f, 10.0f, 10.0f, 0.0f, 3.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        // texture matrix set to transform texture coordinates to light space
        glActiveTexture(GL_TEXTURE1);
        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();
        glScalef(0.5f, 0.5f, 0.5f);
        glTranslatef(1.0f, 1.0f, 1.0f);

        if(pointLight)
        {
            gluPerspective(90.0f, 1.0f, 1.0f, 50.0f);
            gluLookAt(light[0], light[1], light[2],
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }
        else
        {
            glOrtho(-depthVision, depthVision,
                    -depthVision, depthVision,
                    -depthVision, depthVision);
            gluLookAt(10 * light[0], 10 * light[1], 10 * light[2],
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }

        // texture coordinate generation
	glEnable(GL_TEXTURE_GEN_S);
	glEnable(GL_TEXTURE_GEN_T);
	glEnable(GL_TEXTURE_GEN_R);
	glEnable(GL_TEXTURE_GEN_Q);

	glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
	glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);

        floats.clear();
        floats.put(lightPlaneX).flip();
	glTexGen(GL_S, GL_EYE_PLANE, floats);

        floats.clear();
        floats.put(lightPlaneY).flip();
	glTexGen(GL_T, GL_EYE_PLANE, floats);

        floats.clear();
        floats.put(lightPlaneZ).flip();
	glTexGen(GL_R, GL_EYE_PLANE, floats);

        floats.clear();
        floats.put(lightPlaneW).flip();
	glTexGen(GL_Q, GL_EYE_PLANE, floats);

        glColor3f(1, 1, 1);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glShadeModel(GL_SMOOTH);

        floats.clear();
        floats.put(light).flip();
        glLight(GL_LIGHT0, GL_POSITION, floats);

        glLightf(GL_LIGHT0, GL_CONSTANT_ATTENUATION, 0.5f);
        glLightf(GL_LIGHT0, GL_LINEAR_ATTENUATION, 0.2f);
        glLightf(GL_LIGHT0, GL_QUADRATIC_ATTENUATION, 0.01f);

        glEnable(GL_TEXTURE_2D);

        glActiveTexture(GL_TEXTURE0);

        int count = model.size();

        // iterate over all materials
        for (Material material : materials)
        {
            String filename = material.getFilename();

            // bind texture if material has one
            if (filename != null)
            {
                int texture = getTexture(filename);

                glEnable(GL_TEXTURE_2D);
                glBindTexture(GL_TEXTURE_2D, texture);
            }

            glBegin(GL_TRIANGLES);

            // iterate over all vertices with given material
            for (int i = 0; i < count; i += 3)
            {
                Vertex vertex = model.get(i);

                if (vertex.getMaterial() != material) continue;

                for (int j = 0; j < 3; j++)
                {
                    vertex = model.get(i + j);

                    glTexCoord2f(vertex.getU(), vertex.getV());
                    glNormal3f(vertex.getNX(), vertex.getNY(), vertex.getNZ());
                    glVertex3f(vertex.getX(), vertex.getY(), vertex.getZ());
                }
            }

            glEnd();

            // unbind and disable texture if necessary
            if (filename != null)
            {
                glBindTexture(GL_TEXTURE_2D, 0);
                glDisable(GL_TEXTURE_2D);
            }
        }

        // disable shadow mapping
        glActiveTexture(GL_TEXTURE1);
	glDisable(GL_TEXTURE_GEN_S);
	glDisable(GL_TEXTURE_GEN_T);
	glDisable(GL_TEXTURE_GEN_R);
	glDisable(GL_TEXTURE_GEN_Q);

        glDisable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        glMatrixMode(GL_TEXTURE);
        glLoadIdentity();

        glDisable(GL_LIGHTING);
        glDisable(GL_LIGHT0);
    }

    // renders small yellow point at light position if rendering point light
    private void renderLight()
    {
        if(!pointLight) return;

        glColor3f(1.0f, 1.0f, 0.0f);
        glPointSize(5.0f);

        glBegin(GL_POINTS);

            glVertex3f(light[0], light[1], light[2]);

        glEnd();
    }

    private int getTexture(String filename)
    {
        if (textures.containsKey(filename))
        {
            return textures.get(filename);
        }

        BufferedImage image = null;

        try
        {
            image = ImageIO.read(new File(filename));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }

        int width = image.getWidth();
        int height = image.getHeight();

        Raster raster = image.getData();
        ByteBuffer buffer = BufferUtils.createByteBuffer(4 * width * height);
        int[] color = new int[4];

        for (int y = height - 1; y >= 0; y--)
        {
            for (int x = 0; x < width; x++)
            {
                raster.getPixel(x, y, color);

                buffer.put((byte) color[0]);
                buffer.put((byte) color[1]);
                buffer.put((byte) color[2]);
                buffer.put((byte) 255);
            }
        }

        buffer.flip();

        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        textures.put(filename, texture);

        return texture;
    }
}
