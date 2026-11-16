package ar.com.catgis.layout;

import java.io.File;
import java.io.IOException;

/** Persists and loads a whole CATMAP layout document (page geometry + elements). */
public interface LayoutDocumentCodec {

    void save(LayoutModel model, File file) throws IOException;

    LayoutModel load(File file) throws IOException;
}
