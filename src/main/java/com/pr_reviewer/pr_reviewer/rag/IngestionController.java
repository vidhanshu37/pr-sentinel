package com.pr_reviewer.pr_reviewer.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Files;

public class IngestionController {
    private final CodebaseIngestionService codebaseIngestionService;


    public IngestionController(CodebaseIngestionService codebaseIngestionService) {
        this.codebaseIngestionService = codebaseIngestionService;
    }

    @PostMapping("/ingest-file")
    public ResponseEntity<String> ingestFile(@RequestParam String filePath) throws Exception {
        String content = Files.readString(java.nio.file.Path.of(filePath));

        codebaseIngestionService.ingestJavaFile(filePath, content);

        return ResponseEntity.ok("File ingested successfully" + filePath);
    }
}
