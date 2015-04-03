package program.geometry;

import java.nio.ByteBuffer;

/**
 * This is immutable vertex attributes container.
 * @author Tomasz Kapuściński
 */
public final class Vertex
{
    private final float x, y, z;
    private final float nx, ny, nz;
    private final float u, v;
    private final Material material;


    public Vertex(VertexCoord vc, TexCoord tc, Normal n, Material material)
    {
        if(vc == null) throw new NullPointerException();

        this.x = vc.getX();
        this.y = vc.getY();
        this.z = vc.getZ();

        if(tc != null)
        {
            this.u = tc.getU();
            this.v = tc.getV();
        }
        else
        {
            this.u = 0.0f;
            this.v = 0.0f;
        }

        if(n != null)
        {
            this.nx = n.getX();
            this.ny = n.getY();
            this.nz = n.getZ();
        }
        else
        {
            this.nx = 0.0f;
            this.ny = 1.0f;
            this.nz = 0.0f;
        }

        this.material = material;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getZ()
    {
        return z;
    }

    public float getNX()
    {
        return nx;
    }

    public float getNY()
    {
        return ny;
    }

    public float getNZ()
    {
        return nz;
    }

    public float getU()
    {
        return u;
    }

    public float getV()
    {
        return v;
    }

    public Material getMaterial()
    {
        return material;
    }
    
    public int getMaterialID()
    {
        return material.getID();
    }

    public void store(ByteBuffer buffer)
    {
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        buffer.putFloat(u);
        buffer.putFloat(v);
        buffer.putFloat(nx);
        buffer.putFloat(ny);
        buffer.putFloat(nz);
        buffer.putInt(material.getID());
    }
}
