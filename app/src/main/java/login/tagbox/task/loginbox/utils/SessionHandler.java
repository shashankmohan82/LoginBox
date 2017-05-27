package login.tagbox.task.loginbox.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import login.tagbox.task.loginbox.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by shashank on 5/27/2017.
 */
public class SessionHandler {

    private static final String SESSION_PREFS_NAME = "SessionPreference";

    public static int readSessionFlagFromPreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        int sessonFlag = prefs.getInt(context.getString(R.string.preference_session_flag), 0);
        return sessonFlag;
    }

    public static void writeSessionFlagToPreference(Context context, int sessionFlag) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(context.getString(R.string.preference_session_flag), sessionFlag);
        editor.apply();
    }

    public static String readUserNameFromPreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String sessonFlag = prefs.getString(context.getString(R.string.preference_username), null);
        return sessonFlag;
    }

    public static void writeUserNameToPreference(Context context, String userName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(context.getString(R.string.preference_username),userName);
        editor.apply();
    }

    public static void encryptPasswordUsingKeyStore(Context context,String password) {
        try {
            PasswordEncryption.createKeys(context,context.getString(R.string.keystore_alias));
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        //encryptng password using keystore
        PasswordEncryption passwordEncryption = new PasswordEncryption();
        String encryptedValue = passwordEncryption.encryptString(context.getString(R.string.keystore_alias),
                password);

        //storing cipher value of password in shared preference for security
        SharedPreferences.Editor editor = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(context.getString(R.string.preference_password_hashed),
                encryptedValue);
        editor.apply();
    }

    public static String decryptPasswordFromKeyStore(Context context) {

        //retrieving password cipher value in shared preference
        SharedPreferences prefs = context.getSharedPreferences(SESSION_PREFS_NAME, MODE_PRIVATE);
        String passwordHash = prefs.getString(context.getString(R.string.preference_password_hashed), null);

        //decrypt using keystore
        PasswordEncryption passwordEncryption = new PasswordEncryption();
        return passwordEncryption.decryptString("password_alias",passwordHash);
    }
}
