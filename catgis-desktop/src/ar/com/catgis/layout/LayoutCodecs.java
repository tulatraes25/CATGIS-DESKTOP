package ar.com.catgis.layout;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Bridge between the layout model and the CATMAP serialization implementation.
 * <p>
 * The layout package must not depend on {@code ar.com.catgis.catmap} (the
 * serializer depends on layout). Layout code calls these registries; the CATMAP
 * implementation is either installed explicitly through
 * {@link #install(LayoutElementCodec)} / {@link #installDocument(LayoutDocumentCodec)}
 * or resolved lazily by class name when only the layout engine is exercised
 * (headless layout tests, standalone bootstrap).
 */
public final class LayoutCodecs {

    private static volatile LayoutElementCodec elementCodec;
    private static volatile LayoutDocumentCodec documentCodec;

    private LayoutCodecs() {}

    public static void install(LayoutElementCodec codec) {
        elementCodec = codec;
    }

    public static void installDocument(LayoutDocumentCodec codec) {
        documentCodec = codec;
    }

    /** Forget explicit installs (tests); the lazy CATMAP bridge is used again. */
    public static void reset() {
        elementCodec = null;
        documentCodec = null;
    }

    public static LayoutElementCodec elements() {
        LayoutElementCodec current = elementCodec;
        if (current == null) {
            current = reflectiveElementCodec();
            elementCodec = current;
        }
        return current;
    }

    public static LayoutDocumentCodec documents() {
        LayoutDocumentCodec current = documentCodec;
        if (current == null) {
            current = reflectiveDocumentCodec();
            documentCodec = current;
        }
        return current;
    }

    private static LayoutElementCodec reflectiveElementCodec() {
        try {
            Class<?> serializer = Class.forName("ar.com.catgis.catmap.CatmapSerializer");
            Method encode = serializer.getMethod("serializeElementRaw", LayoutElement.class);
            Method decode = serializer.getMethod("parseElementRaw", String.class);
            return new LayoutElementCodec() {
                @Override
                public String encode(LayoutElement element) {
                    try {
                        return (String) encode.invoke(null, element);
                    } catch (InvocationTargetException failure) {
                        throw rethrow(failure, "CATMAP element could not be serialized.");
                    } catch (ReflectiveOperationException invalid) {
                        throw new IllegalStateException(
                                "CATMAP element codec is not callable.", invalid);
                    }
                }

                @Override
                public LayoutElement decode(String record) {
                    try {
                        return (LayoutElement) decode.invoke(null, record);
                    } catch (InvocationTargetException failure) {
                        throw rethrow(failure, "CATMAP element record is invalid.");
                    } catch (ReflectiveOperationException invalid) {
                        throw new IllegalStateException(
                                "CATMAP element codec is not callable.", invalid);
                    }
                }
            };
        } catch (ReflectiveOperationException missing) {
            throw new IllegalStateException(
                    "CATMAP serialization is not on the classpath: the layout codec "
                            + "requires ar.com.catgis.catmap.CatmapSerializer.", missing);
        }
    }

    private static LayoutDocumentCodec reflectiveDocumentCodec() {
        try {
            Class<?> io = Class.forName("ar.com.catgis.catmap.CatmapDocumentIO");
            Method save = io.getMethod("save", LayoutModel.class, File.class);
            Method load = io.getMethod("load", File.class);
            return new LayoutDocumentCodec() {
                @Override
                public void save(LayoutModel model, File file) throws IOException {
                    try {
                        save.invoke(null, model, file);
                    } catch (InvocationTargetException failure) {
                        Throwable cause = failure.getCause();
                        if (cause instanceof IOException ioFailure) {
                            throw ioFailure;
                        }
                        throw new IOException("CATMAP document could not be saved.", cause);
                    } catch (ReflectiveOperationException invalid) {
                        throw new IllegalStateException(
                                "CATMAP document codec is not callable.", invalid);
                    }
                }

                @Override
                public LayoutModel load(File file) throws IOException {
                    try {
                        return (LayoutModel) load.invoke(null, file);
                    } catch (InvocationTargetException failure) {
                        Throwable cause = failure.getCause();
                        if (cause instanceof IOException ioFailure) {
                            throw ioFailure;
                        }
                        throw new IOException("CATMAP document could not be loaded.", cause);
                    } catch (ReflectiveOperationException invalid) {
                        throw new IllegalStateException(
                                "CATMAP document codec is not callable.", invalid);
                    }
                }
            };
        } catch (ReflectiveOperationException missing) {
            throw new IllegalStateException(
                    "CATMAP serialization is not on the classpath: the layout codec "
                            + "requires ar.com.catgis.catmap.CatmapDocumentIO.", missing);
        }
    }

    private static RuntimeException rethrow(InvocationTargetException failure, String message) {
        Throwable cause = failure.getCause();
        if (cause instanceof RuntimeException runtime) {
            return runtime;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new UncheckedIOException(message, new IOException(cause));
    }
}
