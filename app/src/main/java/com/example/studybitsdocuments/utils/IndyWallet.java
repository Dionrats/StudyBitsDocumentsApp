package com.example.studybitsdocuments.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.studybitsdocuments.exceptions.WalletException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.util.concurrent.CompletableFuture;


public class IndyWallet implements AutoCloseable {

    //Constants
    private static final String TAG = "SB-Documents";

    private static final int REQUEST_CODE_READ = 0;
    private static final int REQUEST_CODE_WRITE = 1;
    private static final String DEFAULT_NAME = "";
    private static final String DEFAULT_PATH = "/";
    private static final String DEFAULT_SEED = "00000000000000000000000000000001";

    //Attributes
    private static String name;
    private static String path;
    private static String seed;

    private static Wallet wallet;
    private static boolean locked;

    private static IndyWallet instance;
    private static boolean build = false;

    //Builder
    public static class Builder {
        private String name = DEFAULT_NAME;
        private String path = DEFAULT_PATH;
        private String seed = DEFAULT_SEED;
        private Wallet wallet = null;
        private boolean locked = true;

        public Builder name(String name){this.name = name; return this;}
        public Builder path(String path){this.path = path; return this;}
        public Builder seed(String seed){this.seed = seed; return this;}
        public Builder wallet(Wallet wallet){this.wallet = wallet; return this;}
        public Builder locked(boolean locked){this.locked = locked; return this;}

        public IndyWallet build() {
            if(instance != null) throw new WalletException("IndyWallet has already been build");
            instance = new IndyWallet(this);
            build = true;
            return instance;
        }
    }

    //Contructor
    private IndyWallet(Builder builder) {
        name = builder.name;
        seed = builder.seed;
        path = builder.path;
        wallet = builder.wallet;
        locked = builder.locked;
    }


    //Static methods
    public static IndyWallet getInstance() {
        return isBuild() ? instance : new Builder().build();
    }

    public static void clean() {
        name = null;
        seed = null;
        path = null;
        wallet = null;
        instance = null;
        build = false;
    }

    public static boolean isBuild() {
        return build;
    }

    public static boolean isLocked() {
        return locked;
    }

    public static String getName() {
        return name;
    }

    public static String getPath() {
        return path;
    }

    public static String getSeed() {
        return seed;
    }

    //instance methods
    public void lock() throws IndyException {
        if(wallet == null) return;
        wallet.closeWallet().thenRun(() -> locked = true);
    }

    public CompletableFuture<Wallet> unlock(Activity activity) throws IndyException {
        String config = WalletUtils.composeConfig(name, path);
        String credentials = WalletUtils.composeUnlockCredentials(seed);
        checkPermissions(activity);
        CompletableFuture<Wallet> future = Wallet.openWallet(config, credentials);
        Log.d(TAG, future.toString());
        future.thenApply(result -> wallet = result)
                .thenRun(() -> locked = false);

        return future;
    }

    @Override
    public void close() {
        try {
            lock();
        } catch (IndyException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //private methods
    private void checkPermissions(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE);
    }
}
