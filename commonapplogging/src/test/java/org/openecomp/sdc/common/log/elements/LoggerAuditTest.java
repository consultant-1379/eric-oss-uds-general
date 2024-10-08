package org.openecomp.sdc.common.log.elements;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.Severity;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static org.onap.logging.ref.slf4j.ONAPLogConstants.MDCs.*;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.*;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_CLASS_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_ELAPSED_TIME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_END_TIMESTAMP;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_PROCESS_KEY;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_REMOTE_HOST;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SERVER_IP_ADDRESS;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.common.log.elements.LogFieldsMdcHandler.hostAddress;
import commonapplogging.src.main.java.org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;

@RunWith(MockitoJUnitRunner.class)
public class LoggerAuditTest {
    @Mock
    private Logger logger;

    private LoggerAudit auditLog;

    @Before
    public void init() {
        auditLog = new LoggerAudit(LogFieldsMdcHandler.getInstance(), logger);
    }

    @After
    public void tearDown() {
        MDC.clear();
    }

    @Test
    public void whenNoAuditFieldsArePopulated_ShouldReturnAssertTrue_onMdcMap() {
        auditLog.clear()
                .log(LogLevel.INFO, "some error code");
        Assert.assertNotNull(MDC.get(SERVER_FQDN));
        Assert.assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
    }

    @Test
    public void whenAllAuditFieldsArePopulated_ShouldReturnAssertTrue_onEachMACField() throws UnknownHostException {

        String uuid = UUID.randomUUID().toString();
        String hostName = InetAddress.getByName(hostAddress).getCanonicalHostName();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        auditLog.clear()
                .startTimer()
                .stopTimer()
                .setKeyRequestId(uuid)
                .setInstanceUUID(INSTANCE_UUID)
                .setRemoteHost(MDC_REMOTE_HOST)
                .setServiceName(SERVICE_NAME)
                .setResponseCode(EcompLoggerErrorCode.DATA_ERROR)
                .setStatusCodeByResponseCode("201")
                .setResponseDesc(RESPONSE_DESCRIPTION)
                .setPartnerName(PARTNER_NAME)

                .setOptClassName(LoggerAuditTest.class.toString())
                .setOptAlertSeverity(Severity.CRITICAL)
                .setOptProcessKey(MDC_PROCESS_KEY)
                .setOptServiceInstanceId(MDC_SERVICE_INSTANCE_ID)
                .log(LogLevel.DEBUG, "");


        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(ENTRY_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_END_TIMESTAMP));
        Assert.assertFalse(LogFieldsMdcHandler.getInstance().isMDCParamEmpty(MDC_ELAPSED_TIME));

        Assert.assertEquals(MDC.get(MDC_SERVER_IP_ADDRESS), hostAddress);
        Assert.assertEquals(MDC.get(SERVER_FQDN), hostName);
        Assert.assertEquals(MDC.get(MDC_REMOTE_HOST), MDC_REMOTE_HOST);
        Assert.assertEquals(MDC.get(RESPONSE_STATUS_CODE), ONAPLogConstants.ResponseStatus.COMPLETE.name());

        Assert.assertEquals(MDC.get(REQUEST_ID), uuid);
        Assert.assertEquals(MDC.get(SERVICE_NAME), SERVICE_NAME);
        Assert.assertEquals(MDC.get(PARTNER_NAME), PARTNER_NAME);
        Assert.assertEquals(MDC.get(RESPONSE_CODE), String.valueOf(EcompLoggerErrorCode.DATA_ERROR.getErrorCode()));
        Assert.assertEquals(MDC.get(RESPONSE_DESCRIPTION), RESPONSE_DESCRIPTION);
        Assert.assertEquals(MDC.get(INSTANCE_UUID), INSTANCE_UUID);
        Assert.assertEquals(MDC.get(MDC_CLASS_NAME), LoggerAuditTest.class.toString());

        Assert.assertEquals(MDC.get(RESPONSE_SEVERITY), String.valueOf(Severity.CRITICAL.getSeverityType()));
        Assert.assertEquals(MDC.get(MDC_PROCESS_KEY), MDC_PROCESS_KEY);
        Assert.assertEquals(MDC.get(MDC_SERVICE_INSTANCE_ID), MDC_SERVICE_INSTANCE_ID);
    }
}
