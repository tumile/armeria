/*
 * Copyright 2020 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.server.graphql;

import java.util.Map;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.reactivestreams.Publisher;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.linecorp.armeria.common.HttpData;
import com.linecorp.armeria.common.HttpHeaders;
import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.common.ResponseHeaders;
import com.linecorp.armeria.internal.server.ResponseConversionUtil;
import com.linecorp.armeria.internal.server.annotation.AnnotatedService;
import com.linecorp.armeria.server.ServiceRequestContext;
import com.linecorp.armeria.server.annotation.Get;
import com.linecorp.armeria.server.annotation.Header;
import com.linecorp.armeria.server.annotation.Param;
import com.linecorp.armeria.server.annotation.Post;
import com.linecorp.armeria.server.annotation.ProducesJson;

/**
 * An {@link AnnotatedService} that handles <a href="https://graphql.org">GraphQL</a> requests.
 */
public final class GraphQLService {

    private static final ResponseHeaders OK = ResponseHeaders.of(200);
    private static final HttpHeaders EMPTY_TRAILERS = HttpHeaders.of();
    private static final TypeReference<Map<String, Object>> STRING_MAP =
            new TypeReference<Map<String, Object>>() {
            };

    /**
     * Creates a new {@link GraphQLService}.
     */
    public static GraphQLService of(GraphQL graphQL) {
        return new GraphQLService(graphQL);
    }

    private final GraphQL graphQL;
    private final ObjectMapper mapper;
    @Nullable
    private BiFunction<ServiceRequestContext, ExecutionInput, ExecutionInput> onRequest;
    @Nullable
    private BiFunction<ServiceRequestContext, ExecutionResult, ExecutionResult> onResult;

    private GraphQLService(GraphQL graphQL) {
        this.graphQL = graphQL;
        mapper = new ObjectMapper();
    }

    /**
     * Accepts GraphQL GET requests.
     */
    @Get
    @ProducesJson
    public HttpResponse graphQLGet(ServiceRequestContext context,
                                   @Param("query") String query,
                                   @Param("operationName") @Nullable String operationName,
                                   @Param("variables") @Nullable String variables) {
        return execute(context, new GraphQLRequest(query, operationName, parseVariables(variables)));
    }

    /**
     * Accepts GraphQL POST requests.
     */
    @Post
    @ProducesJson
    public HttpResponse graphQLPost(ServiceRequestContext context,
                                    @Header("Content-Type") String mediaType,
                                    @Param("query") @Nullable String query,
                                    @Param("operationName") @Nullable String operationName,
                                    @Param("variables") @Nullable String variables,
                                    String body) {
        if (MediaType.JSON.toString().equals(mediaType)) {
            try {
                return execute(context, mapper.readValue(body, GraphQLRequest.class));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Body must be JSON-encoded");
            }
        }
        if (query != null) {
            return execute(context, new GraphQLRequest(query, operationName, parseVariables(variables)));
        }
        if (MediaType.GRAPHQL.toString().equals(mediaType)) {
            return execute(context, new GraphQLRequest(body, null, null));
        }
        return HttpResponse.of(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Nullable
    private Map<String, Object> parseVariables(@Nullable String variables) {
        if (variables == null) {
            return null;
        }
        try {
            return mapper.readValue(variables, STRING_MAP);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Variables must be JSON-encoded");
        }
    }

    private HttpResponse execute(ServiceRequestContext context, GraphQLRequest request) {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                                                      .query(request.getQuery())
                                                      .operationName(request.getOperationName())
                                                      .variables(request.getVariables())
                                                      .build();
        if (onRequest != null) {
            executionInput = onRequest.apply(context, executionInput);
        }
        final ExecutionResult executionResult = graphQL.execute(executionInput);

        if (executionResult.isDataPresent() && executionResult.getData() instanceof Publisher) {
            final Publisher<ExecutionResult> resultPublisher = executionResult.getData();
            return ResponseConversionUtil.streamingFrom(resultPublisher, OK, EMPTY_TRAILERS,
                                                        result -> resultToData(context, result));
        }
        return HttpResponse.of(OK, resultToData(context, executionResult));
    }

    private HttpData resultToData(ServiceRequestContext context, ExecutionResult result) {
        if (onResult != null) {
            result = onResult.apply(context, result);
        }
        final Map<String, Object> data = result.toSpecification();
        try {
            return HttpData.ofUtf8(mapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
