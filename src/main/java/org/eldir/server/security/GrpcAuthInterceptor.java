package org.eldir.server.security;

import io.grpc.*;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@GRpcGlobalInterceptor
@Order(1)
public class GrpcAuthInterceptor implements ServerInterceptor {

    private final JwtService jwtService;
    public static final Context.Key<String> USER_LOGIN_KEY = Context.key("user_login");

    public GrpcAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();

        if (methodName.toLowerCase().contains("authservice")) {
            return next.startCall(call, headers);
        }

        String token = null;
        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        String authHeader = headers.get(authKey);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token != null && jwtService.validateToken(token)) {
            String login = jwtService.extractLogin(token);

            Context context = Context.current().withValue(USER_LOGIN_KEY, login);
            return Contexts.interceptCall(context, call, headers, next);
        }

        call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing JWT token"), headers);
        return new ServerCall.Listener<>() {};
    }
}