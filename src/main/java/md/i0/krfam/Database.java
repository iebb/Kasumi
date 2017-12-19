package md.i0.krfam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;

public final class Database {
    private krfam_dbHelper dbHelper = new krfam_dbHelper(KRFAM.getContext());

    public Database() {
        KRFAM.log("Database > initiate");
    }

    public long saveNewAccount(String name, String user, String pass, String code, String server, long folder) {
        if (user.length() == 0) {
            KRFAM.log("Attempted to save blank user.");
            return -1;
        }
        KRFAM.log("Database > saveNewAccount");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cName, name);
        values.put(Accounts.cUser, user);
        values.put(Accounts.cPass, pass);
        values.put(Accounts.cCode, code);
        values.put(Accounts.cServer, server);
        values.put(Accounts.cFolder, folder);
        values.put(Accounts.cLock, 0);
        values.put(Accounts.cUsed, String.valueOf(System.currentTimeMillis()));
        long newRowId;
        newRowId = db.insert(Accounts.tName, null, values);
        db.close();
        return newRowId;
    }

    public int countAccounts(long folder, String search, boolean searchAll) {
        KRFAM.log("Database > countAccounts");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {"count(" + Folders._ID + ")"};
        String selection = "";
        for (Server s : KRFAM.serverList) {
            if (s.enabled == true) {
                if (selection.length() >= 1) {
                    selection += " OR ";
                }
                selection += Accounts.cServer + "='" + s.codeName + "'";
            }
        }
        ArrayList<String> selectionArgs = new ArrayList<>();
        if (selection.length() > 0) {
            selection = "(" + selection + ") AND ";
        }
        if (search.length() > 0) {
            selection += Accounts.cName + " LIKE ?";
            selectionArgs.add("%" + search + "%");
            if (!searchAll) {
                selection += " AND " + Accounts.cFolder + "=?";
                selectionArgs.add(String.valueOf(folder));
            }
        } else {
            selection += Accounts.cFolder + "=?";
            selectionArgs.add(String.valueOf(folder));
        }
        Cursor c = db.query(Accounts.tName, projection, selection, selectionArgs.toArray(new String[selectionArgs.size()]), null, null, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }

    public long getFolderParent(long parentOf) {
        KRFAM.log("Database > getFolderParent");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {Folders.cParent};
        String selection = Folders._ID + "=?";
        String[] selectionArgs = {String.valueOf(parentOf)};
        try {
            Cursor c = db.query(Folders.tName, projection, selection, selectionArgs, null, null, null);
            c.moveToFirst();
            return c.getLong(0);
        } catch (Exception e) {
            KRFAM.log(KRFAM.exceptionToString(e));
        }
        return -1;
    }

    public String getFolderName(long folderID) {
        KRFAM.log("Database > getFolderName");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {Folders.cName};
        String selection = Folders._ID + "=?";
        String[] selectionArgs = {String.valueOf(folderID)};
        try {
            Cursor c = db.query(Folders.tName, projection, selection, selectionArgs, null, null, null);
            c.moveToFirst();
            return c.getString(0);
        } catch (Exception e) {
            KRFAM.log(KRFAM.exceptionToString(e));
        }
        return "???";
    }

    public boolean folderExistsIn(String name, long parentFolder) {
        KRFAM.log("Database > folderExistsIn");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"count(" + Folders._ID + ")"};
        String selection = Folders.cName + "=? AND " + Folders.cParent + "=?";
        String[] selectionArgs = {name, "" + parentFolder};
        Cursor c = db.query(Folders.tName, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        db.close();
        return count > 0;
    }

    public boolean accountExistsIn(String name, long folder) {
        KRFAM.log("Database > accountExistsIn");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {"count(" + Accounts._ID + ")"};
        String selection = Accounts.cName + "=? AND " + Accounts.cFolder + "=?";
        String[] selectionArgs = {name, "" + folder};
        Cursor c = db.query(Accounts.tName, columns, selection, selectionArgs, null, null, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        db.close();
        return count > 0;
    }

    public long createFolder(String name, long parentFolder) {
        KRFAM.log("Database > createFolder");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Folders.cName, name);
        values.put(Folders.cParent, parentFolder);
        long newRowId;
        newRowId = db.insert(Folders.tName, null, values);
        return newRowId;
    }

    public long findFolder(String name, long parentFolder){
        KRFAM.log("Database > findFolder");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {Folders._ID};
        String selection = Folders.cName + "=? AND " + Folders.cParent + "=?";
        String[] selectionArgs = {name, "" + parentFolder};
        Cursor c = db.query(Folders.tName, columns, selection, selectionArgs, null, null, null);
        try {
            c.moveToFirst();
            long fId = c.getLong(0);
            c.close();
            db.close();
            return fId;
        }catch(Exception e){
            return -1;
        }
    }

    public ArrayList<Account> getFolders(long folder) {
        KRFAM.log("Database > getFolders");
        ArrayList<Account> folders = new ArrayList<Account>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                Folders._ID,
                Folders.cName,
                Folders.cLock,
                Folders.cParent
        };
        String sortOrder = Folders.cName + " ASC";
        String selection = Folders.cParent + "=?";
        String[] selectionArgs = {String.valueOf(folder)};
        try {
            Cursor c = db.query(Folders.tName, projection, selection, selectionArgs, null, null, sortOrder);
            c.moveToFirst();
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    Account a = new Account();
                    a.isFolder = true;
                    a.name = c.getString(c.getColumnIndex(Folders.cName));
                    a.id = c.getLong(c.getColumnIndex(Folders._ID));
                    a.parentFolder = c.getLong(c.getColumnIndex(Folders.cParent));
                    a.accountCount = countAccounts(a.id,"",false);
                    if (c.getInt(c.getColumnIndex(Folders.cLock)) == 0) {
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

    public ArrayList<Account> getAllFolders() {
        ArrayList<Account> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(Folders.tName, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            while (c.isAfterLast() == false) {
                Account f = new Account();
                f.isFolder = true;
                f.id = c.getLong(c.getColumnIndex(Folders._ID));
                f.parentFolder = c.getLong(c.getColumnIndex(Folders.cParent));
                f.name = c.getString(c.getColumnIndex(Folders.cName));
                list.add(f);
                c.moveToNext();
            }
        }
        return list;
    }

    public ArrayList<Account> getAccounts(long folder, Integer limit, Integer offset, String sortby, boolean reverse, String search, boolean searchAll) {
        KRFAM.log("Database > getAccounts");
        ArrayList<Account> accountList = new ArrayList<>();
        if (KRFAM.FOLDERS_ON_BOTTOM == false && search.equals("")) {
            accountList.addAll(getFolders(folder));
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = "";
        ArrayList<String> selectionArgs = new ArrayList<>();
        for (Server s : KRFAM.serverList) {
            if (s.enabled == true) {
                if (selection.length() >= 1) {
                    selection += " OR ";
                }
                selection += Accounts.cServer + "='" + s.codeName + "'";
            }
        }
        if (selection.length() > 0) {
            selection = "(" + selection + ")";
        } else {
            return null;
        }
        if (!searchAll) {
            if (selection.length() > 0) {
                selection += " AND ";
            }
            selection += Accounts.cFolder + "=?";
            selectionArgs.add(String.valueOf(folder));
        }
        if (search.length() > 0) {
            if (selection.length() > 0) {
                selection += " AND ";
            }
            selection += Accounts.cName + " LIKE ?";
            selectionArgs.add("%" + search + "%");
        }
        String limitStr = null;
        if (limit != null && offset != null) {
            limitStr = offset + "," + limit;
        }
        if (sortby.equals("")) {
            sortby = "name";
        }
        if (reverse) {
            sortby += " DESC";
        } else {
            sortby += " ASC";
        }
        try {
            Cursor c = db.query(Accounts.tName, null, selection, selectionArgs.toArray(new String[selectionArgs.size()]), null, null, sortby, limitStr);
            if (c.moveToFirst()) {
                while (c.isAfterLast() == false) {
                    Account a = new Account();
                    a.id = c.getLong(c.getColumnIndex(Accounts._ID));
                    a.name = c.getString(c.getColumnIndex(Accounts.cName));
                    try {
                        a.loaded = Long.parseLong(c.getString(c.getColumnIndex(Accounts.cUsed)));
                    } catch (Exception ex) {
                        a.loaded = 0;
                    }
                    a.server = c.getString(c.getColumnIndex(Accounts.cServer));
                    a.isFolder = false;
                    a.locked = true;
                    if (c.getInt(c.getColumnIndex(Accounts.cLock)) == 0) {
                        a.locked = false;
                    }
                    a.u_UUID = c.getString(c.getColumnIndex(Accounts.cUser));
                    a.u_Code = c.getString(c.getColumnIndex(Accounts.cCode));
                    a.u_Pass = c.getString(c.getColumnIndex(Accounts.cPass));
                    a.parentFolder = c.getLong(c.getColumnIndex(Accounts.cFolder));
                    if (a.u_UUID.length() > 0) {
                        accountList.add(a);
                    } else {
                        KRFAM.log("Invalid Account Deleted: id " + a.id);
                        deleteAccount(a);
                    }


                    c.moveToNext();
                }
            }
        } catch (Exception ex) {
            KRFAM.log(KRFAM.exceptionToString(ex));
        }
        if (KRFAM.FOLDERS_ON_BOTTOM == true && search.equals("")) {
            accountList.addAll(getFolders(folder));
        }
        return accountList;
    }

    public Account findAccountByUser(String user, String serverCode) {
        if (user.length() == 0) {
            return null;
        }
        KRFAM.log("Database > findAccountByUser");
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String selection = Accounts.cUser + "=? AND " + Accounts.cServer + "=?";
            String[] selectionArgs = {user, serverCode};
            Cursor c = db.query(Accounts.tName, null, selection, selectionArgs, null, null, null, "1");
            if (c.moveToFirst()) {
                Account a = new Account();
                a.id = c.getLong(c.getColumnIndex(Accounts._ID));
                a.name = c.getString(c.getColumnIndex(Accounts.cName));
                a.server = c.getString(c.getColumnIndex(Accounts.cServer));
                a.locked = true;
                a.loaded = 0;
                a.isFolder = false;
                a.u_UUID = c.getString(c.getColumnIndex(Accounts.cUser));
                a.u_Code = c.getString(c.getColumnIndex(Accounts.cCode));
                a.u_Pass = c.getString(c.getColumnIndex(Accounts.cPass));
                c.close();
                db.close();
                return a;
            }
        } catch (Exception e) {
            KRFAM.log(e);
        }
        return null;
    }

    public void updatePassCode(Account account, String newPassKey) {
        KRFAM.log("Database > updatePassCode");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cPass, newPassKey);
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
    }

    public void updateAccessTime(Account account) {
        KRFAM.log("Database > updateAccessTime");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cUsed, String.valueOf(System.currentTimeMillis()));
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
    }

    public int renameAccount(Account account, String newName) {
        KRFAM.log("Database > renameAccount");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cName, newName);
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        int count = db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
        return count;
    }

    public int renameFolder(Account folder, String newName) {
        KRFAM.log("Database > renameFolder");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Folders.cName, newName);
        String selection = Folders._ID + "=?";
        String[] selectionArgs = {String.valueOf(folder.id)};
        int count = db.update(Folders.tName, values, selection, selectionArgs);
        db.close();
        return count;
    }

    public void deleteAccount(Account account) {
        KRFAM.log("Database > deleteAccount");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        db.delete(Accounts.tName, selection, selectionArgs);
        db.close();
    }

    public void deleteFolder(Account folder) {
        KRFAM.log("Database > deleteFolder");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = Folders._ID + "=?";
        String[] selectionArgs = {String.valueOf(folder.id)};
        db.delete(Folders.tName, selection, selectionArgs);
        db.close();
    }

    public void setLock(Account account, boolean lockState) {
        KRFAM.log("Database > setLock");
        int lock = 1;
        if (lockState) {
            lock = 1;
        } else {
            lock = 0;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cLock, lock);
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
    }

    public void moveAccount(Account account, long newFolder) {
        KRFAM.log("Database > moveAccount [account]");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cFolder, newFolder);
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(account.id)};
        db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
    }

    public void moveAccount(long id, long newFolder){
        KRFAM.log("Database > moveAccount [id]");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Accounts.cFolder, newFolder);
        String selection = Accounts._ID + "=?";
        String[] selectionArgs = {String.valueOf(id)};
        db.update(Accounts.tName, values, selection, selectionArgs);
        db.close();
    }

    public void moveFolder(Account folder, long newFolder) {
        KRFAM.log("Database > moveFolder");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(Folders.cParent, newFolder);
        String selection = Folders._ID + "=?";
        String[] selectionArgs = {String.valueOf(folder.id)};
        db.update(Folders.tName, values, selection, selectionArgs);
        db.close();
    }

    public static abstract class Accounts implements BaseColumns {
        public static final String tName = "Accounts";
        public static final String cName = "name";
        public static final String cUser = "user";
        public static final String cPass = "pass";
        public static final String cCode = "codeName";
        public static final String cUsed = "used";
        public static final String cLock = "lock";
        public static final String cServer = "server";
        public static final String cFolder = "folder";
        private static final String sCreateEntries =
                "CREATE TABLE " + tName + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        cName + " TEXT," +
                        cUser + " TEXT," +
                        cPass + " TEXT," +
                        cCode + " TEXT," +
                        cUsed + " TEXT," +
                        cLock + " INTEGER," +
                        cServer + " TEXT, " +
                        cFolder + " TEXT)";
    }

    public static abstract class Folders implements BaseColumns {
        public static final String tName = "Folders";
        public static final String cName = "name";
        public static final String cParent = "parent";
        public static final String cLock = "lock";
        private static final String sCreateEntries =
                "CREATE TABLE " + tName + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        cName + " TEXT," +
                        cParent + " INTEGER," +
                        cLock + " INTEGER)";
    }

    public class krfam_dbHelper extends SQLiteOpenHelper {
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "KRFAM.db";

        public krfam_dbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            KRFAM.log("Database > onCreate");
            db.execSQL(Accounts.sCreateEntries);
            db.execSQL(Folders.sCreateEntries);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
    }

}
