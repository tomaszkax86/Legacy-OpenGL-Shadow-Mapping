package program.geometry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is simple model implementation.
 * @author Tomasz Kapuściński
 */
public final class Model
{
    private final Vertex[] vertices;
    
    
    /**
     * Creates new model with given vertices.
     * @param vertices vertices to use for new model
     */
    public Model(ArrayList<Vertex> vertices)
    {
        this.vertices = vertices.toArray(new Vertex[vertices.size()]);
    }
    
    /**
     * Returns number of vertices in this model.
     * @return number of vertices in this model
     */
    public int size()
    {
        return vertices.length;
    }
    
    /**
     * Returns vertex under given index.
     * @param index index
     * @return vertex under given index
     */
    public Vertex get(int index)
    {
        return vertices[index];
    }
    
    /**
     * Stores all vertices into {@code ByteBuffer} object
     * @param buffer {@code ByteBuffer) object where to store vertices
     */
    public void store(ByteBuffer buffer)
    {
        for(Vertex vertex : vertices)
        {
            vertex.store(buffer);
        }
    }
    
    /**
     * Loads model from .obj file
     * @param file file to read
     * @return new model read from file
     */
    public static Model load(File file)
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            ArrayList<VertexCoord> vertexcoords = new ArrayList<>();
            ArrayList<Normal> normals = new ArrayList<>();
            ArrayList<TexCoord> texcoords = new ArrayList<>();
            
            ArrayList<Vertex> vertices = new ArrayList<>();
            
            HashMap<String, Material> materials = new HashMap<>();
            
            Material material = null;
            
            while(true)
            {
                String line = reader.readLine();
                if(line == null) break;
                
                String[] parts = line.split(" ");
                
                switch(parts[0])
                {
                    case "v":
                        float x = Float.parseFloat(parts[1]);
                        float y = Float.parseFloat(parts[2]);
                        float z = Float.parseFloat(parts[3]);
                        vertexcoords.add(new VertexCoord(x, y, z));
                        break;
                    case "vt":
                        float u = Float.parseFloat(parts[1]);
                        float v = Float.parseFloat(parts[2]);
                        texcoords.add(new TexCoord(u, v));
                        break;
                    case "vn":
                        float nx = Float.parseFloat(parts[1]);
                        float ny = Float.parseFloat(parts[2]);
                        float nz = Float.parseFloat(parts[3]);
                        normals.add(new Normal(nx, ny, nz));
                        break;
                    case "mtllib":
                        loadMaterials(materials, new File(parts[1]));
                        break;
                    case "usemtl":
                        material = materials.get(parts[1]);
                        break;
                    case "f":
                        Vertex[] verts = new Vertex[parts.length - 1];
                        
                        // attribute parsing
                        for(int i=0; i<verts.length; i++)
                        {
                            String[] temp = parts[i+1].split("/");
                            
                            int vc = Integer.parseInt(temp[0]) - 1;
                            int tc = Integer.parseInt(temp[1]) - 1;
                            int n = Integer.parseInt(temp[2]) - 1;
                            
                            verts[i] = new Vertex(vertexcoords.get(vc), texcoords.get(tc), normals.get(n), material);
                        }
                        
                        // triangulation
                        int count = verts.length - 2;
                        
                        for(int i=0; i<count; i++)
                        {
                            vertices.add(verts[0]);
                            vertices.add(verts[1+i]);
                            vertices.add(verts[2+i]);
                        }
                        
                        break;
                }
            }
            
            return new Model(vertices);
        }
        catch(IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    private static void loadMaterials(HashMap<String, Material> materials, File file)
    {
        try(BufferedReader reader = new BufferedReader(new FileReader(file)))
        {
            Material current = null;
            int id = 0;
            
            while(true)
            {
                String line = reader.readLine();
                if(line == null) break;
                
                String[] parts = line.split(" ");
                
                switch(parts[0])
                {
                    case "newmtl":
                        current = new Material(parts[1], id);
                        materials.put(parts[1], current);
                        id++;
                        break;
                    case "map_Kd":
                        current.setFilename(parts[1]);
                        break;
                }
            }
        }
        catch(IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
