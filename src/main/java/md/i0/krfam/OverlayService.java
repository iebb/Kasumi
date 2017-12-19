package md.i0.krfam;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager windowManager;
    private ImageView forceQuitButton;
    private ImageView renameButton;
    private TextView accountNameTextView;
    private LinearLayout topLeftContainer;
    private LinearLayout bottomContainer;
    private EditText renameTextBox;
    public AlertDialog alertDialog;
    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();
        KRFAM.log("OverlayService > onCreate");
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        if (KRFAM.CLOSE_BUTTON || KRFAM.OVERLAY_NAME) {
            topLeftContainer = new LinearLayout(this);
            topLeftContainer.setBackgroundColor(Color.BLACK);

            if (KRFAM.CLOSE_BUTTON) {
                forceQuitButton = new ImageView(this);
                forceQuitButton.setImageResource(R.drawable.ic_close_red_24dp);

                forceQuitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent krfamActivity = new Intent(KRFAM.getContext(), MainActivity.class);
                        krfamActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(krfamActivity);
                    }
                });
                topLeftContainer.addView(forceQuitButton, params);
            }
            if (KRFAM.OVERLAY_NAME) {
                accountNameTextView = new TextView(this);
                accountNameTextView.setText(KRFAM.lastLoadedAccountName);
                accountNameTextView.setTextColor(Color.WHITE);
                accountNameTextView.setTextSize(18);
                accountNameTextView.setPadding(3, 0, 3, 3);
                topLeftContainer.addView(accountNameTextView, params);
                if (KRFAM.OVERLAY_RENAME && (!KRFAM.lastLoadedAccountName.equals("New Account"))) {
                    renameButton = new ImageView(this);
                    renameButton.setImageResource(R.drawable.ic_mode_edit_white_24dp);

                    renameButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            KRFAM.log("OverlayService > RenameButton Pressed");

                            final EditText nameInput = new EditText(OverlayService.this);
                            nameInput.setSingleLine(true);
                            nameInput.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                    OverlayService.this.alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(MainActivity.isSaveNameValid(s.toString()));
                                }
                            });
                            nameInput.setHint("New Account Name");
                            InputFilter[] FilterArray = new InputFilter[1];
                            FilterArray[0] = new InputFilter.LengthFilter(64);
                            nameInput.setFilters(FilterArray);
                            AlertDialog.Builder builder = new AlertDialog.Builder(KRFAM.getContext());
                            builder.setMessage("Enter new name for current account")
                                    .setView(nameInput).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = nameInput.getText().toString();
                                    Database db = new Database();
                                    if (db.renameAccount(KRFAM.lastLoadedAccount, name) >= 1) {
                                        KRFAM.Toast("Renamed Account");
                                        KRFAM.lastLoadedAccount.name = name;
                                        KRFAM.lastLoadedAccountName = KRFAM.pathToCurrentFolder + " > " + KRFAM.lastLoadedAccount.name;
                                        accountNameTextView.setText(KRFAM.lastLoadedAccountName);
                                    } else {
                                        KRFAM.Toast("Rename Failed");
                                    }
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                            WindowManager.LayoutParams wmlp = alertDialog.getWindow().getAttributes();
                            wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                            alertDialog.show();
                        }
                    });
                    topLeftContainer.addView(renameButton,params);
                }
            }
            windowManager.addView(topLeftContainer,params);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (forceQuitButton != null) topLeftContainer.removeView(forceQuitButton);
        if (accountNameTextView != null) topLeftContainer.removeView(accountNameTextView);
        if (renameButton != null) topLeftContainer.removeView(renameButton);
        if (topLeftContainer != null) windowManager.removeView(topLeftContainer);
        if (renameTextBox != null) bottomContainer.removeView(renameTextBox);
        if (bottomContainer != null) windowManager.removeView(bottomContainer);
    }
}

