package com.gofobao.framework.migrate;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;


public class FileHelper {
    /**
     * 创建文件
     *
     * @param pPath
     * @param sPath
     * @param fileName
     * @return
     */
    public static File createFile(String pPath, String sPath, String fileName) throws Exception {
        File dir = new File(pPath + "/" + sPath);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, fileName);
        if (file.exists()) {
            File backDir = new File(pPath + "/back/" + sPath);

            if (!backDir.exists()) {
                backDir.mkdirs();
            }

            File backFile = new File(backDir, fileName + System.currentTimeMillis());

            try {
                Files.move(file, backFile);
            } catch (IOException e) {
                throw new Exception(e);
            }

            file.delete();
            file = new File(dir, fileName);
        }

        return file;
    }
}
