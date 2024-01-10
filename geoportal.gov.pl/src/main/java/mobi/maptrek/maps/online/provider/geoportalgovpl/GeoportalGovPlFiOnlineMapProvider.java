/*
 * Copyright 2024 Andrey Novikov
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mobi.maptrek.maps.online.provider.geoportalgovpl;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public class GeoportalGovPlFiOnlineMapProvider extends ContentProvider {
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        assert context != null;
        String authority = context.getString(R.string.authority);
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(authority, "maps", 1);
        uriMatcher.addURI(authority, "tiles/*/#/#/#", 2);
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        if (uriMatcher.match(uri) < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);

        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = uriMatcher.match(uri);
        if (code < 0)
            throw new IllegalArgumentException("Unknown URI " + uri);

        switch (code) {
            case 1:
                return queryMaps(projection);
            case 2:
                return queryTiles(uri, projection);
        }
        return null;
    }

    @SuppressLint("DiscouragedApi")
    private Cursor queryMaps(String[] projection) {
        Context context = getContext();
        assert context != null;
        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();
        for (String column : projection) {
            switch (column) {
                case "IDENTIFIER":
                    row.add("ORTO");
                    break;
                case "NAME":
                    row.add(context.getString(R.string.name));
                    break;
                case "LICENSE":
                    row.add(context.getString(R.string.license));
                    break;
                case "MIN_ZOOM":
                    row.add(4);
                    break;
                case "MAX_ZOOM":
                    row.add(19);
                    break;
                }
        }
        return cursor;
    }

    private Cursor queryTiles(@NonNull Uri uri, String[] projection) {
        List<String> pathSegments = uri.getPathSegments();
        String mapId = pathSegments.get(1);
        int z = Integer.parseInt(pathSegments.get(2));
        int x = Integer.parseInt(pathSegments.get(3));
        int y = Integer.parseInt(pathSegments.get(4));

        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();

        BoundingBox bb = BoundingBox.tileToBoundingBox(z, x, y);

        @SuppressLint("DefaultLocale")
        String url = String.format("http://mapy.geoportal.gov.pl/wss/service/img/guest/%s/MapServer/WMSServer?STYLES=default&SERVICE=WMS&FORMAT=image/jpeg&REQUEST=GetMap&HEIGHT=256&WIDTH=256&VERSION=1.1.1&BBOX=%f,%f,%f,%f&LAYERS=Raster&SRS=EPSG:4326&EXCEPTIONS=application/vnd.ogc.se_inimage&TRANSPARENT=false", mapId, bb.west, bb.south, bb.east, bb.north);
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
