package xyz.rollingstone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * It's created so you don't need to do query by yourself YOLO
 */
public class ActionSQLHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "BigDB";

    // Bigs table name
    private static final String TABLE_BIG = "Big";

    // Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_DIRECTION = "direction";
    public static final String KEY_LENGTH = "length";

    private static final String[] COLUMNS = {KEY_ID, KEY_NAME};

    public ActionSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create book table
        String CREATE_BOOK_TABLE = "CREATE TABLE Big ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT );";

        // create books table
        db.execSQL(CREATE_BOOK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS books");

        // create fresh books table
        this.onCreate(db);
    }

    /**
     * CRUD operations (create "add", read "get", update, delete) book + get all books + delete all books
     */

    public void addBig(Big big) {
        //for logging
        Log.d("addBig", big.getName());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, big.getName()); // get title

        // 3. insert
        db.insert(TABLE_BIG, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();

        getAllBigs();
    }

    public Big getBig(int id) {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_BIG, // a. table
                        COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[]{String.valueOf(id)}, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build big object
        Big big = new Big();
        big.setId(Integer.parseInt(cursor.getString(0)));
        big.setName(cursor.getString(1));


        //log
        Log.d("getBook(" + id + ")", big.toString());

        // 5. return big
        return big;
    }

    public List<Big> getAllBigs() {
        List<Big> bigs = new LinkedList<Big>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_BIG;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Big big = null;
        if (cursor.moveToFirst()) {
            do {
                big = new Big();
                big.setId(Integer.parseInt(cursor.getString(0)));
                big.setName(cursor.getString(1));

                // Add big to bigs
                Log.d("getAllBigs()", big.toString());
                bigs.add(big);
            } while (cursor.moveToNext());
        }
        // return books
        return bigs;
    }

    public int updateBig(Big big) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("name", big.getName()); // get name

        // 3. updating row
        int i = db.update(TABLE_BIG, //table
                values, // column/value
                KEY_ID + " = ?", // selections
                new String[]{String.valueOf(big.getId())}); //selection args

        // 4. close
        db.close();

        return i;

    }

    public void deleteBigById(int id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_BIG, //table name
                KEY_ID + " = ?",  // selections
                new String[]{String.valueOf(id)}); //selections args

        // 3. close
        db.close();

        //log
        Log.d("deleteBig", Integer.toString(id));

    }

    public void deleteBigByName(String name) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_BIG, //table name
                KEY_NAME + " = ?",  // selections
                new String[]{name}); //selections args

        // 3. close
        db.close();

        deleteTableByName(name);

        //log
        Log.d("deleteBig", name);

    }

    public List<String> getAllBigsName() {
        List<String> bigsName = new LinkedList<String>();

        // 1. build the query
        String query = "SELECT  NAME FROM " + TABLE_BIG;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        if (cursor.moveToFirst()) {
            do {
                // Add book to books
                bigsName.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        Log.d("getAllBigsName()", bigsName.toString());

        // return books
        return bigsName;
    }

    /*
        Action CRUD
    */

    public void createActionTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String CREATE_ACTION_TABLE = String.format("CREATE TABLE %s ( ", tableName) +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "DIRECTION TEXT, LENGTH INTEGER );";
        Log.d("createActionTable - ", tableName);

        db.execSQL(CREATE_ACTION_TABLE);
    }

    public void addActionToTable(String tableName, Action action) {
        //for logging
        Log.d("addAction", action.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_DIRECTION, action.getDirection());
        values.put(KEY_LENGTH, action.getLength());

        // 3. insert
        db.insert(tableName, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public List<Action> getAllActionsFromTable(String tableName) {
        List<Action> actions = new LinkedList<Action>();

        // 1. build the query
        String query = "SELECT  * FROM " + tableName;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build book and add it to list
        Action act = null;
        if (cursor.moveToFirst()) {
            do {
                act = new Action();
                act.setId(Integer.parseInt(cursor.getString(0)));
                act.setDirection(cursor.getString(1));
                act.setLength(cursor.getInt(2));

                // Add act to actions
                actions.add(act);
            } while (cursor.moveToNext());
        }

        Log.d("getAllActions()", actions.toString());

        // return books
        return actions;
    }

    public int updateActionInTable(String tableName, Action act) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_ID, act.getId()); // get id
        values.put(KEY_DIRECTION, act.getDirection()); // get direction
        values.put(KEY_LENGTH, act.getLength()); // get length

        // 3. updating row
        int i = db.update(tableName, //table
                values, // column/value
                KEY_ID + " = ?", // selections
                new String[]{String.valueOf(act.getId())}); //selection args

        // 4. close
        db.close();

        return i;

    }

    public void deleteActioninTableId(String tableName, int id) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(tableName, //table name
                KEY_ID + " = ?",  // selections
                new String[]{String.valueOf(id)}); //selections args

        // 3. close
        db.close();

        //log
        Log.d("deleteAction", Integer.toString(id));

    }

    public void deleteTableByName(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = String.format("DROP TABLE IF EXISTS %s", tableName);
        db.execSQL(sql);

        //log
        Log.d("deleteTableByName", tableName);
    }
}