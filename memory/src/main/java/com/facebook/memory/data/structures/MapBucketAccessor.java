package com.facebook.memory.data.structures;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.collections.ByteArray;


public class MapBucketAccessor implements BucketAccessor {
  private static final AtomicLong NEXT_ADDRESS = new AtomicLong(1L);

  final Map<Long, Bucket> bucketMap = Maps.newConcurrentMap();
  final Map<Long, ByteArray> addressToKey = Maps.newConcurrentMap();
  final Map<ByteArray, Long> keyToAddress = Maps.newConcurrentMap();
  final Map<ByteArray, Long> keyToCacheKey = Maps.newConcurrentMap();
  final Map<ByteArray, ByteArray> data = Maps.newConcurrentMap();

  @Override
  public Bucket create() {
    MapBucket mapBucket = new MapBucket(Integer.MAX_VALUE, NEXT_ADDRESS.getAndIncrement(), this);

    bucketMap.put(mapBucket.getAddress(), mapBucket);

    return mapBucket;
  }

  @Override
  public Bucket wrap(long address) {
    return bucketMap.get(address);
  }

  public ByteArrayAccessor createKeyAccessor() {
    return new ByteArrayAccessor() {
      @Override
      public AnnotatedByteArray create() {
        throw new UnsupportedOperationException();
      }

      @Override
      public AnnotatedByteArray wrap(long address) {
        ByteArray wrappedKey = addressToKey.get(address);
        Long annotationAddress = keyToCacheKey.get(wrappedKey);
        AnnotatedByteArray annotatedByteArray = new AnnotatedByteArray(address, annotationAddress);
        annotatedByteArray.setData(wrappedKey.getArray());

        return annotatedByteArray;
      }
    };
  }
}
