/*
 * Copyright 2020 Andrey Novikov
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
import android.content.UriMatcher;
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
    public static final String AUTHORITY = "mobi.maptrek.maps.offline.provider.worldmap";
    public static final String TILE_TYPE = "vnd.android.cursor.item/vnd.mobi.maptrek.maps.offline.provider.tile";

    private static final String SQL_GET_IMAGE = "SELECT tile_data FROM tiles WHERE tile_column = ? AND tile_row = ? AND zoom_level = ?";

    private UriMatcher uriMatcher;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "*/#/#/#", 1);
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        db = databaseHelper.getReadableDatabase(); // DB is closed automagically when content provider is destroyed
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
        // String path = pathSegments.get(0);
        String z = pathSegments.get(1);
        String x = pathSegments.get(2);
        String y = pathSegments.get(3);

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
