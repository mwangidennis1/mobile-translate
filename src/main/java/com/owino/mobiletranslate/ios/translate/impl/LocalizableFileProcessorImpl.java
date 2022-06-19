package com.owino.mobiletranslate.ios.translate.impl;

import com.owino.mobiletranslate.common.RunnerInputReader;
import com.owino.mobiletranslate.googletranslate.GoogleTranslator;
import com.owino.mobiletranslate.ios.model.LocalizableTable;
import com.owino.mobiletranslate.ios.translate.LocalizableFileProcessor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LocalizableFileProcessorImpl implements LocalizableFileProcessor {

    public static final Logger LOGGER = Logger.getLogger(LocalizableFileProcessorImpl.class.getSimpleName());
    private GoogleTranslator googleTranslator;

    public LocalizableFileProcessorImpl() {
        this.googleTranslator = new GoogleTranslator();
    }

    @Override
    public File getRootLocalizableFile() {
        return RunnerInputReader.requestRootLocalizableFile();
    }

    @Override
    public LocalizableTable getLocalizableTableFromString(String textLineOfLocalizable) {
        LOGGER.info("Obtaining localizable table from a line of localizable text\n==>" + textLineOfLocalizable);
        var components = textLineOfLocalizable.split("[=]");
        var updatedComponents = Arrays.stream(components)
                .map(component -> component.replace('"', ' '))
                .map(e -> e.replace(" ", ""))
                .collect(Collectors.toList());

        updatedComponents.forEach(System.out::println);

        return new LocalizableTable(updatedComponents.get(0), updatedComponents.get(1));
    }

    @Override
    public List<LocalizableTable> extractLocalizableTableFromFile(File localizableFile) {
        try {
            return Files.lines(localizableFile.toPath())
                    .map(this::getLocalizableTableFromString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.severe(String.format(Locale.getDefault(), "%s \n%s \n%s \n%s",
                    "Failed to read localizable file content",
                    localizableFile.getAbsolutePath(),
                    "Cause:",
                    e.getMessage()));
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LocalizableTable> translateLocalizableTable(List<LocalizableTable> originalLocalizableValues, String locale) {
        List<LocalizableTable> translatedLocalizableTable = new ArrayList<>();
        for (LocalizableTable unTranslatedTable : originalLocalizableValues) {
            var translatedResource = googleTranslator.getTranslatedText(unTranslatedTable.getTranslatableResource(), locale);
            unTranslatedTable.setTranslatableResource(translatedResource);
            translatedLocalizableTable.add(unTranslatedTable);
        }
        return translatedLocalizableTable;
    }

    @Override
    public void placeTranslatedTextInDestinationDir(List<LocalizableTable> translatedLocalizable, String locale) throws IOException {
        LOGGER.info("Writing translated content to file| estimated size " + translatedLocalizable.size());
        var destinationFile = generateLocalizableDestinationFile(locale);
        if (!Files.exists(destinationFile.toPath())) throw new AssertionError("Invalid destination file " + destinationFile);
        for (LocalizableTable localizableTable : translatedLocalizable) {
            var writer = new BufferedWriter(new FileWriter(destinationFile, true));
            writer.append("\n".concat(localizableTable.getKey()).concat( " = ").concat(localizableTable.getTranslatableResource()));
            writer.close();
        }
    }

    @Override
    public File generateLocalizableDestinationFile(String localeSymbol) {
        return new File(localeSymbol.concat(".lproj").concat("/").concat("Localizable.strings"));
    }
}
