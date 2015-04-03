package program.geometry;

import java.nio.ByteBuffer;

/**
 * This is simple matrix implementation.
 * Contains various methods for creating matrices for OpenGL usage.
 * @author Tomasz Kapuściński
 */
public final class Matrix
{
    private final int rows, columns;
    private float[][] values;
    private Matrix aux = null;      // auxiliary matrix to speed up some operations
    
    
    /**
     * Creates new square matrix.
     * @param size number of rows and columns
     */
    public Matrix(int size)
    {
        this(size, size);
    }
    
    /**
     * Creates new matrix.
     * @param rows number of rows
     * @param columns number of columns
     */
    public Matrix(int rows, int columns)
    {
        this.rows = rows;
        this.columns = columns;
        this.values = new float[rows][columns];
    }
    
    /**
     * Returns number of rows.
     * @return number of rows
     */
    public int getRows()
    {
        return rows;
    }
    
    /**
     * Returns number of columns.
     * @return number of columns
     */
    public int getColumns()
    {
        return columns;
    }
    
    /**
     * Returns a value under given row and column.
     * @param row row index
     * @param column column index
     * @return value
     */
    public float get(int row, int column)
    {
        return values[row][column];
    }
    
    /**
     * Changes value under given row and column
     * @param row row index
     * @param column column index
     * @param value new value
     */
    public void set(int row, int column, float value)
    {
        values[row][column] = value;
    }
    
    @Override
    public Matrix clone()
    {
        try
        {
            return (Matrix) super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return null; // should never happen
        }
    }
    
    /**
     * Loads this matrix with identity values.
     * @return this matrix
     */
    public Matrix loadIdentity()
    {
        loadIdentity(this);
        
        return this;
    }
    
    /**
     * Transforms this matrix by other matrix.
     * @param transform transformation matrix
     * @return this matrix
     */
    public Matrix transform(Matrix transform)
    {
        requestAux();
        
        multiply(this, transform, aux);
        swap(this, aux);
        
        return this;
    }
    
    /**
     * Loads this matrix from {@code ByteBuffer} object.
     * @param buffer {@code ByteBuffer} to load matrix from
     */
    public void load(ByteBuffer buffer)
    {
        load(this, buffer);
    }
    
    /**
     * Stores this matrix to {@code ByteBuffer} object.
     * @param buffer {@code ByteBuffer} to store matrix to
     */
    public void store(ByteBuffer buffer)
    {
        store(this, buffer);
    }
    
    /**
     * Loads this matrix with translation values.
     * @param dx X translation
     * @param dy Y translation
     * @param dz Z translation
     */
    public void loadTranslation(float dx, float dy, float dz)
    {
        loadTranslation(this, dx, dy, dz);
    }
    
    /**
     * Loads this matrix with rotation around X axis.
     * @param angle angle in degrees
     */
    public void loadRotationX(float angle)
    {
        loadRotationX(this, angle);
    }
    
    /**
     * Loads this matrix with rotation around Y axis.
     * @param angle angle in degrees
     */
    public void loadRotationY(float angle)
    {
        loadRotationY(this, angle);
    }
    
    /**
     * Loads this matrix with rotation around Z axis.
     * @param angle angle in degrees
     */
    public void loadRotationZ(float angle)
    {
        loadRotationZ(this, angle);
    }
    
    /**
     * Loads this matrix with scale values.
     * @param sx X scale
     * @param sy Y scale
     * @param sz Z scale
     */
    public void loadScale(float sx, float sy, float sz)
    {
        loadScale(this, sx, sy, sz);
    }
    
    /**
     * Loads this matrix with perspective projection values.
     * @param fov field of view in degrees
     * @param aspect aspect ratio (width divided by height)
     * @param near near plane
     * @param far far plane
     */
    public void loadPerspective(float fov, float aspect, float near, float far)
    {
        loadPerspective(this, fov, aspect, near, far);
    }
    
    /**
     * Loads this matrix with orthographic projection values.
     * @param left left plane
     * @param right right plane
     * @param bottom bottom plane
     * @param top top plane
     * @param near near plane
     * @param far far plane
     */
    public void loadOrtho(float left, float right, float bottom, float top, float near, float far)
    {
        loadOrtho(this, left, right, bottom, top, near, far);
    }
    
    /**
     * Loads this matrix with 2D orthographics projection values;
     * @param left left plane
     * @param right right plane
     * @param bottom bottom plane
     * @param top top plane
     */
    public void loadOrtho2D(float left, float right, float bottom, float top)
    {
        loadOrtho2D(this, left, right, bottom, top);
    }
    
