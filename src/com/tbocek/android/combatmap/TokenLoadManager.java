package com.tbocek.android.combatmap;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.Handler;

import com.tbocek.android.combatmap.model.primitives.BaseToken;

/**
 * This class maintains a queue of tokens that need to be loaded. Each entry in
 * the queue comes with a callback that is called when the token is loaded. This
 * callback is called in the UI thread so that UI elements can update with the
 * newly loaded token.
 * 
 * Token loads can be batched, so that the callback is only called when every
 * token in the batch is loaded.
 * 
 * Registering a token to load provides a handle that allows the load action to
 * be cancelled at any time.
 * 
 * @author Tim
 * 
 */
public class TokenLoadManager {
    private TokenLoadManager() {
    }

    private static TokenLoadManager mInstance = null;

    public static TokenLoadManager getInstance() {
        if (mInstance == null) {
            mInstance = new TokenLoadManager();
        }
        return mInstance;
    }

    BlockingQueue<JobHandle> mQueue = new LinkedBlockingQueue<JobHandle>();

    private boolean isStarted;

    public interface JobCallback {
        void onJobComplete(List<BaseToken> loadedTokens);
    }

    public class JobHandle {
        private List<BaseToken> mTokensToLoad;
        private JobCallback mCallback;
        private Handler mUiThreadHandler;

        private boolean mIsCancelled = false;

        private JobHandle(List<BaseToken> tokensToLoad, JobCallback callback,
                Handler uiThreadHandler) {
            mTokensToLoad = tokensToLoad;
            mCallback = callback;
            mUiThreadHandler = uiThreadHandler;
        }

        public void cancel() {
            synchronized (this) {
                mIsCancelled = true;
            }
        }

        private boolean isCancelled() {
            synchronized (this) {
                return mIsCancelled;
            }
        }

        private void postResult() {
            mUiThreadHandler.post(new JobCallbackRunnableWrapper(mCallback,
                    mTokensToLoad));
        }
    }

    private class JobCallbackRunnableWrapper implements Runnable {
        private JobCallback mCallback;
        private List<BaseToken> mLoadedTokens;

        private JobCallbackRunnableWrapper(JobCallback callback,
                List<BaseToken> loadedTokens) {
            mCallback = callback;
            mLoadedTokens = loadedTokens;
        }

        @Override
        public void run() {
            mCallback.onJobComplete(mLoadedTokens);
        }

    }

    public JobHandle startJob(List<BaseToken> tokensToLoad,
            JobCallback callback, Handler uiThreadHandler) {
        JobHandle handle =
                new JobHandle(tokensToLoad, callback, uiThreadHandler);
        try {
            mQueue.put(handle);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return handle;
    }

    public void startThread() {
        synchronized (this) {
            if (!isStarted) {
                new TokenLoadJobThread().start();
            }
            isStarted = true;
        }
    }

    private class TokenLoadJobThread extends Thread {
        public void run() {
            while (true) {
                try {
                    JobHandle currentJob = mQueue.take();
                    handleJob(currentJob);
                    currentJob.postResult();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private void handleJob(JobHandle job) {
        for (BaseToken t : job.mTokensToLoad) {
            t.load();
            if (job.isCancelled()) {
                return;
            }
        }
    }
}
