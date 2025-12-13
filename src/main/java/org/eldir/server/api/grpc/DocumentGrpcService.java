package org.eldir.server.api.grpc;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.eldir.server.service.DocumentService;
import org.eldir.shared.grpc.*; // Импорт сгенерированных классов
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Map;

@GRpcService
@Profile("server")
public class DocumentGrpcService extends DocumentServiceGrpc.DocumentServiceImplBase {

    private final DocumentService documentService;

    public DocumentGrpcService(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void createDocument(CreateDocumentRequest request, StreamObserver<Document> responseObserver) {
        try {
            FileType type = request.getFileType();
            AccessLevel access = request.getAccessLevel();
            Map<String, String> attrs = request.getInitialAttributesMap();

            // В реальной системе логин берется из SecurityContextHolder (из токена)
            // Пока для простоты хардкод или передача, но допустим admin
            String currentLogin = "admin";

            Document createdDoc = documentService.createDocument(type, access, attrs, currentLogin);

            responseObserver.onNext(createdDoc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getDocument(GetDocumentRequest request, StreamObserver<Document> responseObserver) {
        try {
            Document doc = documentService.getDocument(request.getId());
            responseObserver.onNext(doc);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void listDocuments(ListDocumentsRequest request, StreamObserver<ListDocumentsResponse> responseObserver) {
        try {
            List<Document> docs = documentService.getAllDocuments();

            ListDocumentsResponse response = ListDocumentsResponse.newBuilder()
                    .addAllDocuments(docs)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateDocument(UpdateDocumentRequest request, StreamObserver<Document> responseObserver) {
        try {
            Document updated = documentService.updateDocument(
                    request.getId(),
                    request.getFileType(),
                    request.getAccessLevel(),
                    request.getUpdatedAttributesMap()
            );
            responseObserver.onNext(updated);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteDocument(DeleteDocumentRequest request, StreamObserver<DeleteDocumentResponse> responseObserver) {
        try {
            documentService.deleteDocument(request.getId());

            DeleteDocumentResponse response = DeleteDocumentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Document deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}