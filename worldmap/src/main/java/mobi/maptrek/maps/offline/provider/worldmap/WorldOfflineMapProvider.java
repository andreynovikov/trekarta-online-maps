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

package mobi.maptrek.maps.offline.provider.worldmap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

/*
 * Map source:
 * https://www.arcgis.com/home/item.html?id=7b650618563741ca9a5186c1aa69126e
 */
public class WorldOfflineMapProvider extends ContentProvider {
    private static final String SQL_GET_IMAGE = "SELECT tile_data FROM tiles WHERE tile_column = ? AND tile_row = ? AND zoom_level = ?";

    private UriMatcher uriMatcher;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        assert context != null;
        String authority = context.getString(R.string.authority);
        String mapId = context.getString(R.string.world_identifier);
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(authority, "maps", 1);
        uriMatcher.addURI(authority, "tiles/" + mapId + "/#/#/#", 2);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        db = databaseHelper.getReadableDatabase(); // DB is closed automagically when content provider is destroyed
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

    private Cursor queryMaps(String[] projection) {
        Context context = getContext();
        assert context != null;
        Resources resources = context.getResources();
        MatrixCursor cursor = new MatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();
        for (String column : projection) {
            switch (column) {
                case "IDENTIFIER":
                    String id = context.getString(R.string.world_identifier);
                    row.add(id);
                    break;
                case "NAME":
                    String name = context.getString(R.string.world_name);
                    row.add(name);
                    break;
                case "LICENSE":
                    String license = context.getString(R.string.world_license);
                    row.add(license);
                    break;
                case "MIN_ZOOM":
                    int minZoom = resources.getInteger(R.integer.world_minzoom);
                    row.add(minZoom);
                    break;
                case "MAX_ZOOM":
                    int maxZoom = resources.getInteger(R.integer.world_maxzoom);
                    row.add(maxZoom);
                    break;
            }
        }
        return cursor;
    }

    private Cursor queryTiles(@NonNull Uri uri, String[] projection) {
        List<String> pathSegments = uri.getPathSegments();
        // String mapId = pathSegments.get(1);
        String z = pathSegments.get(2);
        String x = pathSegments.get(3);
        String y = pathSegments.get(4);

        // This particular map uses TMS addressing scheme so we have to invert Y axis
        int zoom = Integer.parseInt(z);
        y = String.valueOf((1 << zoom) - Integer.parseInt(y) - 1);

        byte[] bytes = null;
        try (Cursor c = db.rawQuery(SQL_GET_IMAGE, new String[]{x, y, z})) {
            if (c.moveToFirst())
                bytes = c.getBlob(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        MatrixCursor cursor = new BlobMatrixCursor(projection);
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(bytes);
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
