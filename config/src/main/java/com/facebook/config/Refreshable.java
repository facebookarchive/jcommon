/*
 * Copyright (C) 2012 Facebook, Inc.
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
package com.facebook.config;

/**
 * object that supports the idea of being 'refreshed'.  
 * Example: a config file accessor that parses the file once and stores
 * it for access.  It may implement this interface so that it may be 
 * reloaded on-demand
 */
public interface Refreshable {
  public void refresh() throws ConfigException;
}
