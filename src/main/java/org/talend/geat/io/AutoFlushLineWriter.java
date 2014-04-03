package org.talend.geat.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Simple utility java.io.Writer wrapper used to 1. add a carriage return after each line, 2. flush every line.
 */
public class AutoFlushLineWriter extends Writer {

    private Writer writer;

    public AutoFlushLineWriter(Writer writer) {
        super();
        this.writer = writer;
    }

    @Override
    public void close() throws IOException {
        this.writer.close();
    }

    @Override
    public void flush() throws IOException {
        this.writer.flush();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        this.writer.write(cbuf, off, len);
    }

    public void write(String str) throws IOException {
        this.writer.write(str + System.lineSeparator());
        this.writer.flush();
    }

}
