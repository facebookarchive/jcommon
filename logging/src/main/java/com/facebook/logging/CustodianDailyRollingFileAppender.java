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
package com.facebook.logging;

import com.facebook.logging.log4j.CustodianLog4JDailyRollingFileAppender;

/**
 * @deprecated Use the parent class directly in logging-log4j artifact instead.
 * This class is provided here to preserve full backwards compatibility of the maven module.
 */
@Deprecated
public class CustodianDailyRollingFileAppender extends CustodianLog4JDailyRollingFileAppender
{
}
