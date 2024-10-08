/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2020 Nordix Foundation
 * ================================================================================
 */
package org.openecomp.sdc.common.log.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import commonapplogging.src.main.java.org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;

public class EcompLoggerErrorCodeTest {

    @Test
    public void testEnumValues() {
        for (final Object value : EcompLoggerErrorCode.values()) {
            assertThat(value).isNotNull().isInstanceOf(EcompLoggerErrorCode.class);
        }
    }

    @Test
    public void testGetByValue() {
        for (final EcompLoggerErrorCode value : EcompLoggerErrorCode.values()) {
            assertThat(EcompLoggerErrorCode.getByValue(value.name())).isNotNull()
                .isInstanceOf(EcompLoggerErrorCode.class);
        }
    }
}
