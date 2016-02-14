import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by urijkravcenko on 12/02/16.
 */
public class LazyFactoryTest {

    private static class LazyFactoryTester1<T> {
        private int runs = 0;

        LazyFactoryTester1(final Supplier<T> supplier) {
            runs = 0;
            Lazy<T> myLazy = LazyFactory.createLazy1(new Supplier<T>() {
                public T get() {
                    runs++;
                    return supplier.get();
                }
            });
            T answer = myLazy.get();
            for (int i = 0; i < 100; ++i) {
                assertSame(answer, myLazy.get());
            }

            assertEquals(1, runs);
        }
    }

    @org.junit.Test
    public void testCreateLazy1Null() {
        new LazyFactoryTester1<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy1SameIntegers() {
        new LazyFactoryTester1<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return 1;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy1RandomInteger() {
        new LazyFactoryTester1<Integer>(new Supplier<Integer>() {
            Random random = new Random(123);

            public Integer get() {
                return random.nextInt();
            }
        });
    }

    private static class LazyFactoryTester2<T> {
        volatile int runs;
        static final int n = 10;
        ArrayList<Object> answer = new ArrayList<Object>();

        LazyFactoryTester2(final Supplier<T> supplier) {
            answer.clear();
            runs = 0;
            final Lazy<T> myLazy = LazyFactory.createLazy2(new Supplier<T>() {
                public T get() {
                    runs++;
                    return supplier.get();
                }
            });
            final CyclicBarrier barrier = new CyclicBarrier(n);
            ArrayList<Thread> threads = new ArrayList<Thread>(n);
            for (int i = 0; i < n; i++) {
                final int finalI = i;
                answer.add(null);
                threads.add(new Thread(new Runnable() {
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        answer.set(finalI, myLazy.get());
                        for (int i = 0; i < 100; ++i) {
                            assertSame(answer.get(finalI), myLazy.get());
                        }
                    }
                }));
                threads.get(i).start();
            }
            for (int i = 0; i < n; ++i) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            assertEquals(1, runs);
            for (int i = 0; i < n - 1; ++i) {
                assertSame(answer.get(i), answer.get(i + 1));
            }

        }
    }

    @org.junit.Test
    public void testCreateLazy2Null() {
        new LazyFactoryTester2<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy2SameInteger() {
        new LazyFactoryTester2<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return 1;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy2RandomInteger() {
        new LazyFactoryTester2<Integer>(new Supplier<Integer>() {
            Random random = new Random(123);

            public Integer get() {
                return random.nextInt();
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy2NullThenString() {
        new LazyFactoryTester2<String>(new Supplier<String>() {
            boolean flag = true;

            public String get() {
                if (flag) {
                    flag = false;
                    return null;
                }
                return "abacaba";
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy2StringThenNull() {
        new LazyFactoryTester2<String>(new Supplier<String>() {
            boolean flag = true;

            public String get() {
                if (flag) {
                    flag = false;
                    return "abacaba";
                }
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy2StringOrNullRandomly() {
        new LazyFactoryTester2<String>(new Supplier<String>() {
            Random random = new Random(123);

            public String get() {
                if (random.nextBoolean()) {
                    return "abacaba";
                }
                return null;
            }
        });
    }

    static class LazyFactoryTester3<T> {
        static final int n = 10;
        ArrayList<Object> answer = new ArrayList<Object>();

        LazyFactoryTester3(final Supplier<T> supplier) {
            answer.clear();
            final Lazy<T> myLazy = LazyFactory.createLazy3(supplier);
            final CyclicBarrier barrier = new CyclicBarrier(n);
            ArrayList<Thread> threads = new ArrayList<Thread>(n);
            for (int i = 0; i < n; i++) {
                final int finalI = i;
                answer.add(null);
                threads.add(new Thread(new Runnable() {
                    public void run() {
                        try {
                            barrier.await();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        answer.set(finalI, myLazy.get());
                        for (int i = 0; i < 100; ++i) {
                            assertSame(answer.get(finalI), myLazy.get());
                        }
                    }
                }));
                threads.get(i).start();
            }
            for (int i = 0; i < n; ++i) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < n - 1; ++i) {
                assertSame(answer.get(i), answer.get(i + 1));
            }

        }
    }

    @org.junit.Test
    public void testCreateLazy3Null() {
        new LazyFactoryTester3<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3SameInteger() {
        new LazyFactoryTester3<Integer>(new Supplier<Integer>() {
            public Integer get() {
                return 1;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3RandomInteger() {
        new LazyFactoryTester3<Integer>(new Supplier<Integer>() {
            Random random = new Random(123);

            public Integer get() {
                return random.nextInt();
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3NullThenString() {
        new LazyFactoryTester3<String>(new Supplier<String>() {
            boolean flag = true;

            public String get() {
                if (flag) {
                    flag = false;
                    return null;
                }
                return "abacaba";
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3StingThenNull() {
        new LazyFactoryTester3<String>(new Supplier<String>() {
            boolean flag = true;

            public String get() {
                if (flag) {
                    flag = false;
                    return "abacaba";
                }
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3NullOrStringRandom() {
        new LazyFactoryTester3<String>(new Supplier<String>() {
            Random random = new Random(123);

            public String get() {
                if (random.nextBoolean()) {
                    return "abacaba";
                }
                return null;
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3Supplier() {
        new LazyFactoryTester3<Supplier<String>>(new Supplier<Supplier<String>>() {
            public Supplier<String> get() {
                return new Supplier<String>() {
                    Random random = new Random(123);

                    public String get() {
                        if (random.nextBoolean()) {
                            return "abacaba";
                        }
                        return null;
                    }
                };
            }
        });
    }

    @org.junit.Test
    public void testCreateLazy3SupplierOrNullRandomly() {
        new LazyFactoryTester3<Supplier<String>>(new Supplier<Supplier<String>>() {
            Random random = new Random(123);

            public Supplier<String> get() {
                if (random.nextBoolean()) {
                    return null;
                }
                return new Supplier<String>() {
                    Random random = new Random(1234);

                    public String get() {
                        if (random.nextBoolean()) {
                            return "abacaba";
                        }
                        return null;
                    }
                };
            }
        });
    }
}