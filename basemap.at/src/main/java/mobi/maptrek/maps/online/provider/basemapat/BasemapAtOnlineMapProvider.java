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

package mobi.maptrek.maps.online.provider.basemapat;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public class BasemapAtOnlineMapProvider extends ContentProvider {
    private static final String MAPS_URL = "https://%s.wien.gv.at/basemap/%s/normal/google3857/%d/%d/%d.%s";
    private final String[] maps = {"geolandbasemap", "bmapgrau", "bmaporthofoto30cm"};
    private final String[] servers = {"maps", "maps1", "maps2", "maps3", "maps4"};
    private UriMatcher uriMatcher;
    private int nextServer = 0;

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
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        String license = context.getString(R.string.license);
        MatrixCursor cursor = new MatrixCursor(projection);
        int id;
        for (String map : maps) {
            MatrixCursor.RowBuilder row = cursor.newRow();
            for (String column : projection) {
                switch (column) {
                    case "IDENTIFIER":
                        row.add(map);
                        break;
                    case "NAME":
                        id = resources.getIdentifier(map + "_name", "string", packageName);
                        String name = context.getString(id);
                        row.add(name);
                        break;
                    case "LICENSE":
                        row.add(license);
                        break;
                    case "MIN_ZOOM":
                        row.add(6);
                        break;
                    case "MAX_ZOOM":
                        row.add(20);
                        break;
                }
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

        String ext = "bmaporthofoto30cm".equals(mapId) ? "jpeg" : "png";

        @SuppressLint("DefaultLocale")
        String url = String.format(MAPS_URL, servers[nextServer], mapId, z, y, x, ext);
        row.add(url);

        nextServer++;
        if (servers.length <= nextServer)
            nextServer = 0;

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
}
