//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) Fefo6644 <federico.lopez.1999@outlook.com>
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

package com.github.fefo6644.betterjails.common.message;

import com.github.fefo6644.betterjails.common.plugin.BetterJailsPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class TranslationManager {

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
  private static final Key TRANSLATIONS_KEY = Key.key("betterjails", "translations");

  private final BetterJailsPlugin plugin;
  private final TranslationRegistry registry = TranslationRegistry.create(TRANSLATIONS_KEY);

  public TranslationManager(final BetterJailsPlugin plugin) {
    this.plugin = plugin;
    this.registry.defaultLocale(DEFAULT_LOCALE);
    GlobalTranslator.get().addSource(this.registry);
  }

  public void loadTranslations() throws Exception {
    final Path localeFolder = this.plugin.getPluginFolder().resolve("locale");
    Files.createDirectories(localeFolder);

    extractTranslationFiles(localeFolder);
    registerTranslationFiles(localeFolder);
  }

  private void extractTranslationFiles(final Path localeFolder) throws IOException, URISyntaxException {
    // jar:file:<absolute path>!/locale
    final URL url = this.plugin.getResourceURL("locale");
    // file:<absolute path>!/locale
    String jarPath = url.getPath();
    // file:<absolute path>
    jarPath = jarPath.substring(0, jarPath.length() - "!/locale".length());

    final File thisFile = new File(new URL(jarPath).toURI());
    final JarFile jarFile = new JarFile(thisFile);
    jarFile
        .stream().parallel()
        .map(JarEntry::getName)
        .filter(name -> name.startsWith("locale/"))
        .filter(name -> name.endsWith(".properties"))
        .filter(name -> Files.notExists(localeFolder.resolve(name.substring("locale/".length()))))
        .forEach(name -> {
          try (final InputStream stream = this.plugin.getResource(name)) {
            Files.copy(stream, localeFolder.resolve(name.substring("locale/".length())));
          } catch (final IOException exception) {
            exception.printStackTrace();
          }
        });
  }

  private void registerTranslationFiles(final Path localeFolder) throws IOException {
    try (final Stream<Path> localeFolderStream = Files.list(localeFolder)) {
      localeFolderStream
          .forEach(path -> {
            final String fileName = path.getFileName().toString();
            final String localeString = fileName.substring(0, fileName.length() - ".properties".length());
            final Locale locale = Translator.parseLocale(localeString);
            this.registry.registerAll(locale != null ? locale : DEFAULT_LOCALE, path, false);
          });
    }
  }
}
