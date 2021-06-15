//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) emilyy-dev
// Copyright (c) contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//

package io.github.emilyydev.betterjails.common.message;

import io.github.emilyydev.betterjails.common.plugin.BetterJailsPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

public class TranslationManager {

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final Key TRANSLATIONS_KEY = Key.key("betterjails", "translations");
  private static final Pattern MESSAGES_FILE_PATTERN = Pattern.compile("messages_([a-zA-Z_]+)\\.properties");

  private final BetterJailsPlugin plugin;
  private final Path translationsFolder;
  private TranslationRegistry registry = null;

  public TranslationManager(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.translationsFolder = this.plugin.getPluginFolder().resolve("translations");
  }

  public void loadTranslations() throws IOException {
    Files.createDirectories(this.translationsFolder);
    extractTranslationFiles();
    registerTranslationFiles();
  }

  public void reloadTranslations() throws IOException {
    registerTranslationFiles();
  }

  public void reinstallTranslations() throws IOException {
    try (final Stream<Path> files = Files.list(this.translationsFolder)) {
      final Iterator<Path> iterator = files.filter(Files::isRegularFile).iterator();
      while (iterator.hasNext()) {
        Files.delete(iterator.next());
      }
    }
    loadTranslations();
  }

  private void extractTranslationFiles() throws IOException {
    try (final InputStream translationsZip = this.plugin.getResource("translations.zip");
         final ZipInputStream zip = new ZipInputStream(requireNonNull(translationsZip, "translationsZip"))) {
      ZipEntry zipEntry;
      while ((zipEntry = zip.getNextEntry()) != null) {
        final Path translationFile = this.translationsFolder.resolve(zipEntry.getName());
        if (Files.notExists(translationFile)) {
          Files.copy(zip, translationFile);
        }
        zip.closeEntry();
      }
    }
  }

  private void registerTranslationFiles() throws IOException {
    if (this.registry != null) {
      GlobalTranslator.get().removeSource(this.registry);
    }

    this.registry = TranslationRegistry.create(TRANSLATIONS_KEY);
    this.registry.defaultLocale(DEFAULT_LOCALE);
    GlobalTranslator.get().addSource(this.registry);

    try (final Stream<Path> translationsFolderStream = Files.list(this.translationsFolder)) {
      translationsFolderStream.forEach(file -> {
        final Locale targetLocale = getFileTargetLocale(file);
        if (targetLocale != null) {
          this.registry.registerAll(targetLocale, file, false);
        }
      });
    }
  }

  private Locale getFileTargetLocale(final Path file) {
    final Matcher matcher = MESSAGES_FILE_PATTERN.matcher(file.getFileName().toString());
    if (matcher.matches()) {
      return Translator.parseLocale(matcher.group(1));
    } else {
      return null;
    }
  }
}
