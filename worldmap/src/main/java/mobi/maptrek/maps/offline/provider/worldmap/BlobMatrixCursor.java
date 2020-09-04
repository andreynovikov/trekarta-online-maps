package mobi.maptrek.maps.offline.provider.worldmap;

import android.database.CursorWindow;
import android.database.MatrixCursor;

/*
 * Special MatrixCursor that correctly processes Blobs.
 * It works on a content provider for JUST BLOBS.
 * https://stackoverflow.com/a/5657078
 */
class BlobMatrixCursor extends MatrixCursor {
    public BlobMatrixCursor(String[] projection) {
        super(projection);
    }

    public void fillWindow(int position, CursorWindow window) {
        if (position < 0 || position >= getCount()) {
            return;
        }
        window.acquireReference();
        try {
            int oldpos = getPosition();
            moveToPosition(position - 1);
            window.clear();
            window.setStartPosition(position);
            int columnNum = getColumnCount();
            window.setNumColumns(columnNum);
            while (moveToNext() && window.allocRow()) {
                for (int i = 0; i < columnNum; i++) {
                    byte [] field = getBlob(i);
                    if (field != null) {
                        if (!window.putBlob(field, getPosition(), i)) {
                            window.freeLastRow();
                            break;
                        }
                    } else {
                        if (!window.putNull(getPosition(), i)) {
                            window.freeLastRow();
                            break;
                        }
                    }
                }
            }

            moveToPosition(oldpos);
        } catch (IllegalStateException e) {
            // simply ignore it
        } finally {
            window.releaseReference();
        }
    }
}
