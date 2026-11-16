package ar.com.catgis.layout;

/** Encodes/decodes a single layout element record (undo snapshots, templates). */
public interface LayoutElementCodec {

    String encode(LayoutElement element);

    LayoutElement decode(String record);
}
