package program.geometry;

/**
 * This class is a container for material properties.
 * Deliberately left simple.
 * @author Tomasz Kapuściński
 */
public final class Material
{
    private String name;
    private String filename;
    private int id;

    
    /**
     * Creates new material with given name and id.
     * @param name material name
     * @param id material id
     */
    public Material(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    /**
     * Returns material name
     * @return material name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Changes material name
     * @param name new material name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns texture filename
     * @return texture filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Changes texture filename
     * @param filename new texture filename
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Returns material id
     * @return material id
     */
    public int getID()
    {
        return id;
    }

    /**
     * Changes material id
     * @param id new material id
     */
    public void setID(int id)
    {
        this.id = id;
    }
}
