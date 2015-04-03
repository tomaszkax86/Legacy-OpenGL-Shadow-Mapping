package program.geometry;

/**
 * This is immutable container for vertex normal.
 * @author Tomasz Kapuściński
 */
public final class Normal
{
    private final float x, y, z;

    /**
     * Creates new normal.
     * @param x X direction
     * @param y Y direction
     * @param z Z direction
     */
    public Normal(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns X direction.
     * @return X direction
     */
    public float getX()
    {
        return x;
    }

    /**
     * Returns Y direction.
     * @return Y direction
     */
    public float getY()
    {
        return y;
    }

    /**
     * Returns Z direction.
     * @return Z direction
     */
    public float getZ()
    {
        return z;
    }
}
