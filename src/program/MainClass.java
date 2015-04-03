package program;

import java.io.File;
import org.lwjgl.LWJGLUtil;

public class MainClass
{
    public static void main(String[] args)
    {
        String path = System.getProperty("user.dir")
                + File.separator
                + "native"
                + File.separator
                + LWJGLUtil.getPlatformName();
        
        System.setProperty("org.lwjgl.librarypath", path);
        
        new Program().run();
    }
}
