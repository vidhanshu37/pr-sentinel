package com.pr_reviewer.pr_reviewer.diff;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DiffParser {

    private static final Pattern FILE_HEADER = Pattern.compile("^diff --git a/(.+) b/(.+)$");
    private static final Pattern HUNK_HEADER = Pattern.compile("^@@ -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@.*$");

    public List<FileDiff> parse(String rawDiff) {
        List<FileDiff> files = new ArrayList<>();
        String[] lines = rawDiff.split("\n", -1);

        String currentFilePath = null;
        List<FileDiff.Hunk> currentHunks = null;
        List<FileDiff.DiffLine> currentHunkLines = null;
        int oldLineNum = 0;
        int newLineNum = 0;
        int hunkOldStart = 0;
        int hunkNewStart = 0;
        int diffPosition = 0;

        for (String line : lines) {
            Matcher fileMatch = FILE_HEADER.matcher(line);
            if (fileMatch.matches()) {
                // starting a new file — close out the previous one first
                if (currentFilePath != null) {
                    files.add(new FileDiff(currentFilePath, currentHunks));
                }
                currentFilePath = fileMatch.group(2); // "b/" path = the new file path
                currentHunks = new ArrayList<>();
                diffPosition = 0; // GitHub resets position counting per file
                continue;
            }

            Matcher hunkMatch = HUNK_HEADER.matcher(line);
            if (hunkMatch.matches()) {
                if (currentHunkLines != null) {
                    currentHunks.add(new FileDiff.Hunk(hunkOldStart, hunkNewStart, currentHunkLines));
                }
                hunkOldStart = Integer.parseInt(hunkMatch.group(1));
                hunkNewStart = Integer.parseInt(hunkMatch.group(2));
                oldLineNum = hunkOldStart;
                newLineNum = hunkNewStart;
                currentHunkLines = new ArrayList<>();
                diffPosition++; // the @@ line itself counts as a position
                continue;
            }

            if (currentHunkLines == null) continue; // skip diff preamble (index lines, etc.)

            diffPosition++;
            if (line.startsWith("+")) {
                currentHunkLines.add(new FileDiff.DiffLine(
                        FileDiff.LineType.ADDED, line.substring(1), null, newLineNum, diffPosition));
                newLineNum++;
            } else if (line.startsWith("-")) {
                currentHunkLines.add(new FileDiff.DiffLine(
                        FileDiff.LineType.REMOVED, line.substring(1), oldLineNum, null, diffPosition));
                oldLineNum++;
            } else if (line.startsWith(" ")) {
                currentHunkLines.add(new FileDiff.DiffLine(
                        FileDiff.LineType.CONTEXT, line.substring(1), oldLineNum, newLineNum, diffPosition));
                oldLineNum++;
                newLineNum++;
            }
            // lines like "\ No newline at end of file" are ignored
        }

        // flush the last file/hunk
        if (currentHunkLines != null) {
            currentHunks.add(new FileDiff.Hunk(hunkOldStart, hunkNewStart, currentHunkLines));
        }
        if (currentFilePath != null) {
            files.add(new FileDiff(currentFilePath, currentHunks));
        }

        return files;
    }
}