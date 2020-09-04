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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    static String DB_NAME = "world_countries.mbtiles";

    public DatabaseHelper(Context context) {
        super(context, new File(context.getExternalFilesDir("databases"), DB_NAME).getAbsolutePath(), null, 1);
        File dataDir = context.getExternalFilesDir("databases");
        File dbFile = new File(dataDir, DB_NAME);
        if (!dbFile.exists()) {
            //noinspection ConstantConditions,unused
            boolean created = dbFile.getParentFile().mkdirs();
            copyDatabase(context, dbFile);
        }
    }

    private void copyDatabase(Context context, File dbFile) {
        byte[] buffer = new byte[1024];
        int length;
        try {
            InputStream in = context.getAssets().open("databases/" + DB_NAME);
            OutputStream out = new FileOutputStream(dbFile);
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // do nothing
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing
    }
}
