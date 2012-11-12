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
package com.facebook.util.digest;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Random;

/**
 * Murmur Hash 3 from http://sites.google.com/site/murmurhash/
 */
public class TestMurmurHash {
  private MurmurHash repeatableHasher;
  private Random random;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    repeatableHasher = MurmurHash.createRepeatableHasher();
    random = new Random(0);
  }

  @Test(groups = "fast")
  public void testKnownValues() throws Exception {
    Assert.assertEquals(repeatableHasher.hash(-4962768465676381896L), 7773119132477651622L);
    Assert.assertEquals(repeatableHasher.hash(4437113781045784766L), 6757418645758543340L);
    Assert.assertEquals(repeatableHasher.hash(-6688467811848818630L), -8604718945125690697L);
    Assert.assertEquals(repeatableHasher.hash(-8292973307042192125L), -1755701397742413297L);
    Assert.assertEquals(repeatableHasher.hash(-7423979211207825555L), 5774611486760861830L);
    Assert.assertEquals(repeatableHasher.hash(6146794652083548235L), 8757784006236800338L);
    Assert.assertEquals(repeatableHasher.hash(7105486291024734541L), -6569813424171585058L);
    Assert.assertEquals(repeatableHasher.hash(-279624296851435688L), 7632093365049823438L);
    Assert.assertEquals(repeatableHasher.hash(-2228689144322150137L), 497800614768617723L);
    Assert.assertEquals(repeatableHasher.hash(-1083761183081836303L), 2915145328950419925L);
    Assert.assertEquals(repeatableHasher.hash(5072005423257391728L), -7773107499203625692L);
    Assert.assertEquals(repeatableHasher.hash(2377732757510138102L), 6974945548766164881L);
    Assert.assertEquals(repeatableHasher.hash(2704323167362897208L), 7161717917719587664L);
    Assert.assertEquals(repeatableHasher.hash(428667830982598836L), 2683109897113636060L);
    Assert.assertEquals(repeatableHasher.hash(-8361175665883705505L), -8409675042511862740L);
    Assert.assertEquals(repeatableHasher.hash(-655101936082782086L), -2092458593058694154L);
    Assert.assertEquals(repeatableHasher.hash(1927512926176735975L), -3887252205803619106L);
    Assert.assertEquals(repeatableHasher.hash(-6914829020992303508L), -3869316830942580212L);
    Assert.assertEquals(repeatableHasher.hash(7577852396602278602L), 3237828910232585790L);
    Assert.assertEquals(repeatableHasher.hash(-4126310024944755050L), -3482777379146282878L);
    Assert.assertEquals(repeatableHasher.hash(-171134583860878546L), -6694962879312959976L);
    Assert.assertEquals(repeatableHasher.hash(8987859488885190724L), 2897511040551861668L);
    Assert.assertEquals(repeatableHasher.hash(-4681020001986070314L), 2061550936700009985L);
    Assert.assertEquals(repeatableHasher.hash(-4922475540349336432L), -2709147105248819624L);
    Assert.assertEquals(repeatableHasher.hash(-3370274031255729188L), 5031958990353962034L);
  }

  @Test(groups = "fast")
  public void testPumaAndGuavaLongMurmurHash() throws Exception {
    // our impl is slightly faster, and this is just a check to see we match guava
    HashFunction function = Hashing.murmur3_128((int) MurmurHash.JCOMMON_SEED);
    int numToCheck = 10000;

    for (int i = 0; i < numToCheck; i++) {
      long input = random.nextLong();

      Assert.assertEquals(repeatableHasher.hash(input), function.hashLong(input).asLong());
    }

  }
}