    /**
     * Loads this matrix with camera view transformation.
     * @param x X camera coordinate
     * @param y Y camera coordinate
     * @param z Z camera coordinate
     * @param pitch angle of rotation around X axis in degrees
     * @param yaw angle of rotation around Y axis in degrees
     * @param roll angle of rotation around Z axis in degrees
     */
    public void loadCameraView(float x, float y, float z, float pitch, float yaw, float roll)
    {
        loadCameraView(this, x, y, z, pitch, yaw, roll);
    }
    
    /**
     * Inverses this matrix.
     */
    public void inverse()
    {
        if(rows != columns)
            throw new IllegalStateException("Cannot invert non-square matrix");
        
        requestAux();
        
        inverse(this, aux);
        swap(this, aux);
    }
    
    /**
     * Transposes this matrix.
     */
    public void transpose()
    {
        requestAux();
        
        transpose(this, aux);
        swap(this, aux);
    }
    
    // requests auxiliary matrix
    private void requestAux()
    {
        if(aux == null) aux = new Matrix(rows, columns);
    }
    
    
    /**
     * Loads matrix from {@code ByteBuffer} object.
     * @param matrix matrix to be loaded with values
     * @param buffer {@code ByteBuffer} to load matrix from
     */
    public static void load(Matrix matrix, ByteBuffer buffer)
    {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();
        int index = 0;
        
        for(int column=0; column<columns; column++)
        {
            for(int row=0; row<rows; row++)
            {
                matrix.values[row][column] = buffer.getFloat(index);
                index += 4;
            }
        }
    }
    
    /**
     * Stores matrix to {@code ByteBuffer} object.
     * @param matrix matrix with values to be stored
     * @param buffer {@code ByteBuffer} to store matrix to
     */
    public static void store(Matrix matrix, ByteBuffer buffer)
    {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();
        
        buffer.clear();
        
        for(int column=0; column<columns; column++)
        {
            for(int row=0; row<rows; row++)
            {
                buffer.putFloat(matrix.values[row][column]);
            }
        }
        
        buffer.flip();
    }
    
    /**
     * Loads matrix with identity values.
     * @param matrix matrix to be loaded with values
     */
    public static void loadIdentity(Matrix matrix)
    {
        int rows = matrix.getRows();
        int columns = matrix.getColumns();
        
        for(int row=0; row<rows; row++)
        {
            for(int column=0; column<columns; column++)
            {
                matrix.values[row][column] = (row == column ? 1.0f : 0.0f);
            }
        }
    }
    
    public static void loadTranslation(Matrix matrix, float dx, float dy, float dz)
    {
        loadIdentity(matrix);
        
        matrix.values[0][3] += dx;
        matrix.values[1][3] += dy;
        matrix.values[2][3] += dz;
    }
    
    /**
     * Loads matrix with scale values.
     * @param matrix matrix to be loaded with values
     * @param sx X scale
     * @param sy Y scale
     * @param sz Z scale
     */
    public static void loadScale(Matrix matrix, float sx, float sy, float sz)
    {
        loadIdentity(matrix);
        
        matrix.values[0][0] = sx;
        matrix.values[1][1] = sy;
        matrix.values[2][2] = sz;
    }
    
    /**
     * Loads matrix with perspective projection values.
     * @param matrix matrix to be loaded with values
     * @param fov field of view in degrees
     * @param aspect aspect ratio (width divided by height)
     * @param near near plane
     * @param far far plane
     */
    public static void loadPerspective(Matrix matrix, float fov, float aspect, float near, float far)
    {
        loadIdentity(matrix);
        
        float focal = (float)(1.0f / Math.tan(0.5f * Math.toRadians(fov)));
        
        matrix.values[0][0] = focal / aspect;
        matrix.values[1][1] = focal;
        matrix.values[2][2] = -(far + near) / (far - near);
        matrix.values[2][3] = -2.0f * far * near / (far - near);
        matrix.values[3][2] = -1.0f;
        matrix.values[3][3] = 0.0f;
    }
    
    /**
     * Loads matrix with orthographic projection values.
     * @param matrix matrix to be loaded with values
     * @param left left plane
     * @param right right plane
     * @param bottom bottom plane
     * @param top top plane
     * @param near near plane
     * @param far far plane
     */
    public static void loadOrtho(Matrix matrix, float left, float right, float bottom, float top, float near, float far)
    {
        loadIdentity(matrix);
        
        matrix.values[0][0] = 2.0f / (right - left);
        matrix.values[1][1] = 2.0f / (top - bottom);
        matrix.values[2][2] = -2.0f / (far - near);
        
        matrix.values[0][3] = -(right + left) / (right - left);
        matrix.values[1][3] = -(top + bottom) / (top - bottom);
        matrix.values[2][3] = -(far + near) / (far - near);
    }
    
