package org.drools.example.osgi;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Example monitors a specified directory for new files.
 * If a newly added file is a plain text file, the file can
 * be emailed to the appropriate alias.  The emailing details
 * are left to the reader.  What the example actually does is
 * print the details to standard out.
 */

public class WatchingXLSFile {

        private final WatchService watcher;
        private final Path dir;

        /**
         * Creates a WatchService and registers the given directory
         */
        WatchingXLSFile(Path dir) throws IOException {
            this.watcher = FileSystems.getDefault().newWatchService();
            dir.register(watcher, ENTRY_MODIFY);
            this.dir = dir;
        }

        /**
         * Process all events for the key queued to the watcher.
         */
        void processEvents() throws IOException {

            System.out.println("Wait for a file modification");

            for (;;) {

                // wait for key to be signaled
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException x) {
                    return;
                }

                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    //The filename is the context of the event.
                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    Path filename = ev.context();

                    // File Modified
                    Path child = dir.resolve(filename);
                    BasicFileAttributes attrs = Files.readAttributes(child, BasicFileAttributes.class);

                    System.out.println("Modified attr " + attrs.lastModifiedTime());
                    //Details left to reader....
                }

                //Reset the key -- this step is critical if you want to receive
                //further watch events. If the key is no longer valid, the directory
                //is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }

        static void usage() {
            System.err.println("usage: java Email dir");
            System.exit(-1);
        }

        public static void main(String[] args) throws IOException {

            //register directory and process its events
            Path dir = Paths.get("/Users/chmoulli/Temp/test");
            new WatchingXLSFile(dir).processEvents();
        }
    }
