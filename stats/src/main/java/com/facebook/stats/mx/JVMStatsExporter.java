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
package com.facebook.stats.mx;

import com.google.common.collect.ImmutableSet;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Exports JVM stats so that they are available as counter values in fb303 stats.
 * 
 */
public class JVMStatsExporter {
  private final Stats stats;
  private StatsNameBuilder statsNameBuilder;
  private static final String NAME_PREFIX = "jvm";
  private static final Set<? extends OpenType> NUMERIC_TYPES = new ImmutableSet.Builder<SimpleType>()
    .add(SimpleType.BYTE)
    .add(SimpleType.SHORT)
    .add(SimpleType.INTEGER)
    .add(SimpleType.LONG)
    .add(SimpleType.FLOAT)
    .add(SimpleType.DOUBLE)
    .build();

  /**
   * Creates an instance and exports the counters from the beans matching the supplied name
   * pattern.
   * 
   *
   * @param stats The stats instance to which all the dynamic counters will be registered.
   * @param statNamePattern A regex {@link java.util.regex.Pattern} that is matched against all
   * generated counter key names. Only the counters that match the supplied pattern are added to 
   * Stats. Note that all stats have the prefix <code>"jvm."</code>.
   * @param beanNamePatterns the object name patterns used to discover the Mbeans.
   * 
   * @throws JMException if the beanNamePattern is not a valid bean name pattern.
   * @throws java.util.regex.PatternSyntaxException if the syntax of statNamePattern is invalid
   * 
   * @see ObjectName for details on the syntax of beanNamePattern 
   */
  public JVMStatsExporter(
    Stats stats,
    String statNamePattern,
    String... beanNamePatterns
  ) throws JMException {
    this(stats, patternFilter(Pattern.compile(statNamePattern)), beanNamePatterns);
  }

  /**
   * Creates an instance and exports the counters from the beans matching the supplied name
   * pattern.
   *
   *
   * @param stats The stats instance to which all the dynamic counters will be registered.
   * @param statNameBuilder A name builder {@link StatsNameBuilder} that is applied to all
   * discovered MBeans.
   * @param beanNamePatterns the object name patterns used to discover the Mbeans.
   *
   * @throws JMException if the beanNamePattern is not a valid bean name pattern.
   *
   * @see ObjectName for details on the syntax of beanNamePattern
   */
  public JVMStatsExporter(
    Stats stats,
    StatsNameBuilder statsNameBuilder,
    String... beanNamePatterns
  ) throws JMException {
    this.stats = stats;
    this.statsNameBuilder = statsNameBuilder;
    for (String beanNamePattern : beanNamePatterns) {
      exportNumericAttributes(new ObjectName(beanNamePattern));
    }
  }

  /**
   * Creates an instance and exports all the counters from all the platform beans.
   * 
   * @param stats The stats instance to which all dynamic counters will be registered.
   * @throws JMException unexpected, indicates a coding/error bug.
   */
  public JVMStatsExporter(Stats stats) throws JMException {
    this(stats, ".*", "java.lang:type=*,*");
  }

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public static Stats createAndBindTo(Stats stats) throws JMException {
    new JVMStatsExporter(stats);

    return stats;
  }

