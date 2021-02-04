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

import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

public final class GraphQLRequest {

    private String query;
    @Nullable
    private String operationName;
    @Nullable
    private Map<String, Object> variables;

    public GraphQLRequest() {}

    public GraphQLRequest(String query, @Nullable String operationName,
                          @Nullable Map<String, Object> variables) {
        this.query = query;
        this.operationName = operationName;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    @Nullable
    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables == null ? Collections.emptyMap() : variables;
    }
}
