/**
 * The classes in this package are utility functional interfaces that mirror java.util.function.*.
 * They are extensions that allow the throwing of a typed exception.  To make these interfaces more useful,
 * each interface has a static "quiet" builder, that constructs a java version of the interface that wraps
 * checked exceptions in {@link com.facebook.util.exceptions.UncheckedCheckedException}s.  Here is an
 * example of how to use one of these quiet factory methods:
 * 
 * <pre>
 * {@code
 * // Compile error because Files.delete() throws IOException
 * Files.list(Paths.get("/path/to/some/dir")).forEach(Files::delete);
 * // Quietly re-throws IOExceptions as UncheckedCheckedException
 * Files.list(Paths.get("/path/to/some/dir")).forEach(ExtConsumer.quiet(Files::delete));
 * }
 * </pre>
 */
package com.facebook.util.function;
