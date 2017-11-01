package com.gofobao.framework;

import sun.misc.Launcher;

import java.net.URL;

public class ClassLoaderTest {
    public static void main(String[] args) {
      /*  URL[] urLs = Launcher.getBootstrapClassPath().getURLs();
        for (int i = 0, len = urLs.length; i < len; i++) {
            System.err.println(urLs[i]);
        }*/

        ClassLoader classLoader = ClassLoaderTest.class.getClassLoader();
        while (classLoader != null){
            System.err.println(classLoader);
            classLoader = classLoader.getParent();
        }

    }
}
