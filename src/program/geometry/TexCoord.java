package program.geometry;

/**
 * This is immutable container for texture coordinates.
 * @author Tomasz Kapuściński
 */
public final class TexCoord
{
    private final float u, v;

    /**
     * Create new {@code TexCoord}.
     * @param u U coordinate
     * @param v V coordinate
     */
    public TexCoord(float u, float v)
    {
        this.u = u;
        this.v = v;
    }

    /**
     * Returns U coordinate.
     * @return U coordinate
     */
    public float getU()
    {
        return u;
    }

    /**
     * Returns V coordinate.
     * @return V coordinate
     */
    public float getV()
    {
        return v;
    }
}
