package com.thinkaurelius.thrift;

import com.thinkaurelius.thrift.test.*;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: chenzhen
 * Date: 14-1-21
 * Time: 下午8:20
 * To change this template use File | Settings | File Templates.
 */
public class BenchmarkTest {
    public static void main(String[] args) throws Exception {
        BenchmarkTest bt = new BenchmarkTest();
        System.out.println("start");
        AbstractDisruptorTest.prepareTest(true);
        //bt.start();
    }
    public void start()
    {
        int threadCount = 1000;

        ThreadGroup group = new ThreadGroup("Benchmark");
        List<Thread> threads = new ArrayList<Thread>();
        for (int x = 0; x < threadCount; ++x) {
            Thread t = new EchoThread(x, group);
            threads.add(t);
            t.start();
        }
    }
    public static class EchoThread extends Thread {
        private  int in;
        public EchoThread(int in, ThreadGroup group) {
            super(group, "EchoThread-" + in);
             this.in = in;
        }

        @Override
        public void run() {
            int i = 0;
            boolean f = true;
            while (f) {
                i++;
                try{
                    Thread.sleep(1000);

                    TTransport transport = new TFramedTransport(new TSocket("127.0.0.1", 8080));
                    if (!transport.isOpen())
                        transport.open();

                    TestService.Client client = new TestService.Client(new org.apache.thrift.protocol.TBinaryProtocol(transport, true, true));

                    Response responseAdd = client.invoke(new Request().setId(in*i)
                            .setArg1(toByteBuffer(1))
                            .setArg2(toByteBuffer(1))
                            .setArgType(ArgType.INT)
                            .setOperationType(OperationType.ADD));
                    transport.close();
                 }catch (Exception e){
                    e.printStackTrace();
                }
                Stat.getInstance().inc();

            }
        }

    }
    protected static ByteBuffer toByteBuffer(int integer)
    {
        ByteBuffer b = ByteBuffer.allocate(4).putInt(integer);
        b.clear();

        return b;
    }

    public static class Stat {
        private AtomicLong _count = new AtomicLong(0);

        private static Stat _instance = new Stat();

        public static Stat getInstance() {
            return _instance;
        }

        private Stat() {
            _printer = new RatePrinter(_count);
            _printer.setDaemon(true);
            _printer.start();
        }

        public void inc() {
            _count.incrementAndGet();
        }

        private RatePrinter _printer;

        private static class RatePrinter extends Thread {
            private long _last;

            private AtomicLong _c;

            public RatePrinter(AtomicLong c) {
                _c = c;
            }

            @Override
            public void run() {
                while (true) {
                    try {
                        long current = _c.get();
                        System.out.println("Rate: " + (current - _last) + " req/s");
                        _last = current;
                        Thread.sleep(1000L);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}




