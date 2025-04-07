package org.opendatakit.logging.desktop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Custom log handler for capturing log records during testing.
 */
public class TestLogHandler extends Handler {
    private List<LogRecord> logRecords = new ArrayList<>();

    @Override
    public void publish(LogRecord record) {
        logRecords.add(record);
    }

    @Override
    public void flush() {
        // No-op for testing
    }

    @Override
    public void close() throws SecurityException {
        logRecords.clear();
    }

    public List<LogRecord> getLogRecords() {
        return logRecords;
    }

    public void clear() {
        logRecords.clear();
    }

    public LogRecord getLastLogRecord() {
        if (logRecords.isEmpty()) {
            return null;
        }
        return logRecords.get(logRecords.size() - 1);
    }
}