  /**
   * Exports all the numeric attributes of beans matching the supplied MXBean name pattern.
   * Also, discovers the numeric properties of the attributes of type CompositeData and registers
   * them as well.
   * 
   * @param beanNamePattern the bean name pattern used to discover the beans.
   * @see javax.management.ObjectName for bean name pattern syntax
   * @see java.lang.management for the list of java platform mbeans
   * 
   * @throws JMException if there are errors querying MBeans or information on them
   */
  public void exportNumericAttributes(ObjectName beanNamePattern) throws JMException {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectName> beanNames = beanServer.queryNames(beanNamePattern, null);
    
    // Iterate through all beans and register their numeric properties with Stats
    for (ObjectName beanName : beanNames) {
      MBeanInfo beanInfo = beanServer.getMBeanInfo(beanName);
      
      // Iterate through all bean attributes
      for (MBeanAttributeInfo attributeInfo : beanInfo.getAttributes()) {
        if (attributeInfo.isReadable()) {
          // Figure out the open type for the attribute
          OpenType<?> openType = null;
          if (attributeInfo instanceof OpenMBeanAttributeInfo) {
            openType = ((OpenMBeanAttributeInfo) attributeInfo).getOpenType();
          } else {
            // Sometimes the open mbean info is available in the descriptor
            Object obj = attributeInfo.getDescriptor().getFieldValue("openType");
            if (obj != null && obj instanceof OpenType) {
              openType = (OpenType) obj;
            }
          }
          // once the open type is found, figure out if it's a numeric or composite type
          if (openType != null) {
            if (NUMERIC_TYPES.contains(openType)){
              // numeric attribute types are registered with callbacks that simply 
              // return their value 
              Optional<String> name = statsNameBuilder.name(
                beanName, attributeInfo.getName(), null
              );
              if (name.isPresent()) {
                stats.addDynamicCounter(
                  name.get(),
                  new MBeanLongAttributeFetcher(beanServer, beanName, attributeInfo.getName()));
              }
            } else if (openType instanceof CompositeType) {
              // for composite types, we figure out which properties of the composite type 
              // are numeric and register callbacks to fetch those composite type attributes
              CompositeType compositeType = (CompositeType) openType;
              
              for (String key : compositeType.keySet()) {
                if (NUMERIC_TYPES.contains(compositeType.getType(key))) {
                  Optional<String> name = statsNameBuilder.name(
                    beanName, attributeInfo.getName(), key
                  );
                  if (name.isPresent()) {
                    stats.addDynamicCounter(
                      name.get(),
                      new MBeanLongCompositeValueFetcher(
                        beanServer, beanName, attributeInfo.getName(), key
                      )
                    );
                  }
                }
              }
            }
          }
        }  
      }
    }
  }

  private static StatsNameBuilder patternFilter(Pattern pattern) {
    return (bean, attribute, key) -> {
      String name = getStatName(bean, attribute, key);

      return pattern.matcher(name).matches() ? Optional.of(name) : Optional.empty();
    };
  }

  /**
   * Returns a stat name for the given set of parameters.
   * 
   * @param beanName the MBean name to which the stat belongs
   * @param attributeNames the mbean attribute name followed by any nested attribute names for
   * composite types
   * 
   * @return the stat name
   */
  private static String getStatName(ObjectName beanName, String attributeName, String key) {
    StringBuilder builder = new StringBuilder(NAME_PREFIX);
    String value = beanName.getKeyProperty("type");
    if (value != null) {
      builder.append('.').append(value);
    }
    value = beanName.getKeyProperty("name");
    if (value != null) {
      builder.append('.').append(value);
    }
    if (attributeName != null) {
      builder.append('.').append(attributeName);
    }
    if (key != null) {
      builder.append('.').append(key);
    }
    return builder.toString().replace(' ', '_');
  }

  /**
   * A base class that fetches an mbean attribute value.
   */
  private static class MBeanAttributeFetcher {
    private final MBeanServer mBeanServer;
    private final ObjectName beanName;
    private final String attribute;

    protected MBeanAttributeFetcher(MBeanServer mBeanServer, 
                                    ObjectName beanName, 
                                    String attribute) {
      this.mBeanServer = mBeanServer;
      this.beanName = beanName;
      this.attribute = attribute;
    }

    protected Object getAttributeValue() throws Exception {
      return mBeanServer.getAttribute(beanName, attribute);
    }
  }

  /**
   * Fetches attribute value as long
   */
  private static class MBeanLongAttributeFetcher 
    extends MBeanAttributeFetcher 
    implements Callable<Long> {
    private MBeanLongAttributeFetcher(
        MBeanServer mBeanServer, ObjectName beanName, String attribute) {
      super(mBeanServer, beanName, attribute);
    }

    @Override
    public Long call() throws Exception {
      Object obj = getAttributeValue();
      if(obj instanceof Number) {
        return ((Number)obj).longValue();
      }
      return -1L;
    }
  }

  /**
   * Fetches an attribute of an mbean attribute that is of composite type.
   */
  private static class MBeanLongCompositeValueFetcher 
      extends MBeanAttributeFetcher implements Callable<Long> {
    private final String itemName;

    private MBeanLongCompositeValueFetcher(
        MBeanServer mBeanServer, ObjectName beanName, String attribute, String itemName) {
      super(mBeanServer, beanName, attribute);
      this.itemName = itemName;
    }

    @Override
    public Long call() throws Exception {
      Object obj = getAttributeValue();
      if(obj instanceof CompositeData) {
        obj = ((CompositeData)obj).get(itemName);
        if (obj instanceof Number) {
          return ((Number)obj).longValue();
        }
      }
      return -1L;
    }
  }
}
