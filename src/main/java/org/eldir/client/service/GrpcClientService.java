//package org.eldir.client.service;
//
//import io.grpc.*;
//import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
//import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
//import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
//import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
//import org.eldir.shared.grpc.*;
//
//import javax.net.ssl.SSLException;
//import java.util.Iterator;
//import java.util.Map;
//
//public class GrpcClientService {
//
//    private final ManagedChannel channel;
//    private final AuthServiceGrpc.AuthServiceBlockingStub authStub;
//    private final DocumentServiceGrpc.DocumentServiceBlockingStub docStub;
//
//    private String jwtToken;
//
//    public GrpcClientService() {
//        // --- НАСТРОЙКА SSL (TLS) ---
//        // Для локальной разработки используем InsecureTrustManager (доверяем самоподписанным).
//        // В продакшене сюда нужно подсунуть реальный ca.crt.
//        SslContext sslContext;
//        try {
//            sslContext = GrpcSslContexts.forClient()
//                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                    .build();
//        } catch (SSLException e) {
//            throw new RuntimeException("Failed to init SSL", e);
//        }
//
//        this.channel = NettyChannelBuilder.forAddress("localhost", 6565)
//                .sslContext(sslContext) // ВКЛЮЧАЕМ SSL
//                // .usePlaintext()
//                .build();
//
//        this.authStub = AuthServiceGrpc.newBlockingStub(channel);
//        this.docStub = DocumentServiceGrpc.newBlockingStub(channel);
//    }
//
//    public String login(String login, String password) {
//        LoginRequest request = LoginRequest.newBuilder()
//                .setEmail(login) // В прото у нас email, но по факту логин
//                .setPassword(password)
//                .build();
//
//        LoginResponse response = authStub.login(request);
//        this.jwtToken = response.getAccessToken();
//        return this.jwtToken;
//    }
//
//    public Iterator<Document> getDocuments() {
//        // В курсовой упростим: метод getDocument(id) есть, а listDocuments нет в proto.
//        // Обычно делают rpc ListDocuments.
//        // Но так как у нас API строго по заданию, пока оставим заглушку или
//        // предположим, что мы знаем ID.
//        // Для теста я сделаю создание документа, а потом получение его же.
//        return null;
//    }
//
//    // Метод для вызова защищенных эндпоинтов
//    private DocumentServiceGrpc.DocumentServiceBlockingStub getAuthenticatedDocStub() {
//        if (jwtToken == null) throw new IllegalStateException("Not logged in");
//
//        // Добавляем заголовок Authorization: Bearer <token>
//        Metadata metadata = new Metadata();
//        metadata.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + jwtToken);
//
//        return io.grpc.stub.MetadataUtils.attachHeaders(docStub, metadata);
//    }
//
//    public Document createDocument(FileType type, AccessLevel access, Map<String, String> attrs) {
//        CreateDocumentRequest request = CreateDocumentRequest.newBuilder()
//                .setFileType(type)
//                .setAccessLevel(access)
//                .putAllInitialAttributes(attrs)
//                .build();
//
//        return getAuthenticatedDocStub().createDocument(request);
//    }
//
//    public Document getDocument(String id) {
//        return getAuthenticatedDocStub().getDocument(GetDocumentRequest.newBuilder().setId(id).build());
//    }
//
//    public void shutdown() {
//        channel.shutdown();
//    }
//}