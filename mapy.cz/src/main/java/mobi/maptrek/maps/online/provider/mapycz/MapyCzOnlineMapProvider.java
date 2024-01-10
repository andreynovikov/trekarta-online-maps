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

package mobi.maptrek.maps.online.provider.mapycz;

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

public class MapyCzOnlineMapProvider extends ContentProvider {
    private static final String MAPS_URL = "https://mapserver.mapy.cz/%s-m/%d-%d-%d";
    private final String[] maps = {"base", "turist", "ophoto"};

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
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        String license = resources.getString(R.string.license);
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
                        row.add(0);
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

        @SuppressLint("DefaultLocale")
        String url = String.format(MAPS_URL, mapId, z, x, y);
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
}
