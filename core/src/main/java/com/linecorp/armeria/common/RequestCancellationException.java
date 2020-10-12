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

package com.linecorp.armeria.common;

/**
 * A {@link RuntimeException} raised when a request is cancelled by the user.
 */
public final class RequestCancellationException extends RuntimeException {

    // TODO(trustin): Clean up the type hierarchy between RequestCancellationException,
    //                TimeoutException, RequestTimeoutException and ResponseTimeoutException.

    private static final long serialVersionUID = -8891853443874862294L;

    private static final RequestCancellationException INSTANCE = new RequestCancellationException(false);

    /**
     * Returns a singleton {@link RequestCancellationException}.
     */
    public static RequestCancellationException get() {
        return Flags.verboseExceptionSampler().isSampled(RequestCancellationException.class) ?
                new RequestCancellationException() : INSTANCE;
    }

    private RequestCancellationException() {}

    private RequestCancellationException(@SuppressWarnings("unused") boolean dummy) {
        super(null, null, false, false);
    }
}
