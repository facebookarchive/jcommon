package com.facebook.config;

/**
 * Marker interface for objects that implement both Refreshable
 * and ConfigProvider
 */
public interface RefreshableConfigProvider extends Refreshable, ConfigProvider {
}
