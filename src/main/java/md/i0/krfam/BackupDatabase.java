package md.i0.krfam;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import java.util.ArrayList;

public class BackupDatabase{
    public String databaseName;
    backup_dbHelper dbHelper;

    public BackupDatabase(String name) throws Exception{
        databaseName = name;
        dbHelper = new backup_dbHelper(KRFAM.getContext());
    };

    public class backup_dbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        public backup_dbHelper(Context context) {
            super(context, Environment.getExternalStorageDirectory() + "//KRFAM//Backups//" + databaseName + ".db", null, DATABASE_VERSION);
            KRFAM.log("dbHelper " + databaseName);
        }
        public void onCreate(SQLiteDatabase db) {}
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

    public ArrayList<Account> getFolders(long folder) {
        KRFAM.log("BackupDatabase > getFolders (From Folder "+ folder + ")");
        ArrayList<Account> folders = new ArrayList<Account>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                Database.Folders._ID,
                Database.Folders.cName,
                Database.Folders.cLock,
                Database.Folders.cParent
        };
        String sortOrder = Database.Folders.cName + " ASC";
        String selection = Database.Folders.cParent + "=?";
        String[] selectionArgs = {String.valueOf(folder)};
        try {
            Cursor c = db.query(Database.Folders.tName, projection, selection, selectionArgs, null, null, sortOrder);
            c.moveToFirst();
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    Account a = new Account();
                    a.isFolder = true;
                    a.name = c.getString(c.getColumnIndex(Database.Folders.cName));
                    KRFAM.log(a.name);
                    a.id = c.getLong(c.getColumnIndex(Database.Folders._ID));
                    a.parentFolder = c.getLong(c.getColumnIndex(Database.Folders.cParent));
                    if (c.getInt(c.getColumnIndex(Database.Folders.cLock)) == 0) {
                        a.locked = false;
                    } else {
                        a.locked = true;
                    }
                    folders.add(a);
                    c.moveToNext();
                }
            }
        } catch (Exception ex) {
            KRFAM.log(KRFAM.exceptionToString(ex));
        }
        return folders;
    }

    public ArrayList<Account> getAccounts(long folder) {
        KRFAM.log("BackupDatabase > getAccounts");
        ArrayList<Account> accountList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = Database.Accounts.cFolder + "=?";
        String[] selectionArgs = {""+folder};
        try {
            Cursor c = db.query(Database.Accounts.tName, null, selection, selectionArgs, null,null,null);
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    Account a = new Account();
                    a.id = c.getLong(c.getColumnIndex(Database.Accounts._ID));
                    a.name = c.getString(c.getColumnIndex(Database.Accounts.cName));
                    try {
                        a.loaded = Long.parseLong(c.getString(c.getColumnIndex(Database.Accounts.cUsed)));
                    } catch (Exception ex) {
                        a.loaded = 0;
                    }
                    a.server = c.getString(c.getColumnIndex(Database.Accounts.cServer));
                    a.isFolder = false;
                    a.locked = true;
                    if (c.getInt(c.getColumnIndex(Database.Accounts.cLock)) == 0) {
                        a.locked = false;
                    }
                    a.u_UUID = c.getString(c.getColumnIndex(Database.Accounts.cUser));
                    a.u_Pass = c.getString(c.getColumnIndex(Database.Accounts.cPass));
                    accountList.add(a);
                    c.moveToNext();
                }
            }
        } catch (Exception ex) {
            KRFAM.log(KRFAM.exceptionToString(ex));
        }
        return accountList;
    }



}
