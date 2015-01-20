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

package com.androzic.map.online.provider.thunderforest;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.androzic.map.online.TileProvider;

import java.util.List;

public class ThunderforestOnlineMapProvider extends ContentProvider
{
	public static final String AUTHORITY = "com.androzic.map.online.provider.thunderforest";

	private UriMatcher uriMatcher;
	private String[] servers = {"a", "b", "c"};
	private int nextServer = 0;

	@Override
	public boolean onCreate()
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "*/#/#/#", 1);
		return true;
	}

	@Override
	public String getType(Uri uri)
	{
		if (uriMatcher.match(uri) < 0)
			throw new IllegalArgumentException("Unknown URI " + uri);

		return TileProvider.TILE_TYPE;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{
		if (uriMatcher.match(uri) < 0)
			throw new IllegalArgumentException("Unknown URI " + uri);

		List<String> pathSegments = uri.getPathSegments();
		String path = pathSegments.get(0);
		int z = Integer.parseInt(pathSegments.get(1));
		int x = Integer.parseInt(pathSegments.get(2));
		int y = Integer.parseInt(pathSegments.get(3));

		MatrixCursor cursor = new MatrixCursor(projection);
		MatrixCursor.RowBuilder row = cursor.newRow();

		String url = String.format("http://%s.tile.thunderforest.com/%s/%d/%d/%d.png", servers[nextServer], path, z, x, y);
		row.add(url);

		nextServer++;
		if (servers.length <= nextServer)
			nextServer = 0;

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		throw new UnsupportedOperationException("Tile URIs can not be inserted");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		throw new UnsupportedOperationException("Tile URIs can not be updated");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		throw new UnsupportedOperationException("Tile URIs can not be deleted");
	}
}
