package com.pr_reviewer.pr_reviewer.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CodebaseIngestionService {
    private final JavaMethodChunker chunker;
    private final VectorStore vectorStore;

    public CodebaseIngestionService(JavaMethodChunker chunker, VectorStore vectorStore) {
        this.chunker = chunker;
        this.vectorStore = vectorStore;
    }

    public void ingestJavaFile(String filePath, String sourceCode) {
        List<JavaMethodChunker.CodeChunk> chunks = chunker.chunkFile(filePath, sourceCode);

        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(
                        chunk.content(),
                        Map.of(
                                "filePath", chunk.filePath(),
                                "className", chunk.className(),
                                "documentType", "CODE",
                                "language", "java"
                        )
                ))
                .collect(Collectors.toList());

        vectorStore.add(documents);
    }

    public void ingestStandardsDoc(String docId, String title, String content) {
        Document document = new Document(
                content,
                Map.of(
                        "docId", docId,
                        "title", title,
                        "documentType", "STANDARD"
                )
        );
        vectorStore.add(List.of(document));
    }
}
