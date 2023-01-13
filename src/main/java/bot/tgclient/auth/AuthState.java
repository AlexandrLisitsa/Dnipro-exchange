package bot.tgclient.auth;

import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class AuthState {
    private final CountDownLatch latch = new CountDownLatch(1);

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void release() {
        latch.countDown();
    }
}
