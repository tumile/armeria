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

package com.linecorp.armeria.client.cookie;

import com.linecorp.armeria.common.Cookie;

/**
 * The possible states of a cookie in the jar.
 *
 * @see CookieJar#state(Cookie)
 */
public enum CookieState {
    /**
     * The cookie exists in the jar.
     */
    EXISTENT,
    /**
     * The cookie exists in the jar but is expired.
     */
    EXPIRED,
    /**
     * The cookie does not exist in the jar.
     */
    NON_EXISTENT
}
