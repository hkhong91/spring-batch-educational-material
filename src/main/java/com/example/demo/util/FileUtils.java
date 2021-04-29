package com.example.demo.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.SneakyThrows;

public abstract class FileUtils {

  @SneakyThrows
  public static Stream<File> stream(Path path) {
    return Files.list(path).map(Path::toFile);
  }
}
