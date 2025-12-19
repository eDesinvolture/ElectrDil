package org.eldir.server.api.grpc;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.eldir.server.entity.User;
import org.eldir.server.repository.UserRepository;
import org.eldir.server.security.JwtService;
import org.eldir.shared.grpc.AuthServiceGrpc;
import org.eldir.shared.grpc.LoginRequest;
import org.eldir.shared.grpc.LoginResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@GRpcService
@Profile("server")
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthGrpcService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            User user = userRepository.findByLogin(request.getLogin())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            String token = jwtService.generateToken(user.getLogin());

            LoginResponse response = LoginResponse.newBuilder()
                    .setAccessToken(token)
                    .setUserId(user.getId().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.UNAUTHENTICATED
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        }
    }
}