    /**
     * Loads matrix with 2D orthographics projection values;
     * @param matrix matrix to be loaded with values
     * @param left left plane
     * @param right right plane
     * @param bottom bottom plane
     * @param top top plane
     */
    public static void loadOrtho2D(Matrix matrix, float left, float right, float bottom, float top)
    {
        loadOrtho(matrix, left, right, bottom, top, -1.0f, 1.0f);
    }
    
    /**
     * Loads matrix with camera view transformation.
     * @param matrix matrix to be loaded with values
     * @param x X camera coordinate
     * @param y Y camera coordinate
     * @param z Z camera coordinate
     * @param pitch angle of rotation around X axis in degrees
     * @param yaw angle of rotation around Y axis in degrees
     * @param roll angle of rotation around Z axis in degrees
     */
    public static void loadCameraView(Matrix matrix, float x, float y, float z,
            float pitch, float yaw, float roll)
    {
        Matrix transformation = new Matrix(4);
        
        matrix.loadIdentity();
        
        transformation.loadRotationZ(roll);
        matrix.transform(transformation);
        
        transformation.loadRotationX(pitch);
        matrix.transform(transformation);
        
        transformation.loadRotationY(yaw);
        matrix.transform(transformation);
        
        transformation.loadTranslation(-x, -y, -z);
        matrix.transform(transformation);
    }
    
    /**
     * Loads rotation matrix around X axis.
     * @param matrix matrix to be loaded with rotation values
     * @param angle rotation angle in degrees
     */
    public static void loadRotationX(Matrix matrix, float angle)
    {
        double radians = Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        
        loadIdentity(matrix);
        
        matrix.values[1][1] = cos;
        matrix.values[1][2] = -sin;
        matrix.values[2][1] = sin;
        matrix.values[2][2] = cos;
    }
    
    /**
     * Loads rotation matrix around Y axis.
     * @param matrix matrix to be loaded with rotation values
     * @param angle rotation angle in degrees
     */
    public static void loadRotationY(Matrix matrix, float angle)
    {
        double radians = Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        
        loadIdentity(matrix);
        
        matrix.values[0][0] = cos;
        matrix.values[0][2] = sin;
        matrix.values[2][0] = -sin;
        matrix.values[2][2] = cos;
    }
    
    /**
     * Loads rotation matrix around Z axis.
     * @param matrix matrix to be loaded with rotation values
     * @param angle rotation angle in degrees
     */
    public static void loadRotationZ(Matrix matrix, float angle)
    {
        double radians = Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        
        loadIdentity(matrix);
        
        matrix.values[0][0] = cos;
        matrix.values[0][1] = -sin;
        matrix.values[1][0] = sin;
        matrix.values[1][1] = cos;
    }
    
    /**
     * Adds two matrices and stores result in third one.
     * @param first first matrix to add
     * @param second second matrix to add
     * @param result matrix for result
     */
    public static void add(Matrix first, Matrix second, Matrix result)
    {
        checkCompatibility(first, second);
        checkCompatibility(first, result);
        
        int rows = first.getRows();
        int columns = first.getColumns();
        
        for(int row=0; row<rows; row++)
        {
            for(int column=0; column<columns; column++)
            {
                float value = first.values[row][column] + second.values[row][column];
                result.values[row][column] = value;
            }
        }
    }
    
    /**
     * Multiplies two matrices and stores result in other matrix.
     * @param first first matrix to multiply
     * @param second second matrix to multiply
     * @param result matrix where multiplication result is to be stored
     */
    public static void multiply(Matrix first, Matrix second, Matrix result)
    {
        if(first.getColumns() != result.getColumns())
            throw new IllegalArgumentException("Incompatible matrices");
        
        if(first.getColumns() != second.getRows())
            throw new IllegalArgumentException("Incompatible matrices");
        
        int rows = first.getRows();
        int columns = second.getColumns();
        int depth = first.getColumns();
        
        for(int row=0; row<rows; row++)
        {
            for(int column=0; column<columns; column++)
            {
                float value = 0.0f;
                
                for(int k=0; k<depth; k++)
                    value += first.values[row][k] * second.values[k][column];
                
                result.values[row][column] = value;
            }
        }
    }
    
    /**
     * Copies one matrix to another.
     * @param src source matrix
     * @param dest destination matrix
     */
    public static void copy(Matrix src, Matrix dest)
    {
        checkCompatibility(src, dest);
        
        int rows = src.getRows();
        int columns = src.getColumns();
        
        for(int row=0; row<rows; row++)
        {
            System.arraycopy(src.values[row], 0, dest.values[row], 0, columns);
        }
    }
    
