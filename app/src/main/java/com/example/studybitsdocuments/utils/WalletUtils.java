package com.example.studybitsdocuments.utils;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;


import java.util.concurrent.ExecutionException;

public class WalletUtils {

    private static final String TAG = "SB-Documents";

    protected WalletUtils(){}

    public static String generatWalletKey(String seed) {
        String key = null;

        try {
            key = Wallet.generateWalletKey("{\"seed\": \"" + StringUtils.leftPad(seed, 32, 'x') + "\"}").get();
        } catch (ExecutionException | IndyException e) {
            Log.e(TAG, e.getMessage());
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            Thread.currentThread().interrupt();
        }
        Log.d(TAG, "SEED: " + seed);
        Log.d(TAG, "KEY: " + key);
        return key;
    }


    public static String composeConfig(String name, String path) {
        return "{  \"id\": \"" + name + "\",\"storage_config\": {\"path\": \"" + path + "\"}}";
    }

    public static String composeUnlockCredentials(String seed) {
        return "{\"key\": \"" + generatWalletKey(seed) + "\"}";
    }

    public static String composeLockCredentials(String seed) {
        return "{\"key\": \"\", \"rekey\": \"" + generatWalletKey(seed) + "\"}";
    }

}
