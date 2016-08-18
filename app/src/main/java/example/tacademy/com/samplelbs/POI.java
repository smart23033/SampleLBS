package example.tacademy.com.samplelbs;

import com.skp.Tmap.TMapPOIItem;

/**
 * Created by Tacademy on 2016-08-18.
 */
public class POI {
    TMapPOIItem item;

    public POI(TMapPOIItem item){
        this.item = item;
    }

    @Override
    public String toString() {
        return item.getPOIName();
    }
}
