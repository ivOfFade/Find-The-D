import java.util.concurrent.atomic.AtomicInteger;

/** In-memory id source until a real database assigns ids. */
final class IdGenerator {
    private static final AtomicInteger NEXT = new AtomicInteger(1);
    private IdGenerator() { }
    static int next() { return NEXT.getAndIncrement(); }
}
