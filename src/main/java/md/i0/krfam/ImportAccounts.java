package md.i0.krfam;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ImportAccounts extends AppCompatActivity {
    TextView importInfo;
    boolean importing = false;
    long importCount = 0;
    Database db = new Database();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_accounts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("KRFAM: Import Accounts");
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        KRFAM.log("MainActivity.java > onOptionsMenuSelected");
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!importing) {
            super.onBackPressed();
            finish();
        } else {
            KRFAM.Toast("Please wait for import to finish");
        }
    }

    private ArrayList<File> importFolder(File folder, long parentFolder, Server s) {
        KRFAM.log("Import > importFolder");
        ArrayList<File> fl = new ArrayList<>();
        /*
        if (folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();
            long iFolder;
            if (folder.getPath().equals(new File(s.adFile.getParentFile(), "Accounts").getPath())) {
                iFolder = parentFolder;
            } else {
                iFolder = db.createFolder(folder.getName(), parentFolder);
            }
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    KRFAM.log(" Import > importFolder > importFile > " + listOfFiles[i].toString());
                    String iName = listOfFiles[i].getName().replaceFirst(".xml$", "");
                    KRFAM.log(" Import > importFolder > importFile > name > " + iName);
                    try {
                        String text = new Scanner(listOfFiles[i], "UTF-8").useDelimiter("\\A").next();
                        final File fFile = listOfFiles[i];
                        String s1[] = text.split("<string name=\\\"\\[LOVELIVE_ID\\]user_id\\\">");
                        String s2[] = s1[1].split("<\\/string>");
                        String iUser = s2[0];
                        String s3[] = text.split("<string name=\\\"\\[LOVELIVE_PW\\]passwd\">");
                        String s4[] = s3[1].split("<\\/string>");
                        String iPass = s4[0];
                        db.saveNewAccount(iName, iUser, iPass, s.codeName, iFolder);
                        importCount++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                importInfo.setText("Imported Accounts:\n" + importCount + "\n\nLast File:\n" + fFile);
                            }
                        });
                    } catch (Exception e) {
                        KRFAM.log(e);
                    }

                } else {
                    importFolder(listOfFiles[i], iFolder, s);
                }
            }
        }*/
        return fl;
    }

    public void startImport(final View v) {
        if (importing == false) {
            importInfo = new TextView(this);
            importing = true;
            setContentView(importInfo);
            importInfo.setGravity(Gravity.CENTER_HORIZONTAL);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    KRFAM.Toast("Starting Import");
                    KRFAM.log("Import > Start");
                    for (Server s : KRFAM.serverList) {
                        if (s.codeName.equals(v.getTag())) {
                            File oldAccountsDirectory = new File(s.adFile.getParentFile(), "Accounts");
                            if (oldAccountsDirectory.exists()) {
                                KRFAM.log("Import > Start " + s.name);
                                String fName = "Import " + s.codeName;
                                if (db.folderExistsIn(fName, -1)) {
                                    int iter = 1;
                                    while (db.folderExistsIn("Import " + s.codeName + "(" + iter + ")", -1)) {
                                        iter++;
                                    }
                                    fName = "Import " + s.codeName + "(" + iter + ")";
                                }
                                long importFolder = db.createFolder(fName, -1);
                                importFolder(oldAccountsDirectory, importFolder, s);

                                KRFAM.sharedPreferences.edit().putLong("CURRENT_FOLDER", importFolder).commit();
                                KRFAM.Toast("Import Finished");
                                KRFAM.log("Import > Complete");
                                finish();
                                return;
                            }
                        }
                    }
                    KRFAM.log("Import > No Data");
                    KRFAM.Toast("No data to import.");
                    importing = false;
                }
            });
            thread.start();
        }
    }
}
