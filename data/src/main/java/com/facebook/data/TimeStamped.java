package com.facebook.data;

import com.facebook.util.serialization.SerDe;
import com.facebook.util.serialization.SerDeException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TimeStamped<K> {

  private final DateTime dateKey;
  private final K key;

  public TimeStamped(DateTime dateKey, K key) {
    this.dateKey = dateKey;
    this.key = key;
  }

  public DateTime getDateKey() {
    return dateKey;
  }

  public K getKey() {
    return key;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final TimeStamped<K> that = (TimeStamped<K>) o;

    if (dateKey != null ? !dateKey.equals(that.dateKey) : that.dateKey != null) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = dateKey != null ? dateKey.hashCode() : 0;
    result = 31 * result + (key != null ? key.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TimeStamped{" +
      "dateKey=" + dateKey +
      ", key=" + key +
      '}';
  }

  public static class SerDeImpl<K>
    implements SerDe<TimeStamped<K>> {
    private final SerDe<K> keySerDe;

    public SerDeImpl(SerDe<K> keySerDe) {
      this.keySerDe = keySerDe;
    }

    @Override
    public TimeStamped<K> deserialize(DataInput in) throws SerDeException {
      try {
        long dateKeyMillis= in.readLong();
        DateTime dateKey = new DateTime(dateKeyMillis, DateTimeZone.UTC);
        K key = keySerDe.deserialize(in);
        
        return new TimeStamped<K>(dateKey, key);
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
    
    @Override
    public void serialize(TimeStamped<K> value, DataOutput out)
      throws SerDeException {
      try {
        out.writeLong(value.dateKey.getMillis());
        keySerDe.serialize(value.key, out);
      } catch (IOException e) {
        throw new SerDeException(e);
      }
    }
  }
}
