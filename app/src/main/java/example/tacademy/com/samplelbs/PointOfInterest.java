package example.tacademy.com.samplelbs;

import com.skp.Tmap.TMapPOIItem;

/**
 * Created by Administrator on 2016-08-18.
 */
public class PointOfInterest {
    TMapPOIItem item;

    public PointOfInterest(TMapPOIItem item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return item.getPOIName();
    }
}
