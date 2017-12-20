/*
 * Androzic - android navigation client that uses OziExplorer maps (ozf2, ozfx3).
 * Copyright (C) 2010-2015 Andrey Novikov <http://andreynovikov.info/>
 *
 * This file is part of Androzic application.
 *
 * Androzic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Androzic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Androzic.  If not, see <http://www.gnu.org/licenses/>.
 */

package mobi.maptrek.maps.online.provider.geoportalgovpl;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public class GeoportalGovPlFiOnlineMapProvider extends ContentProvider {
    public static final String AUTHORITY = "mobi.maptrek.maps.online.provider.geoportalgovpl";
    public static final String TILE_TYPE = "vnd.android.cursor.item/vnd.mobi.maptrek.maps.online.provider.tile";

    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "*/#/#/#", 1);
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if (uriMatcher.match(uri) < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);

        return TILE_TYPE;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uriMatcher.match(uri) < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);

        List<String> pathSegments = uri.getPathSegments();
        String path = pathSegments.get(0);
        int z = Integer.parseInt(pathSegments.get(1));
        int x = Integer.parseInt(pathSegments.get(2));
        int y = Integer.parseInt(pathSegments.get(3));

        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();

        BoundingBox bb = BoundingBox.tileToBoundingBox(z, x, y);

        @SuppressLint("DefaultLocale")
        String url = String.format("http://mapy.geoportal.gov.pl/wss/service/img/guest/%s/MapServer/WMSServer?STYLES=default&SERVICE=WMS&FORMAT=image/jpeg&REQUEST=GetMap&HEIGHT=256&WIDTH=256&VERSION=1.1.1&BBOX=%f,%f,%f,%f&LAYERS=Raster&SRS=EPSG:4326&EXCEPTIONS=application/vnd.ogc.se_inimage&TRANSPARENT=false", path, bb.west, bb.south, bb.east, bb.north);
        row.add(url);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Tile URIs can not be inserted");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Tile URIs can not be updated");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Tile URIs can not be deleted");
    }

    static class BoundingBox {
        double north;
        double south;
        double east;
        double west;

        static BoundingBox tileToBoundingBox(final int zoom, final int x, final int y) {
            BoundingBox bb = new BoundingBox();
            bb.north = tile2lat(y, zoom);
            bb.south = tile2lat(y + 1, zoom);
            bb.west = tile2lon(x, zoom);
            bb.east = tile2lon(x + 1, zoom);
            return bb;
        }

        static double tile2lon(int x, int z) {
            return x / Math.pow(2.0, z) * 360.0 - 180;
        }

        static double tile2lat(int y, int z) {
            double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
            return Math.toDegrees(Math.atan(Math.sinh(n)));
        }
    }
}
