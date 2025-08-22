package br.com.paranabanco.dataprev.cnab.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abstraction over a CNAB resource that can provide readers and writers.
 */
public interface CnabResource {

    /**
     * Opens a reader to consume the underlying resource.
     *
     * @return reader for the resource
     * @throws IOException if the resource cannot be opened for reading
     */
    Reader openReader() throws IOException;

    /**
     * Opens a writer to produce content to the underlying resource.
     *
     * @return writer for the resource
     * @throws IOException if the resource cannot be opened for writing
     */
    Writer openWriter() throws IOException;

    /**
     * Creates a {@link CnabResource} backed by a local {@link Path}.
     *
     * @param path path to the resource
     * @return resource implementation for the given path
     */
    static CnabResource forPath(Path path) {
        return new PathCnabResource(path);
    }

    /**
     * TODO: provide implementations for remote resources such as SFTP or CONNECT.
     */

    class PathCnabResource implements CnabResource {

        private final Path path;

        PathCnabResource(Path path) {
            this.path = path;
        }

        @Override
        public Reader openReader() throws IOException {
            return Files.newBufferedReader(path);
        }

        @Override
        public Writer openWriter() throws IOException {
            return Files.newBufferedWriter(path);
        }
    }
}

