/**
 * Stream API and Queues: Subscribe to BlockingQueue stream-style
 * from: https://stackoverflow.com/questions/23462209/stream-api-and-queues-subscribe-to-blockingqueue-stream-style
 */
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class QueueSpliterator<T> extends AbstractSpliterator<T> {
	static private final long DEFAULT_ESTIMATED_SIZE = 2;
	static private final int DEFAULT_CHARATISTICS = Spliterator.CONCURRENT | Spliterator.NONNULL | Spliterator.ORDERED;
	
    private final BlockingQueue<T> queue;
    private final long timeoutMs;
    
    public QueueSpliterator(BlockingQueue<T> queue, long timeoutMs) {
    	super(DEFAULT_ESTIMATED_SIZE, DEFAULT_CHARATISTICS);
        this.queue = queue;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        try {
            final T next = this.queue.poll(this.timeoutMs, TimeUnit.MILLISECONDS);
            if (next == null) {
                return false;
            }
            action.accept(next);
            return true;
        } catch (final InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        return false;
    }
}