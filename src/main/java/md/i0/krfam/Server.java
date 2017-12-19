package md.i0.krfam;

import android.content.Intent;
import android.content.pm.PackageManager;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.msgpack.core.MessagePack;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.OutputStream;
import java.util.Random;

//import org.apache.commons.codec.binary.Base64;



public class Server {
    public String codeName;
    public String name;
    public String className = "com.aniplex.kirarafantasia";
    public boolean enabled = false;
    public boolean installed = false;
    public int failCheckCount = 0;
    public String uuid;
    public String accessToken;
    public String myCode;
    public File adFile;
    public boolean error = false;
    public String EncryptKey = "7gyPmqc54dVNB3Te6pIpd2THj2y3hjOP";
    public int EncryptPasswordCount = 16;
    public String FilePrefix = "files/";
    public String persistentDataPath = "/sdcard/Android/data/" + className;
    public String SaveFile = persistentDataPath + "/" + FilePrefix + "a.d";
    public String SaveFileBak = persistentDataPath + "/" + FilePrefix + "a.d2";
    public char[] PasswordChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();


    public String createPassword() {
        Random random = new Random();
        char[] str = new char[EncryptPasswordCount];
        for(int i = 0; i < EncryptPasswordCount; ++i) {
            str[i] = PasswordChars[random.nextInt(PasswordChars.length)];
        }
        return new String(str);
    }


    public static Server getServer(String code){
        for (Server s : KRFAM.serverList){
            if (s.codeName.equals(code)){
                return s;
            }
        }
        return null;
    }

    public Server(String nCode, String nName, String nClassName) {
        codeName = nCode;
        name = nName;
        className = nClassName;
        persistentDataPath = "/sdcard/Android/data/" + className;
        SaveFile = persistentDataPath + "/" + FilePrefix + "a.d";
        SaveFileBak = persistentDataPath + "/" + FilePrefix + "a.d2";
        adFile = new File(SaveFile);
        updateEnabled();
        updateInstalled();
        updateFromADFile();
    }







    public void updateEnabled() {
        enabled = KRFAM.sharedPreferences.getBoolean(codeName, false);
    }

