package com.example.studybitsdocuments.utils;

import android.util.Log;
import com.example.studybitsdocuments.exceptions.WalletException;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.pool.Pool;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class IndyPool implements AutoCloseable{

    //constants
    private static final String TAG = "SB-Documents";
    private static final int PROTOCOL_VERSION = 2;
    private static final String DEFAULT_POOL_IP = "127.0.0.1";
    private static final String DEFAULT_POOL = "default_pool";

    //Attributes
    private String name;
    private String poolIP;

    private Pool pool;
    private boolean open;

    private static IndyPool instance;
    private static boolean build = false;

    //Builder
    public static class Builder {
        private String name = DEFAULT_POOL;
        private String poolIP = DEFAULT_POOL_IP;
        private Pool pool = null;
        private boolean open = false;

        public Builder name(String name){this.name = name; return this;}
        public Builder poolIP(String poolIP){this.poolIP = poolIP; return this;}
        public Builder pool(Pool pool){this.pool = pool; return this;}
        public Builder open(boolean open){this.open = open; return this;}

        public IndyPool build() {
            if(instance != null) throw new WalletException("IndyPool has already been build");
            Log.d(TAG, "Building IndyPool");
            updateInstance(new IndyPool(this));

            return instance;
        }

        private static void updateInstance(IndyPool indyPool) {
            instance = indyPool;
        }
    }

    //Contructor
    private IndyPool(Builder builder) {
        name = builder.name;
        poolIP = builder.poolIP;
        pool = builder.pool;
        open = builder.open;

        configure();
    }


    //Static methods
    public static IndyPool getInstance() {
        return isBuild()? instance : new Builder().build();
    }

    public static void clean() {
        instance = null;
        build = false;
    }

    public static CompletableFuture<Void> configure() {
        return CompletableFuture
                .runAsync(() -> {
                    try { Pool.setProtocolVersion(PROTOCOL_VERSION); }
                    catch (IndyException e) { Log.e(TAG, e.getMessage()); }
                })
                .thenRunAsync(() -> {
                    try { PoolUtils.composeConfig(DEFAULT_POOL_IP, DEFAULT_POOL); }
                    catch (IOException | IndyException e) { Log.e(TAG, e.getMessage()); }
                })
                .thenRun(IndyPool::complete);
    }

    //instance methods
    public boolean isOpen() {
        return open;
    }

    public String getName() {
        return name;
    }

    public String getPoolIP() {
        return poolIP;
    }

    public CompletableFuture<Pool> open() throws IndyException {
        Log.d(TAG, "Opening IndyPool");
        return Pool.openPoolLedger(DEFAULT_POOL, "{}")
                .thenApply(result -> pool = result);
    }

    @Override
    public void close() throws IndyException {
        pool.closePoolLedger();
    }


    //private methods
    private static boolean isBuild() {
        return build;
    }

    private static void complete() {
        build = true;
        Log.d(TAG, "Pool Ready for use");
    }
}
