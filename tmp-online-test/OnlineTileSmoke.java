import ar.com.catgis.OnlineMapCatalog;
import ar.com.catgis.data.online.OnlineRasterSource;
import ar.com.catgis.data.online.OnlineTileCache;
import java.awt.image.BufferedImage;

public class OnlineTileSmoke {
  public static void main(String[] args) throws Exception {
    OnlineRasterSource osm = OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_OSM);
    OnlineRasterSource esri = OnlineMapCatalog.getById(OnlineMapCatalog.SOURCE_ESRI_WORLD_IMAGERY);
    test("OSM", osm);
    test("ESRI", esri);
    OnlineTileCache.shutdown();
  }
  static void test(String name, OnlineRasterSource src) throws Exception {
    BufferedImage img = null;
    long end = System.currentTimeMillis() + 20000;
    while (System.currentTimeMillis() < end) {
      img = OnlineTileCache.getTile(src, 0, 0, 0, null);
      if (img != null) break;
      Thread.sleep(250);
    }
    System.out.println(name + "=" + (img != null ? (img.getWidth() + "x" + img.getHeight()) : "NULL"));
    System.out.println(name + "_FAIL=" + OnlineTileCache.getRecentSourceFailure(src.getId(), 600000L));
  }
}