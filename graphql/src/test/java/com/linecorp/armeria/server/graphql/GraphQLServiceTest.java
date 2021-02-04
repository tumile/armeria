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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import com.linecorp.armeria.client.WebClient;
import com.linecorp.armeria.common.MediaType;
import com.linecorp.armeria.server.ServerBuilder;
import com.linecorp.armeria.testing.junit5.server.ServerExtension;

class GraphQLServiceTest {

    private static final GraphQL graphQL;

    static {
        final DataFetcher<Project> dataFetcher = env -> {
            String name = env.getArgument("name");
            return new Project(name, "OSS");
        };
        final RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                                                  .type("Query", typeWiring ->
                                                          typeWiring.dataFetcher("project", dataFetcher))
                                                  .build();
        final File schemaFile = new File(GraphQLServiceTest.class
                                                 .getClassLoader()
                                                 .getResource("schema.graphqls")
                                                 .getFile());
        final TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaFile);
        final GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    @RegisterExtension
    static final ServerExtension server = new ServerExtension() {
        @Override
        protected void configure(ServerBuilder sb) {
            sb.annotatedService("/graphql", GraphQLService.of(graphQL));
        }
    };

    @Test
    void graphQLGet() {
        final String resp = WebClient.of(server.httpUri())
                                     .prepare().get("/graphql")
                                     .queryParam("query", "{project(name:\"Armeria\"){name,type}}")
                                     .execute()
                                     .aggregate().join().contentUtf8();
        assertThat(resp).isEqualTo("{\"data\":{\"project\":{\"name\":\"Armeria\",\"type\":\"OSS\"}}}");
    }

    @Test
    void graphQLPost() {
        final String body = "{\"query\":\"{project(name:\\\"Armeria\\\"){name,type}}\"}";
        final String resp = WebClient.of(server.httpUri())
                                     .prepare().post("/graphql")
                                     .content(MediaType.JSON, body)
                                     .execute()
                                     .aggregate().join().contentUtf8();
        assertThat(resp).isEqualTo("{\"data\":{\"project\":{\"name\":\"Armeria\",\"type\":\"OSS\"}}}");
    }

    private static class Project {
        String name;
        String type;

        Project(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }
}