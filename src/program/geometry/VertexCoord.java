package program.geometry;

/**
 * This is immutable container for vertex coordinates.
 * @author Tomasz Kapuściński
 */
public final class VertexCoord
{
    private final float x, y, z;

    /**
     * Creates new {@code VertexCoord} object with 2D coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     */
    public VertexCoord(float x, float y)
    {
        this(x, y, 0.0f);
    }

    /**
     * Creates new {@code VertexCoord} object with 3D coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public VertexCoord(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns X coordinate.
     * @return X coordinate
     */
    public float getX()
    {
        return x;
    }

    /**
     * Returns Y coordinate.
     * @return Y coordinate
     */
    public float getY()
    {
        return y;
    }

    /**
     * Returns Z coordinate.
     * @return Z coordinate
     */
    public float getZ()
    {
        return z;
    }
}
