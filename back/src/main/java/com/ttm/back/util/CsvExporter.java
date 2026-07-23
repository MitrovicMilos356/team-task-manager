package com.ttm.back.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static <T> ResponseEntity<byte[]> export(String filename, List<String> headers, List<T> items,
                                                      Function<T, List<String>> rowMapper) {
        StringBuilder sb = new StringBuilder();
        writeRow(sb, headers);
        for (T item : items) {
            writeRow(sb, rowMapper.apply(item));
        }

        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv;charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    private static void writeRow(StringBuilder sb, List<String> values) {
        sb.append(values.stream().map(CsvExporter::escape).collect(Collectors.joining(","))).append("\r\n");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}
