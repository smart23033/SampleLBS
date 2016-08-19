package example.tacademy.com.samplelbs.request;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;

import example.tacademy.com.samplelbs.data.POIResult;
import example.tacademy.com.samplelbs.manager.NetworkRequest;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.ResponseBody;

/**
 * Created by Tacademy on 2016-08-19.
 */
public class POISearchRequest extends NetworkRequest<POIResult> {
    Request request;

    public POISearchRequest(Context context, String keyword) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("apis.skplanetx.com")
                .addPathSegments("/tmap/pois")
                .addQueryParameter("version","1")
                .addQueryParameter("searchKeyword", keyword)
                .addQueryParameter("resCoordType","WGS84GEO")
                .build();

        request = new Request.Builder()
                .url(url)
                .header("Accept","application/json")
                .header("appKey","9e719482-ed73-3153-9f91-5bcb75e149d5")
                .tag(context)
                .build();
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    protected POIResult parse(ResponseBody body) throws IOException {
        Gson gson = new Gson();
        POIResult result = gson.fromJson(body.charStream(),POIResult.class);
        return result;
    }
}
