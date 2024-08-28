/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.onap.config.test;

import static org.onap.config.util.TestUtil.validateConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.config.util.TestUtil;

/**
 * Created by ARR on 10/13/2016.
 * Scenario 1
 * Validate configuration with Java Properties file format with mode
 */
class JavaPropertiesConfigTest {

    private static final String NAMESPACE = "javaProperties";

    @BeforeEach
    public void setUp() throws Exception {
        TestUtil.cleanUp();
    }

    @Test
    void testConfigurationWithPropertiesFileFormat() {
        validateConfiguration(NAMESPACE);
    }


    @AfterEach
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
