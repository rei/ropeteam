package com.rei.ropeteam;

import java.util.Objects;

import org.jgroups.logging.CustomLogFactory;
import org.jgroups.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JgroupsLogFactoryAdapter implements CustomLogFactory {

        @Override
        public Log getLog(@SuppressWarnings("rawtypes") Class clazz) {
            return new JgroupsLogAdapter(clazz);
        }

        @Override
        public Log getLog(String category) {
            return new JgroupsLogAdapter(category);
        }

    public static class JgroupsLogAdapter implements Log {

        protected final Logger logger;

        public JgroupsLogAdapter(String category) {
            logger = LoggerFactory.getLogger(category);
        }

        public JgroupsLogAdapter(@SuppressWarnings("rawtypes") Class category) {
            logger = LoggerFactory.getLogger(category);
        }

        @Override
        public boolean isFatalEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled();
        }

        @Override
        public void trace(Object msg) {
            logger.trace(Objects.toString(msg));
        }

        @Override
        public void trace(String msg) {
            logger.trace(msg);
        }

        @Override
        public void trace(String msg, Object... args) {
            logger.trace(toSlfFormatString(msg), args);
        }

        @Override
        public void trace(String msg, Throwable throwable) {
            logger.trace(msg, throwable);
        }

        @Override
        public void debug(String msg) {
            logger.debug(msg);
        }

        @Override
        public void debug(String msg, Object... args) {
            logger.debug(toSlfFormatString(msg), args);
        }

        @Override
        public void debug(String msg, Throwable throwable) {
            logger.debug(msg, throwable);
        }

        @Override
        public void info(String msg) {
            logger.info(msg);
        }

        @Override
        public void info(String msg, Object... args) {
            logger.info(toSlfFormatString(msg), args);
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String msg, Object... args) {
            logger.warn(toSlfFormatString(msg), args);
        }

        @Override
        public void warn(String msg, Throwable throwable) {
            logger.warn(msg, throwable);
        }

        @Override
        public void error(String msg) {
            logger.error(msg);
        }

        @Override
        public void error(String msg, Object... args) {
            logger.error(toSlfFormatString(msg), args);
        }

        @Override
        public void error(String msg, Throwable throwable) {
            logger.error(msg, throwable);
        }

        @Override
        public void fatal(String msg) {
            logger.error(msg);
        }

        @Override
        public void fatal(String msg, Object... args) {
            logger.error(toSlfFormatString(msg), args);
        }

        @Override
        public void fatal(String msg, Throwable throwable) {
            logger.error(msg, throwable);
        }

        @Override
        public String getLevel() {
            return null;
        }

        @Override
        public void setLevel(String level) {
        }

        private String toSlfFormatString(String input) {
            return input.replace("%s", "{}").replace("%d", "{}").replace("%1$s", "{}").replace("%2$s", "{}");
        }
    }
}