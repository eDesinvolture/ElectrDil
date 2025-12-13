package org.eldir.server.api.grpc;

import io.grpc.stub.StreamObserver;
import org.eldir.server.entity.User;
import org.eldir.server.repository.UserRepository;
import org.eldir.shared.grpc.*; // Импорты proto
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@GRpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    public UserGrpcService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        // В идеале тут проверить, что вызывающий - админ. Но пока просто отдаем список.
        var users = userRepository.findAll().stream()
                .map(u -> UserDto.newBuilder()
                        .setId(u.getId().toString())
                        .setLogin(u.getLogin())
                        .setClearanceLevel(u.getClearanceLevel())
                        .build())
                .collect(Collectors.toList());

        responseObserver.onNext(ListUsersResponse.newBuilder().addAllUsers(users).build());
        responseObserver.onCompleted();
    }

    @Override
    @Transactional
    public void updateUserClearance(UpdateUserClearanceRequest request, StreamObserver<UpdateUserClearanceResponse> responseObserver) {
        try {
            User user = userRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setClearanceLevel(request.getNewClearanceLevel());
            userRepository.save(user);

            responseObserver.onNext(UpdateUserClearanceResponse.newBuilder().setSuccess(true).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}