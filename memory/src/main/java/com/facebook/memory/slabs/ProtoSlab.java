package com.facebook.memory.slabs;

/**
 * stacks:
 *
 * RawSlab -> ManagedSlab -> ShardedSlab -> ThreadLocalSlab
 */
public class ProtoSlab extends WrappedSlab {
  public ProtoSlab(int numShards, int slabSizeBytes, int tlSlabSizeBytes) {
    super(createSlab(numShards, slabSizeBytes, tlSlabSizeBytes));
  }

  private static Slab createSlab(int numShards, int slabSizeBytes, int tlSlabSizeBytes) {
    ManagedSlabFactory managedSlabFactory = new ManagedSlabFactory();
    SlabPool slabPool = ShardedSlabPool.create(managedSlabFactory, numShards, slabSizeBytes);
    ShardedSlab shardedSlab = new ShardedSlab(slabPool, ThreadLocalThenMostFreePolicy::new);
    ThreadLocalSlab threadLocalSlab = new ThreadLocalSlab(tlSlabSizeBytes, shardedSlab);

    return threadLocalSlab;
  }
}
