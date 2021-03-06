package io.spokestack.spokestack.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A subclass of {@link FutureTask} that allows the user to register a callback
 * to be executed when the task's result is available.
 *
 * @param <T> The type of result produced by the task and expected by the
 *            optional callback.
 */
public class AsyncResult<T> extends FutureTask<T> {

    private Callback<T> completionCallback;

    /**
     * Create a new task.
     *
     * @param callable The {@code Callable} representing the task's work.
     */
    public AsyncResult(@NotNull Callable<T> callable) {
        super(callable);
    }

    /**
     * Register a callback to be executed when the task's result is available.
     * If the task has already been completed, the callback will be called
     * immediately.
     *
     * @param callback The function to be called with the task's result.
     */
    public void registerCallback(Callback<T> callback) {
        this.completionCallback = callback;
        if (isDone()) {
            try {
                callback.call(get());
            } catch (CancellationException | InterruptedException
                  | ExecutionException e) {
                callback.onError(e);
            }
        }
    }

    @Override
    protected void done() {
        if (this.completionCallback != null) {
            try {
                completionCallback.call(get());
            } catch (CancellationException | InterruptedException
                  | ExecutionException e) {
                completionCallback.onError(e);
            }
        }
        super.done();
    }
}
