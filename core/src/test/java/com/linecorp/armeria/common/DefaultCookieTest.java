/*
 * Copyright 2019 LINE Corporation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class DefaultCookieTest {

    @Test
    void toBuilder() {
        final Cookie cookie = Cookie.builder("a", "b")
                                    .domain("c")
                                    .path("/d")
                                    .maxAge(1)
                                    .httpOnly(true)
                                    .secure(true)
                                    .sameSite("Strict")
                                    .valueQuoted(true)
                                    .build();
        assertThat(cookie.toBuilder().build()).isEqualTo(cookie);
        assertThat(cookie.isExpired()).isFalse();
        await().pollDelay(Duration.ofSeconds(1)).until(() -> true);
        assertThat(cookie.isExpired()).isTrue();
    }

    @Test
    void mutation() {
        final Cookie cookie = Cookie.of("a", "b").withMutations(mutator -> mutator.name("c").value("d"));
        assertThat(cookie).isEqualTo(Cookie.of("c", "d"));
    }
}
