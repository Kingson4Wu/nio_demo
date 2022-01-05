package proxy2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class GlobalThreadPool {

    public static final ExecutorService WORK_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    public static final ExecutorService REACTOR_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

    public static final ExecutorService PIPE_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

}
