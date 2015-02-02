/*
 * Copyright (C) 2015 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.logging;

import org.slf4j.Marker;

/**
 * Adapter that forwards slf4j calls to an underlying Logger.
 */
public class Slf4jLoggerAdapter implements org.slf4j.Logger {

    private final com.facebook.logging.Logger fbLogger;

    public Slf4jLoggerAdapter(com.facebook.logging.Logger fbLogger) {
        this.fbLogger = fbLogger;
    }

    @Override
    public String getName() {
        return fbLogger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return fbLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        fbLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        fbLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        fbLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        fbLogger.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        fbLogger.trace(t, msg);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return fbLogger.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String msg) {
        fbLogger.trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        fbLogger.trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        fbLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object[] argArray) {
        fbLogger.trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        fbLogger.trace(t, msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return fbLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        fbLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        fbLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        fbLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        fbLogger.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        fbLogger.debug(t, msg);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return fbLogger.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String msg) {
        fbLogger.debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        fbLogger.debug(format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        fbLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object[] argArray) {
        fbLogger.debug(format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        fbLogger.debug(t, msg);
    }

    @Override
    public boolean isInfoEnabled() {
        return fbLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        fbLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        fbLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        fbLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        fbLogger.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        fbLogger.info(t, msg);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return fbLogger.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        fbLogger.info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        fbLogger.info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        fbLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object[] argArray) {
        fbLogger.info(format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        fbLogger.info(t, msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return fbLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        fbLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        fbLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        fbLogger.warn(format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        fbLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        fbLogger.warn(t, msg);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return fbLogger.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String msg) {
        fbLogger.warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        fbLogger.warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        fbLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object[] argArray) {
        fbLogger.warn(format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        fbLogger.warn(t, msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return fbLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        fbLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        fbLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        fbLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        fbLogger.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        fbLogger.error(t, msg);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return fbLogger.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String msg) {
        fbLogger.error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        fbLogger.error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        fbLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object[] argArray) {
        fbLogger.error(format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        fbLogger.error(t, msg);
    }
}
