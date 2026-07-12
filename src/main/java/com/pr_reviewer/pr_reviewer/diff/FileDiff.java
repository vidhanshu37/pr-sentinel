package com.pr_reviewer.pr_reviewer.diff;

import java.util.List;

public record FileDiff(
        String filePath,
        List<Hunk> hunks
) {
    public record Hunk(
            int oldStart,
            int newStart,
            List<DiffLine> lines
    ) {}

    public record DiffLine(
            LineType type,
            String content,
            Integer oldLineNumber,   // null for ADDED lines (don't exist in old file)
            Integer newLineNumber,   // null for REMOVED lines (don't exist in new file)
            int diffPosition         // offset within the whole diff - what GitHub's comment API wants
    ) {}

    public enum LineType { ADDED, REMOVED, CONTEXT }
}