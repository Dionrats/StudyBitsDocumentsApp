package com.example.studybitsdocuments;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.example.studybitsdocuments.utils.IndyPool;
import com.example.studybitsdocuments.utils.IndyWallet;
import com.example.studybitsdocuments.utils.WalletUtils;

import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.wallet.Wallet;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class Seeder {

    //constants
    private static final String TAG = "SB-Documents";
    private static final String WALLETPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/indy_client/";
    private static final String STUDENT_WALLET = "StudentWallet";
    private static final String SEED = "1234";


    //constructor
    private Seeder() {}

    public static CompletableFuture<Void> seed(Activity activity) {

        if(!needsSeeding()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            future.complete(null);
            return future;
        }

        IndyPool indyPool = IndyPool.getInstance();

        return CompletableFuture
                .runAsync(IndyPool::configurePool)
                .thenRun(() -> {
                    try { indyPool.open(); }
                    catch (IndyException e) {Log.e(TAG, e.getMessage());}
                })
                .thenRun(() -> Objects.requireNonNull(createWallet(SEED)))
                .thenRun(() -> createDiD(activity)
                        .thenApply(result -> Log.d(TAG, "WalletDiD: " + result.getDid())))
                .thenRun(() -> {
                    IndyWallet.getInstance().close();
                    IndyPool.clean();
                    IndyWallet.clean();
                });

    }

    private static boolean needsSeeding() {
        return !new File(WALLETPATH + STUDENT_WALLET + "/sqlite.db").exists();
    }

    private static CompletableFuture<Void> createWallet(String seed) {
        try {
            return Wallet.createWallet(
                    WalletUtils.composeConfig(STUDENT_WALLET, WALLETPATH),
                    WalletUtils.composeUnlockCredentials(seed));
        } catch (IndyException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    private static CompletableFuture<DidResults.CreateAndStoreMyDidResult> createDiD(Activity activity) {
        Log.d(TAG, "Creating DiD");
        CompletableFuture<DidResults.CreateAndStoreMyDidResult> future = new CompletableFuture<>();
        try(IndyWallet indyWallet = new IndyWallet.Builder()
                                            .path(WALLETPATH)
                                            .name(STUDENT_WALLET)
                                            .seed(SEED)
                                            .build()
        ) {
             indyWallet.unlock(activity)
                .thenApply(wallet -> {
                    try {Did.createAndStoreMyDid(wallet, "{}").thenApply(future::complete);}
                    catch (IndyException e) { Log.e(TAG, e.getMessage());}
                    return true;
                });
        } catch (IndyException e) {
            Log.e(TAG,e.getMessage());
        }
        return future;
    }
}
