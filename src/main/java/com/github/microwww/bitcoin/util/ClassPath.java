package com.github.microwww.bitcoin.util;

import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ClassPath {
    private ClassPath() {
        throw new RuntimeException("Not create it");
    }

    public static File findFile(String path) {
        Assert.isTrue(path.startsWith("/"), "path start with / ");
        return new File(ClassPath.class.getResource(path).getFile());
    }

    public static File lookupFile(String path) {
        Assert.isTrue(path.startsWith("/"), "path start with / ");
        return new File(ClassPath.class.getResource("/").getFile(), path);
    }

    public static List<String> readClassPathFile(String path) {
        Assert.isTrue(path.startsWith("/"), "path start with / ");
        try {
            Path path1 = Paths.get(ClassPath.class.getResource(path).toURI());
            return Files.readAllLines(path1);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
