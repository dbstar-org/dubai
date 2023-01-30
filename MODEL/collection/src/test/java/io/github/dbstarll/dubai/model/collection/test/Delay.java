package io.github.dbstarll.dubai.model.collection.test;

public final class Delay {
    private Delay() {
        // 禁止实例化
    }

    /**
     * 产生一个很小的延时.
     *
     * @throws InterruptedException 线程中断时抛出
     */
    public static void delay() throws InterruptedException {
        final Thread delay = new Thread(System.out::println);
        delay.start();
        delay.join();
    }
}
