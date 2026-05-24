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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AttributeRepository attributeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentService documentService;

    private User testUser;
    private EavAttribute titleAttribute;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .login("test_user")
                .clearanceLevel("ACCESS_DSP")
                .build();

        titleAttribute = new EavAttribute("title", "Название документа");
    }

    @ParameterizedTest(name = "Допуск {index}: Пользователь={0}, Документ={1} -> Ожидается доступ: {2}")
    @CsvSource({
            // Допуск PUBLIC (PUBLIC)
            "ACCESS_PUBLIC, ACCESS_PUBLIC, true",
            "ACCESS_PUBLIC, ACCESS_DSP, false",
            "ACCESS_PUBLIC, ACCESS_SECRET, false",
            "ACCESS_PUBLIC, ACCESS_BURN_AFTER_READING, false",

            // Допуск DSP (PUBLIC и DSP)
            "ACCESS_DSP, ACCESS_PUBLIC, true",
            "ACCESS_DSP, ACCESS_DSP, true",
            "ACCESS_DSP, ACCESS_SECRET, false",
            "ACCESS_DSP, ACCESS_BURN_AFTER_READING, false",

            // Допуск SECRET (PUBLIC, DSP, SECRET)
            "ACCESS_SECRET, ACCESS_PUBLIC, true",
            "ACCESS_SECRET, ACCESS_DSP, true",
            "ACCESS_SECRET, ACCESS_SECRET, true",
            "ACCESS_SECRET, ACCESS_BURN_AFTER_READING, false",

            // Допуск TOP_SECRET
            "ACCESS_TOP_SECRET, ACCESS_PUBLIC, true",
            "ACCESS_TOP_SECRET, ACCESS_DSP, true",
            "ACCESS_TOP_SECRET, ACCESS_SECRET, true",
            "ACCESS_TOP_SECRET, ACCESS_BURN_AFTER_READING, true"
    })
    void testDocumentAccessClearance(String userLevel, String docLevel, boolean expectedAccess) {
        String requesterLogin = "test_user";
        User user = User.builder()
                .id(UUID.randomUUID())
                .login(requesterLogin)
                .clearanceLevel(userLevel)
                .build();

        when(userRepository.findByLogin(requesterLogin)).thenReturn(Optional.of(user));

        EavDocument doc = new EavDocument();
        doc.setId(UUID.randomUUID());
        doc.setAccessLevel(docLevel);

        when(documentRepository.findAll()).thenReturn(List.of(doc));

        List<Document> result = documentService.getAllDocuments(requesterLogin);

        if (expectedAccess) {
            assertEquals(1, result.size());
            assertEquals(doc.getId().toString(), result.get(0).getId());
        } else {
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void createDocument_Success() {
        String authorLogin = "test_user";
        Map<String, String> attributes = Map.of("title", "Секретный отчет");

        when(userRepository.findByLogin(authorLogin)).thenReturn(Optional.of(testUser));
        when(attributeRepository.findById("title")).thenReturn(Optional.of(titleAttribute));

        //  сохранение документа в репозитории
        UUID expectedId = UUID.randomUUID();
        when(documentRepository.save(any(EavDocument.class))).thenAnswer(invocation -> {
            EavDocument savedDoc = invocation.getArgument(0);
            savedDoc.setId(expectedId); // Присваиваем ID, как это делает JPA при сохранении
            return savedDoc;
        });

        Document result = documentService.createDocument(
                FileType.FILE_TYPE_PDF,
                AccessLevel.ACCESS_DSP,
                attributes,
                authorLogin
        );

        assertNotNull(result);
        assertEquals(expectedId.toString(), result.getId());
        assertEquals(FileType.FILE_TYPE_PDF, result.getFileType());
        assertEquals(AccessLevel.ACCESS_DSP, result.getAccessLevel());
        assertEquals("Секретный отчет", result.getAttributesOrThrow("title"));

        verify(userRepository).findByLogin(authorLogin);
        verify(attributeRepository).findById("title");
        verify(documentRepository).save(any(EavDocument.class));
    }

    @Test
    void getDocument_Regular_Success() {
        UUID docId = UUID.randomUUID();
        EavDocument doc = new EavDocument();
        doc.setId(docId);
        doc.setFileType("FILE_TYPE_TXT");
        doc.setAccessLevel("ACCESS_PUBLIC");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        Document result = documentService.getDocument(docId.toString());

        assertNotNull(result);
        assertEquals(docId.toString(), result.getId());
        assertEquals(AccessLevel.ACCESS_PUBLIC, result.getAccessLevel());

        // Метод удаления НЕ должен вызываться для обычных документов
        verify(documentRepository, never()).delete(any(EavDocument.class));
    }

    @Test
    void getDocument_BurnAfterReading_ShouldDeleteOnRead() {
        UUID docId = UUID.randomUUID();
        EavDocument doc = new EavDocument();
        doc.setId(docId);
        doc.setFileType("FILE_TYPE_MD");
        doc.setAccessLevel("ACCESS_BURN_AFTER_READING");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));

        Document result = documentService.getDocument(docId.toString());

        assertNotNull(result);
        assertEquals(docId.toString(), result.getId());
        assertEquals(AccessLevel.ACCESS_BURN_AFTER_READING, result.getAccessLevel());

        // Метод удаления ДОЛЖЕН вызваться сразу после чтения
        verify(documentRepository).delete(doc);
    }

    @Test
    void deleteDocument_Success() {
        UUID docId = UUID.randomUUID();

        documentService.deleteDocument(docId.toString());

        verify(documentRepository).deleteById(docId);
    }

    @Test
    void getAllDocuments_FiltersByClearanceLevel() {
        String requesterLogin = "test_user";
        when(userRepository.findByLogin(requesterLogin)).thenReturn(Optional.of(testUser)); // Уровень пользователя: ACCESS_DSP (значение 2)

        // ACCESS_PUBLIC должен быть виден
        EavDocument docPublic = new EavDocument();
        docPublic.setId(UUID.randomUUID());
        docPublic.setAccessLevel("ACCESS_PUBLIC");

        // ACCESS_DSP должен быть виден
        EavDocument docDsp = new EavDocument();
        docDsp.setId(UUID.randomUUID());
        docDsp.setAccessLevel("ACCESS_DSP");

        // ACCESS_SECRET не должен быть виден
        EavDocument docSecret = new EavDocument();
        docSecret.setId(UUID.randomUUID());
        docSecret.setAccessLevel("ACCESS_SECRET");

        // ACCESS_BURN_AFTER_READING не должен быть виден
        EavDocument docBurn = new EavDocument();
        docBurn.setId(UUID.randomUUID());
        docBurn.setAccessLevel("ACCESS_BURN_AFTER_READING");

        List<EavDocument> allDbDocs = List.of(docPublic, docDsp, docSecret, docBurn);
        when(documentRepository.findAll()).thenReturn(allDbDocs);

        List<Document> filteredDocs = documentService.getAllDocuments(requesterLogin);

        assertEquals(2, filteredDocs.size());
        assertTrue(filteredDocs.stream().anyMatch(d -> d.getId().equals(docPublic.getId().toString())));
        assertTrue(filteredDocs.stream().anyMatch(d -> d.getId().equals(docDsp.getId().toString())));
        assertFalse(filteredDocs.stream().anyMatch(d -> d.getId().equals(docSecret.getId().toString())));
        assertFalse(filteredDocs.stream().anyMatch(d -> d.getId().equals(docBurn.getId().toString())));
    }

    @Test
    void getAllDocuments_BurnAfterReading_AllowedForTopSecretUser() {
        String requesterLogin = "admin_user";
        User topSecretUser = User.builder()
                .id(UUID.randomUUID())
                .login(requesterLogin)
                .clearanceLevel("ACCESS_TOP_SECRET")
                .build();

        when(userRepository.findByLogin(requesterLogin)).thenReturn(Optional.of(topSecretUser));

        EavDocument docBurn = new EavDocument();
        docBurn.setId(UUID.randomUUID());
        docBurn.setAccessLevel("ACCESS_BURN_AFTER_READING");

        when(documentRepository.findAll()).thenReturn(List.of(docBurn));

        List<Document> result = documentService.getAllDocuments(requesterLogin);

        assertEquals(1, result.size());
        assertEquals(docBurn.getId().toString(), result.get(0).getId());
    }

    @Test
    void updateDocument_Success() {
        UUID docId = UUID.randomUUID();
        EavDocument existingDoc = new EavDocument();
        existingDoc.setId(docId);
        existingDoc.setFileType("FILE_TYPE_TXT");
        existingDoc.setAccessLevel("ACCESS_PUBLIC");
        existingDoc.setValues(new ArrayList<>());

        when(documentRepository.findById(docId)).thenReturn(Optional.of(existingDoc));
        when(attributeRepository.findById("title")).thenReturn(Optional.of(titleAttribute));
        when(documentRepository.save(any(EavDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, String> updatedAttrs = Map.of("title", "Новое Название");

        Document result = documentService.updateDocument(
                docId.toString(),
                FileType.FILE_TYPE_PDF,
                AccessLevel.ACCESS_SECRET,
                updatedAttrs
        );

        assertNotNull(result);
        assertEquals(FileType.FILE_TYPE_PDF, result.getFileType());
        assertEquals(AccessLevel.ACCESS_SECRET, result.getAccessLevel());
        assertEquals("Новое Название", result.getAttributesOrThrow("title"));

        verify(documentRepository).save(existingDoc);
    }
}