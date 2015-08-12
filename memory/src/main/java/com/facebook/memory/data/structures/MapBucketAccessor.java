package com.facebook.memory.data.structures;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.collections.bytearray.WrappedByteArray;
import com.facebook.memory.FailedAllocationException;

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

  public OffHeapByteArrayAccessor createKeyAccessor() {
    return new OffHeapByteArrayAccessor() {
      @Override
      public OffHeapByteArray create(int size) throws FailedAllocationException {
        throw new UnsupportedOperationException("create not supported");
      }

      @Override
      public OffHeapByteArray wrap(long address) {
        ByteArray wrappedKey = addressToKey.get(address);

        return new SimulatedOffHeapByteArray(wrappedKey, address);
      }
    };
  }

  private static class SimulatedOffHeapByteArray extends WrappedByteArray implements OffHeapByteArray {
    private final long address;

    private SimulatedOffHeapByteArray(ByteArray byteArray, long address) {
      super(byteArray);
      this.address = address;
    }

    @Override
    public long getAddress() {
      return address;
    }
  }
}