    /**
     * Copies available region from one matrix to another.
     * @param src source matrix
     * @param dest destination matrix
     */
    public static void subcopy(Matrix src, Matrix dest)
    {
        if(dest.rows > src.rows) throw new IllegalArgumentException("Incompatible matrices");
        if(dest.columns > src.columns) throw new IllegalArgumentException("Incompatible matrices");
        
        int rows = src.getRows();
        int columns = src.getColumns();
        
        for(int row=0; row<rows; row++)
        {
            System.arraycopy(src.values[row], 0, dest.values[row], 0, columns);
        }
    }
    
    /**
     * Swaps content of two matrices.
     * @param first first matrix
     * @param second second matrix
     */
    public static void swap(Matrix first, Matrix second)
    {
        checkCompatibility(first, second);
        
        float[][] temp = first.values;
        first.values = second.values;
        second.values = temp;
    }
    
    /**
     * Computes transpose of given matrix and stores it in other matrix.
     * @param src source matrix
     * @param dest destination matrix
     */
    public static void transpose(Matrix src, Matrix dest)
    {
        if(src.rows != dest.columns) throw new IllegalArgumentException("Incompatible matrices");
        if(src.columns != dest.rows) throw new IllegalArgumentException("Incompatible matrices");
        
        int rows = src.rows;
        int columns = src.columns;
        
        for(int row=0; row<rows; row++)
        {
            for(int column=0; column<columns; column++)
            {
                dest.values[column][row] = src.values[row][column];
            }
        }
    }
    
    /**
     * Computes inverse of given matrix and stores it in other matrix.
     * @param src source matrix
     * @param dest destination matrix
     */
    public static void inverse(Matrix src, Matrix dest)
    {
        checkCompatibility(src, dest);
        
        if(src.rows == 3) inverse3(src, dest);
        else throw new UnsupportedOperationException("Unsupported matrix dimensions");
    }
    
    // computes inverse of 3x3 matrix
    private static void inverse3(Matrix src, Matrix dest)
    {
        // shortcut for source values
        final float m[][] = src.values;
        
        // determinant
        float det = m[0][0] * m[1][1] * m[2][2]
                + m[1][0] * m[2][1] * m[0][2]
                + m[2][0] * m[0][1] * m[1][2]
                - m[0][0] * m[2][1] * m[1][2]
                - m[1][0] * m[0][1] * m[2][2]
                - m[2][0] * m[1][1] * m[0][2];
        
        if(Math.abs(det) < 1e-6f)
            throw new IllegalArgumentException("Matrix cannot be inverted: determinant is 0");
        
        float detInv = 1.0f / det;
        
        float m00 =  (m[1][1] * m[2][2] - m[2][1] * m[1][2]);
        float m01 = -(m[1][0] * m[2][2] - m[2][0] * m[1][2]);
        float m02 =  (m[1][0] * m[2][1] - m[2][0] * m[1][1]);
        float m10 = -(m[0][1] * m[2][2] - m[2][1] * m[0][2]);
        float m11 =  (m[0][0] * m[2][2] - m[2][0] * m[0][2]);
        float m12 = -(m[0][0] * m[2][1] - m[2][0] * m[0][1]);
        float m20 =  (m[0][1] * m[1][2] - m[1][1] * m[0][2]);
        float m21 = -(m[0][0] * m[1][2] - m[1][0] * m[0][2]);
        float m22 =  (m[0][0] * m[1][1] - m[1][0] * m[0][1]);
        
        dest.values[0][0] = m00 * detInv;
        dest.values[0][1] = m01 * detInv;
        dest.values[0][2] = m02 * detInv;
        dest.values[1][0] = m10 * detInv;
        dest.values[1][1] = m11 * detInv;
        dest.values[1][2] = m12 * detInv;
        dest.values[2][0] = m20 * detInv;
        dest.values[2][1] = m21 * detInv;
        dest.values[2][2] = m22 * detInv;
    }
    
    /**
     * Checks matrix compatibility. Throws {@code IllegalArgumentException}
     * if matrices don't have equal number of rows and columns.
     * @param first first matrix to check
     * @param second second matrix to check
     */
    public static void checkCompatibility(Matrix first, Matrix second)
    {
        if(first.getRows() != second.getRows())
            throw new IllegalArgumentException("Incompatible matrices: different row count");
        
        if(first.getColumns() != second.getColumns())
            throw new IllegalArgumentException("Incompatible matrices: different column count");
    }
}
