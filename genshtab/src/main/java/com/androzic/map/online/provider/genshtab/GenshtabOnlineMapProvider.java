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

package com.androzic.map.online.provider.genshtab;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.androzic.map.online.TileProvider;

import java.util.List;

public class GenshtabOnlineMapProvider extends ContentProvider
{
	public static final String AUTHORITY = "com.androzic.map.online.provider.genshtab";

	private static final int TILE_BASE = 1;
	private static final int GS250M_TILE = 1;
	private static final int GS500M_TILE = 2;
	private static final int GS1KM_TILE = 3;
	private static final int GS2KM_TILE = 4;
	private static final int GS5KM_TILE = 5;
	private static final int GS10KM_TILE = 6;
	private UriMatcher uriMatcher;

	@Override
	public boolean onCreate()
	{
		String[] maps = getContext().getResources().getStringArray(R.array.maps);
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		// Array must be synchronized with *_TILE constants
		for (int i = 0; i < maps.length; i++)
			uriMatcher.addURI(AUTHORITY, maps[i] + "/#/#/#", TILE_BASE + i);
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
		int match = uriMatcher.match(uri);
		if (match < 0)
			throw new IllegalArgumentException("Unknown URI " + uri);

		List<String> pathSegments = uri.getPathSegments();
		int z = Integer.parseInt(pathSegments.get(1));
		int x = Integer.parseInt(pathSegments.get(2));
		int y = Integer.parseInt(pathSegments.get(3));

		String path = "";
		switch (match)
		{
			case GS250M_TILE:
				path = "genshtab250m";
				break;
			case GS500M_TILE:
				path = "genshtab500m";
				break;
			case GS1KM_TILE:
				path = "genshtab1km";
				break;
			case GS2KM_TILE:
				path = "genshtab2km";
				break;
			case GS5KM_TILE:
				path = "genshtab5km";
				break;
			case GS10KM_TILE:
				path = "genshtab10km";
				break;
		}

		MatrixCursor cursor = new MatrixCursor(projection);
		MatrixCursor.RowBuilder row = cursor.newRow();

		int portEx = 2+ (int) (Math.random() * 4);
		String url = String.format("http://91.237.82.95:808%d/%s/%d/%d/%d.jpg", portEx, path, z, x, y);
		row.add(url);

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
