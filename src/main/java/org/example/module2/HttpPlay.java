package org.example.module2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HttpPlay {
    private static final int NUM_USERS = 1;
    public static void main(String[] args) {
        ThreadFactory factory = Thread.ofVirtual().name("request-handler", 0).factory();
        try(ExecutorService svc = Executors.newThreadPerTaskExecutor(factory)) {
            IntStream.range(0, NUM_USERS)
                    .forEach(i -> {
                        svc.submit(new UserRequestHandler());
                    });
        }
    }

    private static class UserRequestHandler implements Callable<String> {
        @Override
        public String call() throws Exception {
            try(ExecutorService svc = Executors.newVirtualThreadPerTaskExecutor()) {

                return CompletableFuture
                        .supplyAsync(this::dbCall, svc)
                        .thenCombine(CompletableFuture.supplyAsync(this::restCall, svc),
                                (result1, result2) ->  "[" + result1 + "," + result2 + "]")
                        .thenApply(result -> {
                            String r = externalCall();
                            System.out.println("combined output = " + r);
                            return "[" + result + "," + r + "]";
                        })
                        .join();
            }
        }

        private String concurrentCallFunctional() throws InterruptedException {

            try(ExecutorService svc = Executors.newVirtualThreadPerTaskExecutor()) {
                String result = svc.invokeAll(Arrays.asList(this::dbCall, this::restCall))
                        .stream()
                        .map(f -> {
                            try {
                                return (String) f.get();
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .collect(Collectors.joining(","));
                return "[" + result + "]";
            }
        }

        private String concurrentCallWithFutures() throws ExecutionException, InterruptedException {
            try(ExecutorService svc = Executors.newVirtualThreadPerTaskExecutor()) {
                long start = System.currentTimeMillis();
                Future<String> result1 = svc.submit(this::dbCall);
                Future<String> result2 = svc.submit(this::restCall);
                String result = String.format("[%s,%s]", result1.get(), result2.get());
                System.out.println(result);
                long end = System.currentTimeMillis();
                System.out.println("time = " + (end - start));
                return result;
            }
        }

        private String sequentialCall() {
            long start = System.currentTimeMillis();
            String result1 = dbCall(); // 2 secs
            String result2 = restCall(); // 4 secs

            String result = String.format("[%s,%s]", result1, result2);
            long end = System.currentTimeMillis();
            System.out.println("time = " + (end - start));
            System.out.println(result);
            return result;
        }

        private String dbCall() {
            NetworkCaller caller = new NetworkCaller("data");
            try {
                return caller.makeCall(2);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private String restCall() {
            NetworkCaller caller = new NetworkCaller("rest");
            try {
                return caller.makeCall(5);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private String externalCall() {
            try {
                NetworkCaller caller = new NetworkCaller("extn");
                caller.makeCall(4);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class NetworkCaller {
        private final String callName;
        public NetworkCaller(String callName) {
            this.callName = callName;
        }

        public String makeCall(int secs)
                throws URISyntaxException, IOException {

            System.out.println("callName: " + callName + " BEG call: " + Thread.currentThread());
            URI uri = new URI("http://httpbin.org/delay/" + secs);
            try {
                try(InputStream stream = uri.toURL().openStream()) {
                    return new String(stream.readAllBytes());
                }
            } finally {
                System.out.println("callName: " + callName + " END call: " + Thread.currentThread());
            }
        }
    }
}
