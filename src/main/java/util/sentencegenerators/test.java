package util.sentencegenerators;

import java.util.Timer;
import java.util.TimerTask;

public class test {

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.schedule(new TestTask(), 0, 1000);
    }

    private static class TestTask extends TimerTask {
        public void run() {
            System.out.println("Message");
        }
    }
}
