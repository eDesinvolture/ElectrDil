package org.eldir.server.service;

import org.eldir.server.entity.EavAttribute;
import org.eldir.server.entity.EavDocument;
import org.eldir.server.entity.EavValue;
import org.eldir.server.entity.User;
import org.eldir.server.repository.AttributeRepository;
import org.eldir.server.repository.DocumentRepository;
import org.eldir.server.repository.UserRepository;
import org.eldir.shared.grpc.AccessLevel;
import org.eldir.shared.grpc.Document;
import org.eldir.shared.grpc.FileType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("server")
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final AttributeRepository attributeRepository;
    private final UserRepository userRepository;

    public DocumentService(DocumentRepository documentRepository,
                           AttributeRepository attributeRepository,
                           UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.attributeRepository = attributeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Document createDocument(FileType fileType, AccessLevel accessLevel, Map<String, String> attributes, String authorLogin) {

        User author = userRepository.findByLogin(authorLogin)
                .orElseThrow(() -> new RuntimeException("Author not found: " + authorLogin));

        EavDocument doc = new EavDocument();
        doc.setAuthor(author);
        doc.setFileType(fileType.name());
        doc.setAccessLevel(accessLevel.name());

        updateAttributes(doc, attributes);
        doc = documentRepository.save(doc);

        return mapToProto(doc);
    }

    @Transactional
    public Document getDocument(String id) {
        EavDocument doc = documentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        // Специфичную бизнес-логику (сжигание файла) можно оставить,
        // но технические логи ("метод вызван") делает Аспект.
        if ("ACCESS_BURN_AFTER_READING".equals(doc.getAccessLevel())) {
            Document proto = mapToProto(doc);
            documentRepository.delete(doc);
            return proto;
        }

        return mapToProto(doc);
    }

    @Transactional
    public Document updateDocument(String id, FileType fileType, AccessLevel accessLevel, Map<String, String> attributes) {
        EavDocument doc = documentRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Document not found"));

        if (fileType != FileType.FILE_TYPE_UNKNOWN) {
            doc.setFileType(fileType.name());
        }
        if (accessLevel != AccessLevel.ACCESS_UNKNOWN) {
            doc.setAccessLevel(accessLevel.name());
        }

        updateAttributes(doc, attributes);

        return mapToProto(documentRepository.save(doc));
    }

    @Transactional
    public void deleteDocument(String id) {
        documentRepository.deleteById(UUID.fromString(id));
    }

    @Transactional
    public List<Document> getAllDocuments() {
        // 1. Берем все документы из базы
        List<EavDocument> docs = documentRepository.findAll();

        // 2. Превращаем их в Proto-формат
        return docs.stream()
                .map(this::mapToProto) // Используем существующий метод маппинга
                .collect(Collectors.toList());
    }

    private void updateAttributes(EavDocument doc, Map<String, String> newAttributes) {
        if (newAttributes == null || newAttributes.isEmpty()) return;

        newAttributes.forEach((code, val) -> {
            EavAttribute attrDef = attributeRepository.findById(code)
                    .orElseThrow(() -> new RuntimeException("Unknown attribute code: " + code));

            EavValue existingValue = doc.getValues().stream()
                    .filter(v -> v.getAttribute().getCode().equals(code))
                    .findFirst()
                    .orElse(null);

            if (existingValue != null) {
                existingValue.setValue(val);
            } else {
                EavValue newValue = new EavValue();
                newValue.setDocument(doc);
                newValue.setAttribute(attrDef);
                newValue.setValue(val);

                doc.getValues().add(newValue);
            }
        });
    }

    private Document mapToProto(EavDocument doc) {
        Map<String, String> attrMap = new HashMap<>();
        if (doc.getValues() != null) {
            attrMap = doc.getValues().stream()
                    .collect(Collectors.toMap(
                            v -> v.getAttribute().getCode(),
                            EavValue::getValue
                    ));
        }

        FileType fType = FileType.FILE_TYPE_UNKNOWN;
        try { if(doc.getFileType() != null) fType = FileType.valueOf(doc.getFileType()); } catch (Exception ignored){}

        AccessLevel aLevel = AccessLevel.ACCESS_UNKNOWN;
        try { if(doc.getAccessLevel() != null) aLevel = AccessLevel.valueOf(doc.getAccessLevel()); } catch (Exception ignored){}

        return Document.newBuilder()
                .setId(doc.getId().toString())
                .setFileType(fType)
                .setAccessLevel(aLevel)
                .putAllAttributes(attrMap)
                .build();
    }
}