package com.svedentsov.xaiobserverapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

public class JavaFileCombiner {

    /**
     * Объединяет содержимое всех .java файлов из указанной директории
     * и всех её поддиректорий в один выходной текстовый файл.
     *
     * @param projectRootDirPath Путь к корневой директории вашего Java проекта.
     * @param outputFilePath     Путь к выходному текстовому файлу, куда будет записано объединенное содержимое.
     * @throws IOException       Если произойдет ошибка ввода/вывода при чтении или записи файлов.
     */
    public void combineJavaFiles(String projectRootDirPath, String outputFilePath) throws IOException {
        File projectRootDir = new File(projectRootDirPath);
        File outputFile = new File(outputFilePath);

        // Проверяем, существует ли корневая директория проекта
        if (!projectRootDir.exists() || !projectRootDir.isDirectory()) {
            throw new IllegalArgumentException("Указанный путь к корневой директории проекта недействителен или не является директорией: " + projectRootDirPath);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            processDirectory(projectRootDir, writer);
            System.out.println("Содержимое всех Java файлов успешно объединено в: " + outputFilePath);
        }
    }

    /**
     * Рекурсивно обрабатывает директории и файлы.
     * Если это директория, то вызывает себя для неё.
     * Если это .java файл, то считывает его содержимое и записывает в BufferedWriter.
     *
     * @param directoryOrFile Текущая директория или файл для обработки.
     * @param writer          BufferedWriter для записи содержимого файлов.
     * @throws IOException    Если произойдет ошибка ввода/вывода.
     */
    private void processDirectory(File directoryOrFile, BufferedWriter writer) throws IOException {
        if (directoryOrFile.isDirectory()) {
            File[] files = directoryOrFile.listFiles();
            if (files != null) {
                for (File file : files) {
                    processDirectory(file, writer); // Рекурсивный вызов для поддиректорий и файлов
                }
            }
        } else if (directoryOrFile.isFile() && directoryOrFile.getName().toLowerCase().endsWith(".java")) {
            // Это Java файл, читаем его содержимое и записываем
            writer.write("// Содержимое файла: " + directoryOrFile.getName());
            writer.newLine();
            try (BufferedReader reader = new BufferedReader(new FileReader(directoryOrFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.newLine(); // Добавляем пустую строку для разделения файлов
            writer.newLine();
        }
    }

    public static void main(String[] args) {
        JavaFileCombiner combiner = new JavaFileCombiner();
        // Путь к корневой директории вашего проекта
        String projectRoot = "src/main";
        // Путь к выходному файлу
        String outputPath = "src/test/java/com/svedentsov/xaiobserverapp/combined_java_code.txt";
        try {
            combiner.combineJavaFiles(projectRoot, outputPath);
        } catch (IOException e) {
            System.err.println("Произошла ошибка при объединении файлов: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}
