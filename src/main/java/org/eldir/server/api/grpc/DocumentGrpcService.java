package org.eldir.server.api.grpc;

import io.grpc.stub.StreamObserver;
import org.eldir.server.security.GrpcAuthInterceptor;
import org.lognet.springboot.grpc.GRpcService;
import org.eldir.server.service.DocumentService;
import org.eldir.shared.grpc.*;
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

            String login = GrpcAuthInterceptor.USER_LOGIN_KEY.get();

            if (login == null) {
                System.err.println("CRITICAL ERROR: Security Context is empty! Using 'admin' as fallback.");
                login = "admin";
            }

            Document createdDoc = documentService.createDocument(type, access, attrs, login);

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
            String login = GrpcAuthInterceptor.USER_LOGIN_KEY.get();
            List<Document> docs = documentService.getAllDocuments(login);

            if (login == null) {
                System.err.println("CRITICAL ERROR: Security Context is empty!");
                login = "admin";
            }
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