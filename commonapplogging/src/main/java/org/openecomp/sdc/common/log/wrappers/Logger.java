package commonapplogging.src.main.java.org.openecomp.sdc.common.log.wrappers;
        

import com.google.common.annotations.VisibleForTesting;
import java.util.UUID;
import java.util.Map;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.elements.ErrorLogOptionalData;
import org.openecomp.sdc.common.log.elements.LoggerDebug;
import org.openecomp.sdc.common.log.elements.LoggerError;
import org.openecomp.sdc.common.log.elements.LoggerFactory;
import org.openecomp.sdc.common.log.elements.LoggerMetric;
import org.openecomp.sdc.common.log.elements.LoggerSecurity;
import org.openecomp.sdc.common.log.enums.EcompErrorSeverity;
import org.openecomp.sdc.common.log.enums.LogLevel;
import commonapplogging.src.main.java.org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.slf4j.Marker;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.Optional;


/**
 * This class wraps {@link org.slf4j.Logger} object and provides mandatory information required by Ecomp logging rules. Note: All deprecated methods
 * are supported to be compatible to the legacy code and have not be used by the new code
 */

public class Logger implements org.slf4j.Logger {

    private final LoggerDebug debug;
    private final LoggerError error;
    private final LoggerMetric metric;
    private final LoggerSecurity security;
    private final org.slf4j.Logger logger;
    private static String logLevel = null;
    private final String serviceLogbackFile = "/app/jetty/config/catalog-be/logback.xml";
    private final String onboardingLogbackFile = "/app/jetty/config/onboarding-be/logback.xml";
    private final String SECURITY = "SECURITY";


    @VisibleForTesting
    public Logger(org.slf4j.Logger logger) {
        this.logger = logger;
        this.security = LoggerFactory.getMdcLogger(LoggerSecurity.class, logger);
        this.debug = LoggerFactory.getMdcLogger(LoggerDebug.class, logger);
        this.error = LoggerFactory.getMdcLogger(LoggerError.class, logger);
        this.metric = LoggerFactory.getMdcLogger(LoggerMetric.class, logger);
    }

    private Logger(String className) {
        this(org.slf4j.LoggerFactory.getLogger(className));
    }

    public static Logger getLogger(String className) {
        return new Logger(className);
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getName());
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isSecurityEnabled() {
        Optional<String> serviceLogLevel = Optional.ofNullable(setLogLevelGlobal(serviceLogbackFile));
        Optional<String> onboardingLogLevel = Optional.ofNullable(setLogLevelGlobal(onboardingLogbackFile));
        if ((serviceLogLevel.isPresent() && serviceLogLevel.get().equals(SECURITY)) ||
                (onboardingLogLevel.isPresent() && onboardingLogLevel.get().equals(SECURITY))) {
            return true;
        } else {
            return false;
        }
    }


