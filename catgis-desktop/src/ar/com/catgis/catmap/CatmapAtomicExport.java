package ar.com.catgis.catmap;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Objects;

/**
 * Fail-closed CATMAP output publication. Encoder and decoder work only on a
 * unique stage in the target directory; the old export is never opened for writing.
 * This does not certify inter-process writer exclusion or power-loss durability.
 */
final class CatmapAtomicExport {

    @FunctionalInterface
    interface StagedWriter {
        void write(Path staged) throws Exception;
    }

    @FunctionalInterface
    interface StagedValidator {
        void verify(Path staged) throws Exception;
    }

    @FunctionalInterface
    interface AtomicMover {
        void move(Path staged, Path destination) throws IOException;
    }

    private static final long MAX_RASTER_PIXELS = 18_000_000L;

    private CatmapAtomicExport() {}

    static void atomicMove(Path staged, Path destination) throws IOException {
        Files.move(staged, destination, StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
    }

    static void publish(File requested, StagedWriter writer,
                        StagedValidator validator) throws Exception {
        publish(requested, writer, validator, CatmapAtomicExport::atomicMove);
    }

    /** Exposed only within this package for deterministic failed-move testing. */
    static void publish(File requested, StagedWriter writer,
                        StagedValidator validator, AtomicMover mover) throws Exception {
        Objects.requireNonNull(requested, "Export destination");
        Objects.requireNonNull(writer, "Export writer");
        Objects.requireNonNull(validator, "Export validator");
        Objects.requireNonNull(mover, "Atomic mover");

        Path initial = requested.toPath().toAbsolutePath().normalize();
        Path parent = initial.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new IOException("El directorio de exportacion CATMAP no existe.");
        }
        // Resolve aliases once, so staging and target share one actual filesystem.
        parent = parent.toRealPath();
        Path target = parent.resolve(initial.getFileName());
        validateTarget(target);

        Path staged = Files.createTempFile(parent,
                "." + target.getFileName() + "-", ".part");
        try {
            writer.write(staged);
            if (!Files.isRegularFile(staged, LinkOption.NOFOLLOW_LINKS)
                    || Files.size(staged) == 0L) {
                throw new IOException("La exportacion CATMAP esta vacia o no es un archivo regular.");
            }
            validator.verify(staged);
            try (FileChannel channel = FileChannel.open(staged, StandardOpenOption.WRITE)) {
                channel.force(true);
            }
            validateTarget(target);
            try {
                mover.move(staged, target);
            } catch (AtomicMoveNotSupportedException unsupported) {
                throw new IOException("El sistema de archivos no permite exportacion atomica; "
                        + "se conservo el archivo anterior.", unsupported);
            }
        } finally {
            // Only a uniquely created CATMAP-owned stage may be cleaned.
            Files.deleteIfExists(staged);
        }
    }

    private static void validateTarget(Path target) throws IOException {
        if (Files.isSymbolicLink(target)
                || (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
                && !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS))) {
            throw new IOException("CATMAP no sobrescribira un enlace simbolico "
                    + "o un destino no regular: " + target.getFileName());
        }
    }

    /** Bounded before allocating an A4 pixel buffer, including JPEG's RGB copy. */
    static int[] rasterSize(int dpi, double widthMm, double heightMm) {
        if (dpi < 72 || dpi > 1200
                || !Double.isFinite(widthMm) || !Double.isFinite(heightMm)
                || widthMm <= 0 || heightMm <= 0) {
            throw new IllegalArgumentException("CATMAP requiere DPI y tamano de pagina validos.");
        }
        double w = widthMm * dpi / 25.4;
        double h = heightMm * dpi / 25.4;
        if (!Double.isFinite(w) || !Double.isFinite(h) || w < 1 || h < 1
                || w > Integer.MAX_VALUE || h > Integer.MAX_VALUE
                || w * h > MAX_RASTER_PIXELS) {
            throw new IllegalArgumentException("CATMAP: resolucion demasiado alta para "
                    + "exportacion segura (maximo 18 millones de pixeles). Reduzca DPI.");
        }
        return new int[] {(int) w, (int) h};
    }

    /** Tiny independent real pixel samples, not a full scientific raster audit. */
    static void verifyImage(Path file, String expectedFormat,
                            int expectedWidth, int expectedHeight) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(file.toFile())) {
            if (input == null) {
                throw new IOException("CATMAP: no se pudo abrir la imagen exportada.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("CATMAP: salida sin decodificador de imagen valido.");
            }
            ImageReader reader = readers.next();
            try {
                String actual = reader.getFormatName().toLowerCase(java.util.Locale.ROOT);
                boolean expectedJpeg = "jpg".equals(expectedFormat);
                if (!(expectedJpeg ? actual.equals("jpeg") || actual.equals("jpg")
                        : actual.equals(expectedFormat))) {
                    throw new IOException("CATMAP: formato de imagen distinto al solicitado.");
                }
                reader.setInput(input);
                if (reader.getWidth(0) != expectedWidth || reader.getHeight(0) != expectedHeight) {
                    throw new IOException("CATMAP: dimensiones de imagen exportada invalidas.");
                }
                int[][] samples = {{0, 0},
                        {expectedWidth / 2, expectedHeight / 2},
                        {expectedWidth - 1, expectedHeight - 1}};
                for (int[] point : samples) {
                    ImageReadParam param = reader.getDefaultReadParam();
                    param.setSourceRegion(new Rectangle(point[0], point[1], 1, 1));
                    java.awt.image.BufferedImage pixel = reader.read(0, param);
                    if (pixel == null || pixel.getWidth() != 1 || pixel.getHeight() != 1) {
                        throw new IOException("CATMAP: los pixeles exportados no se pueden leer.");
                    }
                }
            } finally {
                reader.dispose();
            }
        }
    }

    /** PDFBox 3 parses the actual staged PDF before any existing file is replaced. */
    static void verifyPdf(Path file, double widthMm, double heightMm) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument pdf =
                     org.apache.pdfbox.Loader.loadPDF(file.toFile())) {
            if (pdf.getNumberOfPages() != 1) {
                throw new IOException("CATMAP: cantidad de paginas PDF inesperada.");
            }
            org.apache.pdfbox.pdmodel.common.PDRectangle page =
                    pdf.getPage(0).getMediaBox();
            float expectedWidth = (float) (widthMm * 72.0 / 25.4);
            float expectedHeight = (float) (heightMm * 72.0 / 25.4);
            if (Math.abs(page.getWidth() - expectedWidth) > 0.5f
                    || Math.abs(page.getHeight() - expectedHeight) > 0.5f) {
                throw new IOException("CATMAP: tamano de pagina PDF inesperado.");
            }
        }
    }
}
