package utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class CommonFileUtils {

    public static final String DIR = ".job";

    private static File userDir() {
        return FileUtil.file(System.getProperty("user.dir"));
    }

    public static File copyClassPathResource(String fileName, String name) {
        log.info("fileName: {}", fileName);
        return copyClassPathResource(fileName, name, true);
    }

    /**
     * Copy class path resource to home dir file.
     * class path resource in jar can't be read as file, so copy it to home dir.
     */
    public static File copyClassPathResource(String fileName, String name, boolean overwrite) {
        if (fileName == null) {
            throw new IllegalArgumentException("fileName is null");
        }
        if (!fileName.startsWith("classpath:")) {
            return FileUtil.file(fileName);
        }
        File dir = FileUtil.file(userDir(), DIR);
        if (FileUtil.exist(dir + File.separator + name) && !overwrite) {
            return FileUtil.file(dir, name);
        }
        File file = FileUtil.file(dir, name);
        FileUtil.writeBytes(new byte[0], file);
        file = FileUtil.file(file.getAbsolutePath());
        ClassPathResource pathResource = new ClassPathResource(fileName);
        if (pathResource.getStream() != null) {
            FileUtil.writeFromStream(pathResource.getStream(), file);
        }
        return file;
    }

    public static File getRunDirFile(String name) {
        File dir = FileUtil.file(userDir(), DIR);
        log.info("dir: {}", dir.getAbsolutePath());
        return FileUtil.file(dir, name);
    }
}
