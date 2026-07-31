package com.pr_reviewer.pr_reviewer.rag;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JavaMethodChunker {

    private static final Pattern METHOD_SIGNATURE = Pattern.compile(
            "(?m)^\\s*(public|private|protected)[^;{]*\\([^)]*\\)\\s*(throws\\s+[\\w,\\s]+)?\\s*\\{"
    );

    public List<CodeChunk> chunkFile(String filePath, String sourceCode) {
        List<CodeChunk> chunks = new ArrayList<>();
        String className = extractClassName(sourceCode);

        Matcher matcher = METHOD_SIGNATURE.matcher(sourceCode);
        while (matcher.find()) {
            int methodStart = matcher.start();
            int braceStart = matcher.end() - 1;
            int methodEnd = findMatchingBrace(sourceCode, braceStart);

            if (methodEnd == -1) continue;

            String methodBody = sourceCode.substring(methodStart, methodEnd + 1);
            chunks.add(new CodeChunk(filePath, className, methodBody));
        }

        if (chunks.isEmpty()) {
            chunks.add(new CodeChunk(filePath, className, sourceCode));
        }

        return chunks;
    }

    private int findMatchingBrace(String source, int openBraceIndex) {
        int depth = 0;
        for (int i = openBraceIndex; i < source.length(); i++) {
            if (source.charAt(i) == '{') depth++;
            if (source.charAt(i) == '}') depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    private String extractClassName(String sourceCode) {
        Matcher m = Pattern.compile("(?:class|record|interface)\\s+(\\w+)").matcher(sourceCode);
        return m.find() ? m.group(1) : "Unknown";
    }

    public record CodeChunk(String filePath, String className, String content) {}
}