    // For security logging - reads log level from logback.xml so it can be used by custom security logging methods
    public static String setLogLevelGlobal(String logbackPath) {
        Pattern pattern = Pattern.compile("<root\\s+level=\"(\\w+)\"");
        try (BufferedReader reader = new BufferedReader(new FileReader(logbackPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    logLevel = matcher.group(1);
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Logback configuration file: " + e.getMessage());
        }
        if (logLevel == null) {
            System.err.println("Log level not found in Logback configuration file.");
        }
        return logLevel;
    }


    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}  **/
    public void warn(String msg) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(String msg, Object o) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, o);
        }
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg);
        }
    }

    @Override
    public void info(String msg, Object o) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, o);
        }
    }

    @Override
    public void info(String msg, Object o, Object o1) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, o, o1);
        }
    }

    public void debug(String msg, Object... params) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, params);
        }
    }

    public void security(String msg, Object... params) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, params);
        }
    }

    public void metric(String msg, Object... params) {
        metric.log(LogLevel.INFO, msg, params);
    }

    public void invoke(String targetEntity, String targetServiceName, String serviceName, String msg, Object... params) {
        String invocationId = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        metric.startTimer().stopTimer().setOutgoingInvocationId(invocationId).setTargetServiceName(targetServiceName).setTargetEntity(targetEntity)
                .setStatusCode(ONAPLogConstants.ResponseStatus.COMPLETE.name()).setKeyRequestId(requestID).setServiceName(serviceName);
        metric.log(ONAPLogConstants.Markers.INVOKE, LogLevel.INFO, msg, params);
    }

    public void invokeReturn(String msg, Object... params) {
        try {
            metric.startTimer().stopTimer();
        } catch (Exception e) {
            error(e.getMessage(), e);
        }
        metric.log(ONAPLogConstants.Markers.INVOKE_RETURN, LogLevel.INFO, msg, params);
    }

    public void invokeSynchronous(String msg, Object... params) {
        metric.log(ONAPLogConstants.Markers.INVOKE_SYNCHRONOUS, LogLevel.INFO, msg, params);
    }

    @Override
    public void debug(String msg, Throwable throwable) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return false;
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object o) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, o);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object o, Object o1) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, o, o1);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object... objects) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, objects);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable throwable) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, throwable);
        }
    }

    public void debug(String message) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, message);
        }
    }

    @Override
    public void debug(String msg, Object o) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, o);
        }
    }

    @Override
    public void debug(String msg, Object o, Object o1) {
        if (isDebugEnabled()) {
            debug.log(LogLevel.DEBUG, msg, o, o1);
        }
    }

    public void trace(String message, Object... params) {
//        is messa is secuirty
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, message, params);
        }
    }


    public void security(String msg, Throwable throwable) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, throwable);
        }
    }

    public boolean isSecurityEnabled(Marker marker) {
        return false;
    }

    public void security(Marker marker, String msg) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg);
        }
    }

    public void security(Marker marker, String msg, Object o) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, o);
        }
    }

    public void security(Marker marker, String msg, Object o, Object o1) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, o, o1);
        }
    }

    public void security(Marker marker, String msg, Object... objects) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, objects);
        }
    }

    public void security(Marker marker, String msg, Throwable throwable) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, throwable);
        }
    }

    public void security(String message) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, message);
        }
    }

    public void security(String msg, Object o) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, o);
        }
    }

    public void security(String msg, Object o, Object o1) {
        if (isSecurityEnabled()) {
            security.log(LogLevel.SECURITY, msg, o, o1);
        }
    }

    @Override
    public void trace(String msg, Throwable throwable) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, throwable);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return false;
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object o) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, o);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object o, Object o1) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, o, o1);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object... objects) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, objects);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable throwable) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, throwable);
        }
    }

    public void trace(String msg) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg);
        }
    }

    @Override
    public void trace(String msg, Object o) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, o);
        }
    }

    @Override
    public void trace(String msg, Object o, Object o1) {
        if (isTraceEnabled()) {
            debug.log(LogLevel.TRACE, msg, o, o1);
        }
    }

    public void info(String msg, Object... params) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, params);
        }
    }

    @Override
    public void info(String msg, Throwable throwable) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return false;
    }

    @Override
    public void info(Marker marker, String msg) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object o) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, o);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object o, Object o1) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, o, o1);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object... objects) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, objects);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable throwable) {
        if (isInfoEnabled()) {
            debug.log(LogLevel.INFO, msg, throwable);
        }
    }

    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(String msg, Object... params) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, params);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(String msg, Object o, Object o1) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, o, o1);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(String msg, Throwable throwable) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, throwable);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return false;
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(Marker marker, String msg) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(Marker marker, String msg, Object o) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, o);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(Marker marker, String msg, Object o, Object o1) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, o, o1);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(Marker marker, String msg, Object... objects) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, objects);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #warn(EcompLoggerErrorCode, String, String)}   **/
    public void warn(Marker marker, String msg, Throwable throwable) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, msg, throwable);
        }
    }

    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(String msg, Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, params);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(String msg, Throwable throwable) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return false;
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(Marker marker, String msg) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(Marker marker, String msg, Object o) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, o);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(Marker marker, String msg, Object o, Object o1) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, o, o1);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(Marker marker, String msg, Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, params);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(Marker marker, String msg, Throwable throwable) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, throwable);
        }
    }

    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(String msg) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(String msg, Object o) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, o);
        }
    }

    @Override
    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, String)}  **/
    public void error(String msg, Object o, Object o1) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, msg, o, o1);
        }
    }

    /**
     * Writes out ERROR logging level message to the application error log
     *
     * @param errorLevel       code representing the error severity level
     * @param serviceName      name of the API invoked at the logging component
     * @param targetEntity     name of the ECOMP component or sub-component, or external entity at which the error occurred or null
     * @param errorDescription a human readable description of the error condition
     * @param params           optional parameters of a given error description
     */
    public void error(EcompErrorSeverity errorLevel, EcompLoggerErrorCode errorCodeEnum, String serviceName, String targetEntity,
                      String errorDescription, Object... params) {
        if (isErrorEnabled()) {
            error.log(errorLevel, errorCodeEnum, serviceName, targetEntity, errorDescription, params);
        }
    }

    /**
     * Writes out ERROR logging level message to the application error log
     *
     * @param errorCodeEnum    code representing the error condition
     * @param serviceName      name of the API invoked at the logging component
     * @param targetEntity     name of the ECOMP component or sub-component, or external entity at which the error occurred or null
     * @param errorDescription a human readable description of the error condition
     * @param params           optional parameters of a given error description
     */

    @Deprecated
    /** Please use method {@link #error(EcompLoggerErrorCode, String, ErrorLogOptionalData, String, Object...)}   **/
    public void error(EcompLoggerErrorCode errorCodeEnum,
                      String serviceName,
                      String targetEntity,
                      String errorDescription, Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, errorCodeEnum, serviceName, targetEntity, errorDescription, params);
        }
    }

    /* Writes out ERROR logging level message to the application error LOG
     * @param errorCodeEnum code representing the error condition
     * @param errorDescription a human readable description of the error condition
     * @param params optional parameters of a given error description
     */
    public void error(EcompLoggerErrorCode errorCodeEnum, String serviceName, String errorDescription, Object... params) {
        error(errorCodeEnum, serviceName, (String) null, errorDescription, params);
    }

    /**
     * Writes out ERROR logging level message to the application error log
     *
     * @param errorCodeEnum        code representing the error condition
     * @param serviceName          name of the API invoked at the logging component
     * @param errorLogOptionalData elements that contans all relevant data of the error
     * @param errorDescription     a human readable description of the error condition
     * @param params               optional parameters of a given error description
     */
    public void error(EcompLoggerErrorCode errorCodeEnum, String serviceName, ErrorLogOptionalData errorLogOptionalData, String errorDescription,
                      Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.ERROR, errorCodeEnum, serviceName, errorLogOptionalData, errorDescription, params);
        }
    }

    /**
     * Writes out WARN logging level message to the application error log
     *
     * @param errorCodeEnum    code representing the error condition
     * @param serviceName      name of the API invoked at the logging component
     * @param targetEntity     name of the ECOMP component or sub-component, or external entity at which the error occurred or null
     * @param errorDescription a human readable description of the error condition
     * @param params           optional parameters of a given error description
     */
    public void warn(EcompLoggerErrorCode errorCodeEnum, String serviceName, String targetEntity, String errorDescription, Object... params) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, errorCodeEnum, serviceName, targetEntity, errorDescription, params);
        }
    }

    /**
     * Writes out WARN logging level message to the application error log
     *
     * @param errorCodeEnum        code representing the error condition
     * @param serviceName          name of the API invoked at the logging component
     * @param errorLogOptionalData elements that contans all relevant data of the error
     * @param description          a human readable description of the error condition
     * @param params               optional parameters of a given error description
     */
    public void warn(EcompLoggerErrorCode errorCodeEnum, String serviceName, ErrorLogOptionalData errorLogOptionalData, String description,
                     Object... params) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, errorCodeEnum, serviceName, errorLogOptionalData, description, params);
        }
    }

    /**
     * Writes out WARN logging level message to the application error log
     *
     * @param errorCodeEnum    code representing the error condition
     * @param serviceName      name of the API invoked at the logging component
     * @param errorDescription a human readable description of the error condition
     * @param params           optional parameters of a given error description
     */
    public void warn(EcompLoggerErrorCode errorCodeEnum, String serviceName, String errorDescription, Object... params) {
        if (isWarnEnabled()) {
            error.log(LogLevel.WARN, errorCodeEnum, serviceName, (String) null, errorDescription, params);
        }
    }

    /**
     * Writes out FATAL logging level message to the application error log
     *
     * @param errorCodeEnum    code representing the error condition
     * @param serviceName      name of the API invoked at the logging component
     * @param targetEntity     name of the ECOMP component or sub-component, or external entity at which the error occurred or null
     * @param errorDescription a human readable description of the error condition
     * @param params           optional parameters of a given error description
     */
    public void fatal(EcompLoggerErrorCode errorCodeEnum, String serviceName, String targetEntity, String errorDescription, Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.FATAL, errorCodeEnum, serviceName, targetEntity, errorDescription, params);
        }
    }

    /**
     * Writes out FATAL logging level message to the application error log
     *
     * @param errorCodeEnum        code representing the error condition
     * @param serviceName          name of the API invoked at the logging component
     * @param errorLogOptionalData elements that contans all relevant data of the error
     * @param description          a human readable description of the error condition
     * @param params               optional parameters of a given error description
     */
    public void fatal(EcompLoggerErrorCode errorCodeEnum, String serviceName, ErrorLogOptionalData errorLogOptionalData, String description,
                      Object... params) {
        if (isErrorEnabled()) {
            error.log(LogLevel.FATAL, errorCodeEnum, serviceName, errorLogOptionalData, description, params);
        }
    }
}
