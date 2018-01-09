package com.goldtek.recognitionsvr;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Created by darwinhu on 2017/7/14.
 */

public class CLog {

    private static String TAG = "CLog";
    private Logger LOG;
    private String LOG_ConfigXML	= "";

    CLog(String szFullPathName, String LogLV){
        // inject the name of the current application as "application-name"
        // property of the LoggerContext
        Log.i(TAG, LogLV + ", " + szFullPathName);
        LOG = LoggerFactory.getLogger(TAG);

        LOG_ConfigXML 	 = ComposeLogConfig3(szFullPathName, LogLV);
        ConfigureLogbackByString(LOG_ConfigXML);
    }

    CLog(String szLogger){
        LOG = LoggerFactory.getLogger(szLogger);
    }

    /***
     * Log With rolling policy
     */
    private String ComposeLogConfig3(String szLogFullPath, String LogLV)
    {
        String szRet = "";
        // region Configuration
        szRet =  "<configuration>"
                + "<property name='LOG_PATH' value='" + szLogFullPath + "' />"
                + "<property name='LOG_LEVEL' value='" + LogLV + "' />"
                + "<property name='FILE_SIZE' value='10MB' />"
                + "<property name='TOTAL_CAP' value='11MB' />"

                + "<appender name='RecogSvr' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/RecogSvr.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                + "<fileNamePattern>${LOG_PATH}/Backup/RecogSvr.%d{yyyy-MM}.txt</fileNamePattern>"
                + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                + "</timeBasedFileNamingAndTriggeringPolicy>"
                + "<maxHistory>30</maxHistory>"
                + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<logger name='RecogSvr' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='RecogSvr' />"
                + "</logger>"

                + "</configuration>";
        // endregion
        return szRet;
    }

    /***
     * Log With rolling policy
     */
    private String ComposeLogConfig2(String szLogFullPath, String LogLV)
    {
        String szRet = "";
        // region Configuration
        szRet =  "<configuration>"
                + "<property name='LOG_PATH' value='" + szLogFullPath + "' />"
                + "<property name='LOG_LEVEL' value='" + LogLV + "' />"
                + "<property name='FILE_SIZE' value='10MB' />"
                + "<property name='TOTAL_CAP' value='11MB' />"

                + "<appender name='SrvRemote' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/SrvRemote.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                    + "<fileNamePattern>${LOG_PATH}/Backup/SrvRemote.%d{yyyy-MM}.txt</fileNamePattern>"
                    + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                    + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                    + "</timeBasedFileNamingAndTriggeringPolicy>"
                    + "<maxHistory>30</maxHistory>"
                    + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='SrvBackend' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/SrvBackend.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                    + "<fileNamePattern>${LOG_PATH}/Backup/SrvBackend.%d{yyyy-MM}.txt</fileNamePattern>"
                    + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                    + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                    + "</timeBasedFileNamingAndTriggeringPolicy>"
                    + "<maxHistory>30</maxHistory>"
                    + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='DBHelper' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/DBHelper.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                    + "<fileNamePattern>${LOG_PATH}/Backup/DBHelper.%d{yyyy-MM}.txt</fileNamePattern>"
                    + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                    + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                    + "</timeBasedFileNamingAndTriggeringPolicy>"
                    + "<maxHistory>30</maxHistory>"
                    + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='JSONPriority' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/JSONPriority.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                    + "<fileNamePattern>${LOG_PATH}/Backup/JSONPriority.%d{yyyy-MM}.txt</fileNamePattern>"
                    + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                    + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                    + "</timeBasedFileNamingAndTriggeringPolicy>"
                    + "<maxHistory>1</maxHistory>"
                    + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='JSON' class=\"ch.qos.logback.core.rolling.RollingFileAppender\">"
                + "<file>${LOG_PATH}/JSON.txt</file>"
                + "<rollingPolicy class=\"ch.qos.logback.core.rolling.TimeBasedRollingPolicy\">"
                    + "<fileNamePattern>${LOG_PATH}/Backup/JSON.%d{yyyy-MM}.txt</fileNamePattern>"
                    + "<timeBasedFileNamingAndTriggeringPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP\">"
                    + "<maxFileSize>${FILE_SIZE}</maxFileSize>"
                    + "</timeBasedFileNamingAndTriggeringPolicy>"
                    + "<maxHistory>1</maxHistory>"
                    + "<totalSizeCap>${TOTAL_CAP}</totalSizeCap>"
                + "</rollingPolicy>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<logger name='SrvRemote' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='SrvRemote' />"
                + "</logger>"

                + "<logger name='SrvBackend' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='SrvBackend' />"
                + "</logger>"

                + "<logger name='DBHelper' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='DBHelper' />"
                + "</logger>"

                + "<logger name='JSONPriority' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='JSONPriority' />"
                + "</logger>"

                + "<logger name='JSON' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='JSON' />"
                + "</logger>"

                + "</configuration>";
        // endregion
        return szRet;
    }

    private String ComposeLogConfig(String szLogFullPath, String LogLV)
    {
        String szRet = "";
        // region Configuration
        szRet =  "<configuration>"
                + "<property name='LOG_PATH' value='" + szLogFullPath + "' />"
                + "<property name='LOG_LEVEL' value='" + LogLV + "' />"

                + "<appender name='SrvRemote' class=\"ch.qos.logback.core.FileAppender\">"
                + "<file>${LOG_PATH}/SrvRemote.txt</file><append>true</append>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='SrvBackend' class=\"ch.qos.logback.core.FileAppender\">"
                + "<file>${LOG_PATH}/SrvBackend.txt</file><append>true</append>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<appender name='DBHelper' class=\"ch.qos.logback.core.FileAppender\">"
                + "<file>${LOG_PATH}/DBHelper.txt</file><append>true</append>"
                + "<encoder><pattern>%d{yyyy-MM-dd HH:mm:ss:SSS},  %-5p  %c{35} - %m%n</pattern></encoder>"
                + "</appender>"

                + "<logger name='SrvRemote' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='SrvRemote' />"
                + "</logger>"

                + "<logger name='SrvBackend' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='SrvBackend' />"
                + "</logger>"

                + "<logger name='DBHelper' level='${LOG_LEVEL}' >"
                + "<appender-ref ref='DBHelper' />"
                + "</logger>"

                + "</configuration>";
        // endregion
        return szRet;
    }

    /**
     * Resetting the default configuration write log
     * @param szConfigXML
     */
    private void ConfigureLogbackByString(String szConfigXML)
    {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
        lc.reset();

        JoranConfigurator config = new JoranConfigurator();
        config.setContext(lc);

        InputStream stream = new ByteArrayInputStream(szConfigXML.getBytes());
        try {
            config.doConfigure(stream);
        } catch (JoranException e) {
            e.printStackTrace();
        }
    }

    void info(String var1){
        LOG.info(var1);
    }

    void warn(String var1){
        LOG.warn(var1);
    }

    void error(String var1){
        LOG.error(var1);
    }

    void debug(String var1){
        LOG.debug(var1);
    }

}
