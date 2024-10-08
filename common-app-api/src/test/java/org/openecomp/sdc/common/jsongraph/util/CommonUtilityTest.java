/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
 */
package org.openecomp.sdc.common.jsongraph.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.jsongraph.util.CommonUtility.LogLevelEnum;
import commonapplogging.src.main.java.org.openecomp.sdc.common.log.wrappers.Logger;


@RunWith(MockitoJUnitRunner.class)
public class CommonUtilityTest {

    @Mock
    private Logger logger;

    private static final String ARGUMENT1 = "ARGUMENT1";
    private static final String ARGUMENT2 = "ARGUMENT2";
    private static final String ARGUMENT3 = "ARGUMENT3";
    private static final String FORMAT = "FORMAT";

    @Test
    public void shouldLogError() {
        Mockito.when(logger.isErrorEnabled()).thenReturn(true);
        CommonUtility.addRecordToLog(logger, LogLevelEnum.ERROR, FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
        Mockito.verify(logger).error(FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
    }

    @Test
    public void shouldLogDebug() {
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        CommonUtility.addRecordToLog(logger, LogLevelEnum.DEBUG, FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
        Mockito.verify(logger).debug(FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
    }

    @Test
    public void shouldLogInfo() {
        Mockito.when(logger.isInfoEnabled()).thenReturn(true);
        CommonUtility.addRecordToLog(logger, LogLevelEnum.INFO, FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
        Mockito.verify(logger).info(FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
    }

    @Test
    public void shouldLogTrace() {
        Mockito.when(logger.isTraceEnabled()).thenReturn(true);
        CommonUtility.addRecordToLog(logger, LogLevelEnum.TRACE, FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
        Mockito.verify(logger).trace(FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
    }

    @Test
    public void shouldLogWarning() {
        Mockito.when(logger.isWarnEnabled()).thenReturn(true);
        CommonUtility.addRecordToLog(logger, LogLevelEnum.WARNING, FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
        Mockito.verify(logger).warn(FORMAT, ARGUMENT1, ARGUMENT2, ARGUMENT3);
    }
}