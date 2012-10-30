package com.facebook.data.datumdigest;

import com.facebook.data.types.Datum;
import com.facebook.util.digest.DigestFunction;

/**
 * marker interface for Digest\<Datum\>
 */
public interface DatumDigest extends DigestFunction<Datum> {
}
