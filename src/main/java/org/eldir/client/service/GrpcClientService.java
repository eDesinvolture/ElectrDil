package org.eldir.client.service;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.MetadataUtils; // Добавлен импорт
import org.eldir.shared.grpc.*;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.Map;

public class GrpcClientService {

    private final ManagedChannel channel;
    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;
    private final DocumentServiceGrpc.DocumentServiceBlockingStub docStub;
    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    private String jwtToken;

    public GrpcClientService() {
//        // --- НАСТРОЙКА SSL (TLS) ---
//        SslContext sslContext;
//        try {
//            sslContext = GrpcSslContexts.forClient()
//                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                    .build();
//        } catch (SSLException e) {
//            throw new RuntimeException("Failed to init SSL", e);
//        }

        this.channel = NettyChannelBuilder.forAddress("localhost", 6565)
                //.sslContext(sslContext)
                .usePlaintext()
                .build();

        this.authStub = AuthServiceGrpc.newBlockingStub(channel);
        this.docStub = DocumentServiceGrpc.newBlockingStub(channel);
        this.userStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public void createUser(String login, String password, AccessLevel level) {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setLogin(login)
                .setPassword(password)
                .setClearanceLevel(level.name())
                .build();

        getAuthenticatedUserStub().createUser(request);
    }

    public String login(String login, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setLogin(login)
                .setPassword(password)
                .build();

        LoginResponse response = authStub.login(request);
        this.jwtToken = response.getAccessToken();
        return this.jwtToken;
    }

    public void logout() {
        this.jwtToken = null;
    }

    public List<Document> getDocuments() {
        ListDocumentsRequest request = ListDocumentsRequest.newBuilder().build();

        ListDocumentsResponse response = getAuthenticatedDocStub().listDocuments(request);

        return response.getDocumentsList();
    }

    private DocumentServiceGrpc.DocumentServiceBlockingStub getAuthenticatedDocStub() {
        if (jwtToken == null) throw new IllegalStateException("Not logged in");

        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);

        return docStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    public Document createDocument(FileType type, AccessLevel access, Map<String, String> attrs) {
        CreateDocumentRequest request = CreateDocumentRequest.newBuilder()
                .setFileType(type)
                .setAccessLevel(access)
                .putAllInitialAttributes(attrs)
                .build();

        return getAuthenticatedDocStub().createDocument(request);
    }

    public void deleteDocument(String id) {
        DeleteDocumentRequest request = DeleteDocumentRequest.newBuilder().setId(id).build();
        getAuthenticatedDocStub().deleteDocument(request);
    }

    public Document getDocument(String id) {
        return getAuthenticatedDocStub().getDocument(GetDocumentRequest.newBuilder().setId(id).build());
    }

    public void shutdown() {
        channel.shutdown();
    }

    private UserServiceGrpc.UserServiceBlockingStub getAuthenticatedUserStub() {
        if (jwtToken == null) throw new IllegalStateException("Not logged in");
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);
        return userStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }
}