package sanekp.humster.statistics;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sanek_000 on 7/28/2014.
 */
public class ClientInfo {
    private InetSocketAddress inetSocketAddress;
    private AtomicLong queries = new AtomicLong(1);
    private Date last = new Date();

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public long getQueries() {
        return queries.get();
    }

    public void incrementQueries() {
        queries.incrementAndGet();
    }

    public Date getLast() {
        return last;
    }
}
