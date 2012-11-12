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
package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;
import com.facebook.stats.topk.TopK;
import com.facebook.stats.topk.TreeBasedIntegerTopK;

public class TestTreeBasedIntegerTopK extends TestIntegerTopK {
  private static final Logger LOG = LoggerImpl.getLogger(TestTreeBasedIntegerTopK.class);

  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new TreeBasedIntegerTopK(keySpaceSize, k);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }
}
