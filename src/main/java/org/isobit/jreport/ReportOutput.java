package org.isobit.jreport;

import javax.ws.rs.core.StreamingOutput;

public class ReportOutput {

    private final StreamingOutput stream;
    private final long length;

    public ReportOutput(
        StreamingOutput stream,
        long length
    ) {
        this.stream = stream;
        this.length = length;
    }

    public StreamingOutput getStream() {
        return stream;
    }

    public long getLength() {
        return length;
    }
}