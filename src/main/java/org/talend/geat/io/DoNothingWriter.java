package org.talend.geat.io;

import java.io.IOException;
import java.io.Writer;

/**
 * Implementation of java.io.Writer that does nothing. Used in JUnit.
 */
public class DoNothingWriter extends Writer {


    public DoNothingWriter() {
        super();
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
    }

}
