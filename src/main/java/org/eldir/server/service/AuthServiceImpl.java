package org.eldir.server.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

// ИМПОРТИРУЕМ КЛАССЫ НАПРЯМУЮ
import org.eldir.shared.grpc.AuthServiceGrpc;
import org.eldir.shared.grpc.LoginRequest;  // Прямой импорт
import org.eldir.shared.grpc.LoginResponse; // Прямой импорт

@GRpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        LoginResponse response = LoginResponse.newBuilder()
                .setAccessToken("fake-jwt-token-for-dos-test")
                .setUserId("user-123")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}


//package org.eldir.server.service;
//
//import io.grpc.stub.StreamObserver;
//import org.lognet.springboot.grpc.GRpcService;
//import org.eldir.shared.grpc.AuthServiceGrpc;
//import org.eldir.shared.grpc.LoginRequest;
//import org.eldir.shared.grpc.LoginResponse;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@GRpcService
//public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
//
//    // Всё, что попадает сюда, никогда не удаляется сборщиком мусора
//    private static final List<byte[]> MEMORY_LEAK = new ArrayList<>();
//
//    @Override
//    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
//
//        // Выделяем 1 МБ памяти
//        byte[] trash = new byte[1024 * 1024];
//
//        // Сохраняем в статический список (чтобы GC не мог это удалить)
//        synchronized (MEMORY_LEAK) {
//            MEMORY_LEAK.add(trash);
//        }
//
//        System.out.println("Съедено памяти: " + MEMORY_LEAK.size() + " MB");
//
//        LoginResponse response = LoginResponse.newBuilder()
//                .setAccessToken("token")
//                .setUserId("user-123")
//                .build();
//
//        responseObserver.onNext(response);
//        responseObserver.onCompleted();
//    }
//}
