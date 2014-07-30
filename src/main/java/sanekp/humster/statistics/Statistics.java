package sanekp.humster.statistics;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sanek_000 on 7/27/2014.
 */
public class Statistics {
    private static final int MAX_QUEUE = 16;
    private final Map<String, ClientInfo> uniqueQueries = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> redirects = new ConcurrentHashMap<>();
    private final Queue<ConnectionInfo> connectionsInfo = new LinkedList<>();
    private AtomicLong totalQueries = new AtomicLong(0);
    private AtomicLong activeConnections = new AtomicLong(0);

    public void addRedirect(String url) {
        AtomicLong atomicLong;
        synchronized (redirects) {
            atomicLong = redirects.get(url);
            if (atomicLong == null) {
                redirects.put(url, new AtomicLong(1));
            }
        }
        if (atomicLong != null) {
            atomicLong.incrementAndGet();
        }
    }

    public Map<String, AtomicLong> getRedirects() {
        return redirects;
    }

    public long getActiveConnections() {
        return activeConnections.get();
    }

    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }

    public long getTotalQueries() {
        return totalQueries.get();
    }

    public Map<String, ClientInfo> getUniqueQueries() {
        return uniqueQueries;
    }

    public void addQuery(InetSocketAddress inetSocketAddress) {
        totalQueries.incrementAndGet();
        String hostAddress = inetSocketAddress.getAddress().getHostAddress();
        ClientInfo clientInfo;
        synchronized (uniqueQueries) {
            clientInfo = uniqueQueries.get(hostAddress);
            if (clientInfo == null) {
                ClientInfo newClientInfo = new ClientInfo();
                newClientInfo.setInetSocketAddress(inetSocketAddress);
                uniqueQueries.put(hostAddress, newClientInfo);
            }
        }
        if (clientInfo != null) {
            clientInfo.incrementQueries();
            clientInfo.getLast().setTime(System.currentTimeMillis()); //  no matter
        }
    }

    public void addConnection(ConnectionInfo connectionInfo) {
        synchronized (connectionsInfo) {
            connectionsInfo.add(connectionInfo);
            if (connectionsInfo.size() > MAX_QUEUE) {
                connectionsInfo.remove();
            }
        }
    }

    /**
     * Access to this have to be synchronized
     *
     * @return queue of 16 last connections
     */
    public Queue<ConnectionInfo> getConnectionsInfo() {
        return connectionsInfo;
    }
}