    public void updateInstalled() {
        PackageManager pm = KRFAM.getContext().getPackageManager();
        try {
            pm.getPackageInfo(className, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }

    }

    public void deleteADFile(){
        if (adFile.exists()){
            adFile.delete();
            updateFromADFile();
            KRFAM.delayAction(new Runnable() {
                @Override
                public void run() {
                    if (uuid.equals("")){
                        KRFAM.Toast(codeName + " A.D File Deleted");
                    }
                }
            },500);
        }
    }

    public void updateFromADFile() {
        updateFromADFile(1);
    }

    public void updateFromADFile(int attempt) {
        error = false;
        if (attempt > 2) {
            failCheckCount++;
            if (failCheckCount > 5) {

                KRFAM.log(codeName + " > Failed to read A.D > Abort");
                KRFAM.Toast("Failed to access '" + codeName + "' A.D.\nDid you allow KRFAM root access?");
                return;
            }
            KRFAM.delayAction(new Runnable() {
                @Override
                public void run() {
                    updateFromADFile();
                }
            }, 200*failCheckCount);
            error = true;
            return;
        }
        if (installed == true) {
            if (adFile.exists()) {
                try {
                    boolean flag1 = false;

                    InputStream is = new FileInputStream(SaveFile);
                    LittleEndianDataInputStream binaryReader = new LittleEndianDataInputStream(is);

                    int num1 = binaryReader.readInt();
                    int num2 = num1 & 0x7F;
                    KRFAM.log("Save data Version: " + ((num1 & 65280) >> 8));
                    int count1 = binaryReader.readByte() - num2;

                    byte[] iv = new byte[count1];
                    KRFAM.log("IV Length: " + count1);
                    binaryReader.read(iv, 0, count1);
                    for (int index = 0; index < iv.length; ++index) iv[index] -= (byte) (96 + (int) (byte) index);
                    String _iv = new String(iv);
                    KRFAM.log("IV: " + _iv);
                    int count2 = binaryReader.readInt();
                    byte[] numArray = new byte[count2];
                    binaryReader.read(numArray, 0, count2);

                    if (!flag1)
                    {
                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        SecretKeySpec skeySpec = new SecretKeySpec(this.EncryptKey.getBytes(), "AES");
                        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(iv));
                        byte[] decrypted = cipher.doFinal(numArray);


                        ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());
                        SaveData value = objectMapper.readValue(decrypted, SaveData.class);
                        accessToken = value.m_AccessToken;
                        uuid = value.m_UUID;
                        myCode = value.m_MyCode;
                        KRFAM.log("at: " + value.m_AccessToken);
                        KRFAM.log("uu: " + value.m_UUID);
                        KRFAM.log("mc: " + value.m_MyCode);
                    }
                    KRFAM.log(codeName + " > Updated from A.D File");
                    failCheckCount = 0;
                } catch (Exception ex) {
                    KRFAM.log(ex);
                    KRFAM.log(codeName + " > Failed to Read A.D File - Attempting Fix");
                    KRFAM.forcePermission(adFile);
                    KRFAM.forcePermission(adFile.getParentFile());
                    updateFromADFile(attempt + 1);
                }
            } else {
                KRFAM.log(codeName + " > No A.D File exists");
                uuid = "";
                accessToken = "";
                myCode = "";
            }
        } else {
            uuid = "";
            accessToken = "";
        }
    }

    public boolean writeToGameEngineActivity(int attempt, String user, String pass, String code) {
        if (attempt > 5) {
            KRFAM.Toast("Failed to write to '" + codeName + "' A.D File.\nHave you allowed KRFAM to have root access?");
            return false;
        }
        if (installed == true) {

            SaveData value = new SaveData();
            value.m_UUID = user;
            value.m_AccessToken = pass;
            value.m_MyCode = code;
            value.m_ConfirmedVer = 0;

            String _iv = createPassword();
            byte[] iv = _iv.getBytes();

            KRFAM.log("iv " + _iv);
            try {


                MessagePack.PackerConfig config = new MessagePack.PackerConfig().withStr8FormatSupport(false);
                ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory(config));
                byte[] bytes = objectMapper.writeValueAsBytes(value);

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                SecretKeySpec skeySpec = new SecretKeySpec(this.EncryptKey.getBytes(), "AES");
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(iv));
                byte[] encrypted = cipher.doFinal(bytes);

                OutputStream is = new FileOutputStream(SaveFile);
                LittleEndianDataOutputStream binaryWriter = new LittleEndianDataOutputStream(is);

                int num1 = new Random().nextInt();

                byte num2 = (byte) (num1 & 127);
                int num3 = (int) ((long) num1 & 4294902015L) | 65280 & 19 << 8; // 19 denotes 171101
                /* 16 - 20: _170404,  _170713,  _171101,  _latest, */

                binaryWriter.writeInt(num3);

                for (int index = 0; index < iv.length; ++index) iv[index] += (byte) (96 + (int) (byte) index);

                binaryWriter.writeByte((byte) ((int) iv.length + (int) num2));
                binaryWriter.write(iv);

                binaryWriter.writeInt(encrypted.length);
                binaryWriter.write(encrypted);

            }  catch (Exception ex) {
                KRFAM.log(ex);
                KRFAM.log(codeName + " > Failed to Write A.D File");
                KRFAM.forcePermission(adFile);
                KRFAM.forcePermission(adFile.getParentFile());
            }

            KRFAM.log("saved uu " + uuid);
            KRFAM.log("saved at " + accessToken);

            updateFromADFile();
            if (uuid.equals(user) && accessToken.equals(pass)) {
                return true;
            } else {
                KRFAM.forcePermission(adFile);
                KRFAM.forcePermission(adFile.getParentFile());
                return writeToGameEngineActivity(attempt + 1, user, pass, code);
            }
        } else {
            KRFAM.Toast(codeName + " not installed");
            return false;
        }
    }

    public void forceCloseApp() {
        KRFAM.executeRootCommand("am force-stop " + className);
    }

    public void openApp() {
        KRFAM.log("Server[" + codeName + "] > openApp");
        if (KRFAM.CLOSE_BUTTON || KRFAM.OVERLAY_NAME) {
            KRFAM.getContext().startService(new Intent(KRFAM.getContext(), OverlayService.class));
        }
        Intent launchIntent = KRFAM.getContext().getPackageManager().getLaunchIntentForPackage(className);
        KRFAM.getContext().startActivity(launchIntent);
    }
}