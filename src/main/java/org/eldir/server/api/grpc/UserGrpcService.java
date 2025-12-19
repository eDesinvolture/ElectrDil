package org.eldir.server.api.grpc;

import io.grpc.stub.StreamObserver;
import org.eldir.server.entity.User;
import org.eldir.server.repository.UserRepository;
import org.eldir.shared.grpc.*;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@GRpcService
@Profile("server")
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;  // ИСПРАВЛЕНО

    public UserGrpcService(UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {  // ИСПРАВЛЕНО
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
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

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        try {
            if (userRepository.findByLogin(request.getLogin()).isPresent()) {
                throw new RuntimeException("Пользователь с таким логином уже существует");
            }

            User newUser = User.builder()
                    .login(request.getLogin())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .clearanceLevel(request.getClearanceLevel())
                    .ipAddress("127.0.0.1")
                    .build();

            userRepository.save(newUser);

            CreateUserResponse response = CreateUserResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Пользователь успешно создан")
                    .setUserId(newUser.getId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}