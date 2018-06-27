package nl.remcohoeneveld.birthdaynotifications.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final String TAG = "DatabaseHelper";
    private static final String TABLE_NAME = "users_table";
    private static final String UID = "UID";

    private SQLiteDatabase bdayDB;
    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                UID + " TEXT NOT NULL)" ;
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
    }

    public boolean addData(String uid){
        bdayDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(UID, uid);

        Log.d(TAG, "addData: inserting " + uid + " to " + TABLE_NAME);

        long result = bdayDB.insert(TABLE_NAME, null, contentValues);

        return result != -1;
    }

    public Cursor getData(){
        bdayDB = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = bdayDB.rawQuery(query,null);
        return data;
    }

    @Override
    public synchronized void close() {
        super.close();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public void clearDatabase(String TABLE_NAME) {
        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        bdayDB.execSQL(clearDBQuery);
    }
}