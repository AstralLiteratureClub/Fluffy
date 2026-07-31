package bet.astral.fluffy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Objects;
import java.util.stream.Stream;

public final class Resource {
    /**
     * Loads the file as an file resource from the plugin jar
     *
     * @param fileName file name
     * @return file
     */
    private static @Nullable File loadResourceAsFile(String fileName) {
        InputStream inputStream = Resource.class.getResourceAsStream(fileName);
        if (inputStream == null) {
            return null;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Resource.class.getResourceAsStream(fileName))));
        String[] split = fileName.split("\\.");
        try {
            File file = File.createTempFile(fileName, split[split.length - 1]);
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
            reader.lines().forEachOrdered(line -> {
                try {
                    fileWriter.write(line);
                    fileWriter.newLine();
                    fileWriter.flush();
                } catch (IOException e) {
                    try {
                        fileWriter.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
            });
            fileWriter.close();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
                reader.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Loads the file as a temporary file from the jare
     *
     * @param fileName file name
     * @param end      file type
     * @return file as temporary file
     */
    public static @NotNull File loadResourceAsTemp(String fileName, String end) {
        try {
            try {
                File file = loadResourceAsFile(fileName + "." + end);
                File temp = File.createTempFile(fileName, end);
                return writeFile(file, temp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not find file for id: " + fileName + "." + end);
        }
    }

    /**
     * Loads the file as a temporary file from the jare
     *
     * @param fileName file name
     * @return file as temporary file
     */
    public static @NotNull File loadResourceAsTemp(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        String suffix = fileName.substring(lastDot + 1);
        String prefix = fileName.substring(0, lastDot);
        return loadResourceAsTemp(prefix, suffix);
    }

    /**
     * Writes the temporary file data to the existing file
     *
     * @param file file
     * @param temp temp file
     * @return file written
     * @throws IOException if exception occurs during write up
     */
    @NotNull
    private static File writeFile(File file, File temp) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(temp));
        Stream<String> lines = fileReader.lines();
        lines.forEachOrdered(line -> {
            try {
                fileWriter.write(line);
                fileWriter.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        fileWriter.flush();

        fileReader.close();
        fileWriter.close();

        return temp;
    }

    /**
     * Loads the resource from the file to the file created
     *
     * @param fileName file name
     * @param end      file ending
     * @param file     file
     * @param exists   if the file exists return
     * @return file
     */
    public static @NotNull File loadResourceToFile(String fileName, String end, @NotNull File file, boolean exists) {
        if (file.exists() && exists) {
            return file;
        } else if (!file.exists()) {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't create new file for: " + file.getName(), e);
            }
        }
        try {
            File fileTemp = loadResourceAsFile(fileName + "." + end);
            if (fileTemp == null) {
                throw new RuntimeException("Could not find file for id: " + file);
            }
            return writeFile(fileTemp, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the resource from the file to the file created
     *
     * @param fileName file name
     * @param file     file
     * @param exists   if the file exists return
     * @return file
     */
    public static @NotNull File loadResourceToFile(@NotNull String fileName, @NotNull File file, boolean exists) {
        int lastDot = fileName.lastIndexOf('.');
        String suffix = fileName.substring(lastDot + 1);
        String prefix = fileName.substring(0, lastDot);
        return loadResourceToFile(prefix, suffix, file, exists);
    }
}