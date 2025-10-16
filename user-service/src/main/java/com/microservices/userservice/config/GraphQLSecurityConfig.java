package com.microservices.userservice.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

@Configuration
@Slf4j
public class GraphQLSecurityConfig {

    @Component
    public static class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

        @Override
        protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
            if (ex instanceof AccessDeniedException) {
                return GraphqlErrorBuilder.newError()
                        .errorType(ErrorType.FORBIDDEN)
                        .message("Access denied")
                        .path(env.getExecutionStepInfo().getPath())
                        .location(env.getField().getSourceLocation())
                        .build();
            } else if (ex instanceof AuthenticationCredentialsNotFoundException) {
                return GraphqlErrorBuilder.newError()
                        .errorType(ErrorType.UNAUTHORIZED)
                        .message("Authentication required")
                        .path(env.getExecutionStepInfo().getPath())
                        .location(env.getField().getSourceLocation())
                        .build();
            }
            return null;
        }
    }
}