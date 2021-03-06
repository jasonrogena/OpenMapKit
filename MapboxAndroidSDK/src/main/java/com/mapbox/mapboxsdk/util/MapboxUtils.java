package com.mapbox.mapboxsdk.util;

import android.content.Context;
import android.text.TextUtils;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.constants.MathConstants;

public class MapboxUtils implements MapboxConstants {

    // Access Token For V4 of API.  If it doesn't exist, SDK will fall back to use V3
    private static String accessToken = null;

    public static String getAccessToken() {
        return accessToken;
    }

    public static void setAccessToken(String accessToken) {
        MapboxUtils.accessToken = accessToken;
    }

    public static String qualityExtensionForImageQuality(RasterImageQuality imageQuality) {
        String qualityExtension;
        switch (imageQuality) {
            case MBXRasterImageQualityPNG32:
                qualityExtension = "png32";
                break;
            case MBXRasterImageQualityPNG64:
                qualityExtension = "png64";
                break;
            case MBXRasterImageQualityPNG128:
                qualityExtension = "png128";
                break;
            case MBXRasterImageQualityPNG256:
                qualityExtension = "png256";
                break;
            case MBXRasterImageQualityJPEG70:
                qualityExtension = "jpg70";
                break;
            case MBXRasterImageQualityJPEG80:
                qualityExtension = "jpg80";
                break;
            case MBXRasterImageQualityJPEG90:
                qualityExtension = "jpg90";
                break;
            case MBXRasterImageQualityFull:
            default:
                qualityExtension = "png";
                break;
        }
        return qualityExtension;
    }

    public static String markerIconURL(Context context, String size, String symbol, String color) {
        // Make a string which follows the MapBox Core API spec for stand-alone markers. This relies on the MapBox API
        // for error checking.
        //
        StringBuffer marker = new StringBuffer("pin-");

        if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 'l') {
            marker.append("l"); // large
        } else if (size.toLowerCase(MapboxConstants.MAPBOX_LOCALE).charAt(0) == 's') {
            marker.append("s"); // small
        } else {
            marker.append("m"); // default to medium
        }

        if (!TextUtils.isEmpty(symbol)) {
            marker.append(String.format("-%s+", symbol));
        } else {
            marker.append("+");
        }

        marker.append(color.replaceAll("#", ""));

        if (AppUtils.isRunningOn2xOrGreaterScreen(context)) {
            marker.append("@2x");
        }
        marker.append(".png");

        if (!TextUtils.isEmpty(MapboxUtils.getAccessToken())) {
            marker.append("?access_token=");
            marker.append(MapboxUtils.getAccessToken());
            return String.format(MapboxConstants.MAPBOX_BASE_URL_V4 + "marker/%s", marker);
        }

        return String.format(MapboxConstants.MAPBOX_BASE_URL_V3 + "marker/%s", marker);
    }

    public static String getMapTileURL(Context context, String mapID, int zoom, int x, int y, RasterImageQuality imageQuality) {
        if (!TextUtils.isEmpty(MapboxUtils.getAccessToken())) {
            return String.format(MAPBOX_BASE_URL_V4 + "%s/%d/%d/%d%s.%s?access_token=%s", mapID, zoom, x, y, (AppUtils.isRunningOn2xOrGreaterScreen(context) ? "@2x" : ""), MapboxUtils.qualityExtensionForImageQuality(imageQuality), MapboxUtils.getAccessToken());
        }
        return String.format(MAPBOX_BASE_URL_V3 + "%s/%d/%d/%d%s.%s", mapID, zoom, x, y, (AppUtils.isRunningOn2xOrGreaterScreen(context) ? "@2x" : ""), MapboxUtils.qualityExtensionForImageQuality(imageQuality));
    }

    /**
     * Build a UTFGrid string for given Coordinate and Zoom Level
     * @param latLng Geo Coordinate
     * @param zoom Zoom Level
     * @return UTFGrid String (z/x/y)
     */
    public static String getUTFGridString(ILatLng latLng, int zoom) {

        int tilesPerSide = Double.valueOf(Math.pow(2.0, zoom)).intValue();
        int x = Double.valueOf(Math.floor(((latLng.getLongitude() + 180.0) / 360.0) * tilesPerSide)).intValue();
        int y = Double.valueOf(Math.floor((1.0 - (Math.log(Math.tan(latLng.getLatitude() * MathConstants.PI / 180.0) + 1.0 / Math.cos(latLng.getLatitude() * MathConstants.PI / 180.0)) / MathConstants.PI)) / 2.0 * tilesPerSide)).intValue();

        return String.format("%d/%d/%d", zoom, x, y);
    }
}
