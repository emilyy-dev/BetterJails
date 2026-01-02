//
// This file is part of BetterJails, licensed under the MIT License.
//
// Copyright (c) 2025 emilyy-dev
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

package io.github.emilyydev.betterjails.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Handles logging of actions performed by the plugin for audit purposes.
 */
public final class ActionLogger implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger("BetterJails");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    private final Path logDirectory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r, "BetterJails Action Logger");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Creates a new action logger.
     *
     * @param logDirectory The directory where action logs will be stored
     * @throws IOException If the log directory cannot be created
     */
    public ActionLogger(final Path logDirectory) throws IOException {
        this.logDirectory = logDirectory;
        Files.createDirectories(logDirectory);
    }

    /**
     * Logs a jail action.
     *
     * @param prisonerUuid The UUID of the prisoner
     * @param prisonerName The name of the prisoner
     * @param jailName     The name of the jail
     * @param duration     The jail duration
     * @param reason       The jail reason (nullable)
     * @param actorUuid    The UUID of the actor (null if console)
     * @param actorName    The name of the actor
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logJail(
            final UUID prisonerUuid,
            final @Nullable String prisonerName,
            final String jailName,
            final String duration,
            final @Nullable String reason,
            final @Nullable UUID actorUuid,
            final String actorName
    ) {
        final String entry = String.format("[JAIL] Prisoner: %s (%s) | Jail: %s | Duration: %s | Reason: %s | Actor: %s (%s)",
                prisonerName != null ? prisonerName : "Unknown",
                prisonerUuid,
                jailName,
                duration,
                reason != null ? reason : "No reason",
                actorName,
                actorUuid != null ? actorUuid.toString() : "Console"
        );
        return log(entry);
    }

    /**
     * Logs a release action.
     *
     * @param prisonerUuid The UUID of the prisoner
     * @param prisonerName The name of the prisoner
     * @param jailName     The name of the jail they were in
     * @param actorUuid    The UUID of the actor (null if console)
     * @param actorName    The name of the actor
     * @param automatic    Whether the release was automatic (time served)
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logRelease(
            final UUID prisonerUuid,
            final @Nullable String prisonerName,
            final String jailName,
            final @Nullable UUID actorUuid,
            final String actorName,
            final boolean automatic
    ) {
        final String entry = String.format("[RELEASE] Prisoner: %s (%s) | Jail: %s | Actor: %s (%s) | Automatic: %s",
                prisonerName != null ? prisonerName : "Unknown",
                prisonerUuid,
                jailName,
                actorName,
                actorUuid != null ? actorUuid.toString() : "Console",
                automatic
        );
        return log(entry);
    }

    /**
     * Logs a jail creation action.
     *
     * @param jailName  The name of the jail
     * @param world     The world name
     * @param x         The X coordinate
     * @param y         The Y coordinate
     * @param z         The Z coordinate
     * @param actorName The name of the actor
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logJailCreate(
            final String jailName,
            final String world,
            final double x,
            final double y,
            final double z,
            final String actorName
    ) {
        final String entry = String.format("[JAIL_CREATE] Name: %s | Location: %s %.2f, %.2f, %.2f | Actor: %s",
                jailName,
                world,
                x, y, z,
                actorName
        );
        return log(entry);
    }

    /**
     * Logs a jail deletion action.
     *
     * @param jailName  The name of the jail
     * @param actorName The name of the actor
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logJailDelete(
            final String jailName,
            final String actorName
    ) {
        final String entry = String.format("[JAIL_DELETE] Name: %s | Actor: %s",
                jailName,
                actorName
        );
        return log(entry);
    }

    /**
     * Logs a jail modification action.
     *
     * @param jailName         The name of the jail
     * @param modificationType The type of modification
     * @param actorName        The name of the actor
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logJailModify(
            final String jailName,
            final String modificationType,
            final String actorName
    ) {
        final String entry = String.format("[JAIL_MODIFY] Name: %s | Type: %s | Actor: %s",
                jailName,
                modificationType,
                actorName
        );
        return log(entry);
    }

    /**
     * Logs a sentence time modification action.
     *
     * @param prisonerUuid The UUID of the prisoner
     * @param prisonerName The name of the prisoner
     * @param action       The action type (add/subtract/set)
     * @param newDuration  The new duration
     * @param actorName    The name of the actor
     * @return A future that completes when the log is written
     */
    public @NotNull CompletableFuture<Void> logJailTimeModify(
            final UUID prisonerUuid,
            final @Nullable String prisonerName,
            final String action,
            final String newDuration,
            final String actorName
    ) {
        final String entry = String.format("[JAILTIME_MODIFY] Prisoner: %s (%s) | Action: %s | New Duration: %s | Actor: %s",
                prisonerName != null ? prisonerName : "Unknown",
                prisonerUuid,
                action,
                newDuration,
                actorName
        );
        return log(entry);
    }

    /**
     * Writes a log entry to the log file.
     *
     * @param message The log message
     * @return A future that completes when the log is written
     */
    @Contract("_ -> new")
    private @NotNull CompletableFuture<Void> log(final String message) {
        return CompletableFuture.runAsync(() -> {
            final Instant now = Instant.now();
            final String timestamp = TIMESTAMP_FORMAT.format(now);
            final String fileDate = FILE_DATE_FORMAT.format(now);
            final Path logFile = this.logDirectory.resolve("actions-" + fileDate + ".log");

            try (final BufferedWriter writer = Files.newBufferedWriter(
                    logFile,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            )) {
                writer.write(timestamp);
                writer.write(" ");
                writer.write(message);
                writer.newLine();
            } catch (final IOException ex) {
                LOGGER.error("Failed to write action log", ex);
            }
        }, this.executor);
    }

    @Override
    public void close() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
            }
        } catch (final InterruptedException ex) {
            this.executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}