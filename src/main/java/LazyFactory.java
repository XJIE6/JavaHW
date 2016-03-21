import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

/**
 * Created by urijkravcenko on 12/02/16.
 */

public class LazyFactory {

    abstract private static class MyLazy<T> implements Lazy<T> {
        boolean got = false;
        Object result = null;

        MyLazy(Supplier<T> supplier) {
            result = supplier;
        }
    }

    public static <T> Lazy<T> createLazy1(Supplier<T> supplier) {

        return new MyLazy<T>(supplier) {

            public T get() {
                if (!got) {
                    got = true;
                    result = ((Supplier) result).get();
                }
                return (T) result;
            }

        };
    }

    public static <T> Lazy<T> createLazy2(Supplier<T> supplier) {

        return new MyLazy<T>(supplier) {
            public T get() {
                if (!got) {
                    synchronized (this) {
                        if (!got) {
                            result = ((Supplier) result).get();
                            got = true;
                        }
                    }
                }
                return (T) result;
            }

        };
    }

    abstract private static class MyOtherLazy<T> implements Lazy<T> {
        AtomicMarkableReference<Object> result = new AtomicMarkableReference<Object>(null, false);

        MyOtherLazy(Supplier<T> supplier) {
            result.set(supplier, false);
        }

    }

    public static <T> Lazy<T> createLazy3(Supplier<T> supplier) {

        return new MyOtherLazy<T>(supplier) {

            public T get() {
                boolean currentMark = result.isMarked();
                Object currentObject = result.getReference();
                if (currentMark != result.isMarked()) {
                    currentMark = result.isMarked();
                }
                if (!currentMark) {
                    T newResult = ((Supplier<T>) currentObject).get();
                    result.compareAndSet(currentObject, newResult, currentMark, true);
                }
                return (T) result.getReference();
            }
        };
    }

}
