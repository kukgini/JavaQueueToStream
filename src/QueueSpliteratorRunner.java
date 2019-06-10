import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class QueueSpliteratorRunner {

    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static final Stream<String> stream = StreamSupport.stream(new QueueSpliterator<String>(queue, 60000), false);
    private static final ExecutorService executors = Executors.newCachedThreadPool();

    private static boolean quit = false;
    public static Runnable sendQuitSignal = () -> quit = true;
    public static Supplier<Boolean> quitSignalReceived = () -> quit == true;
    public static Supplier<Boolean> queueIsExhausted = () -> queue.size() == 0;
    public static Supplier<Boolean> exitCreteria = () -> quitSignalReceived.get() && queueIsExhausted.get();

    public static void main(String[] args) {
        executors.submit(() -> doUntil(quitSignalReceived, () -> queue.offer("A"),1));
        executors.submit(() -> doUntil(quitSignalReceived, () -> queue.offer("B"),1));
        executors.submit(() -> doUntil(quitSignalReceived, () -> queue.offer("C"),1));

        executors.submit(() -> doAfter(10, sendQuitSignal));
        executors.submit(() -> stream.parallel().forEach(e -> {
        	System.out.format("consumer thread[%s] got: %s%n", Thread.currentThread().getId(), e);
        }));

        waitUntil(exitCreteria,1);

        stream.close();
        executors.shutdownNow();

        System.out.println("system exit.");
    }

    public static void doWhile(Supplier<Boolean> condition, Runnable r, int interval) {
        while(condition.get()) {
            r.run();
            sleep(interval);
        }
    }

    public static void doUntil(Supplier<Boolean> condition, Runnable r, int interval) {
        while(condition.get() == false) {
            r.run();
            sleep(interval);
        }
    }

    public static void doAfter(int delay, Runnable r) {
        sleep(delay);
        r.run();
    }

    public static void waitUntil(Supplier<Boolean> condition, int interval) {
        while(condition.get() == false) {
            sleep(interval);
        }
    }

    public static void sleep(int interval) {
        try { Thread.sleep(interval); } catch (InterruptedException e) {}
    }
}