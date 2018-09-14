package com.oceanai;

class Test {
    private int a = 0;
    public Test(int a) {
        this.a = a;
    }
    @Override
    public void finalize() {
        System.out.println("Test " + a + " 被回收");
    }
}

class TestReference implements Runnable{
    private Test test;
    public TestReference() {
        test = new Test(1);
    }

    @Override
    public void run() {
        int i = 0;

        while (true) {
            try {
                Test t = new Test(++i);
                //t.finalize();
                System.out.println("Test " + i + " 被创建");
                Thread.sleep(1);
                /*if (i == 3) {
                    break;
                }*/
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class ReferenceTest {
    public static void main(String[] args) throws Exception{
        TestReference testReference = new TestReference();
        Thread thread = new Thread(testReference);
        thread.start();

        Thread.sleep(100000);
    }

}
