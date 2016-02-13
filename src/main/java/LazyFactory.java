import com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundOperation;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Created by urijkravcenko on 12/02/16.
 */

public class LazyFactory {

    abstract static class MyLazy<T> implements Lazy {
        Boolean got = false;
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

            synchronized public T get() {
                if (!got) {
                    got = true;
                    result = ((Supplier) result).get();
                }
                return (T) result;
            }

        };
    }

    abstract static class MyOtherLazy<T> implements Lazy {
        AtomicMarkableReference<Object> result = new AtomicMarkableReference<Object>(null, false);

        MyOtherLazy(Supplier<T> supplier) {
            result.set(supplier, false);
        }

    }

    public static <T> Lazy<T> createLazy3(Supplier<T> supplier) {

        return new MyOtherLazy<T>(supplier) {

            public T get() {
                AtomicMarkableReference<Object> current = result;
                if (!current.isMarked()) {
                    T newResult = ((Supplier<T>) current.getReference()).get();
                    result.compareAndSet(current.getReference(), newResult, false, true);
                }
                return (T) result.getReference();
            }
        };
    }

}
