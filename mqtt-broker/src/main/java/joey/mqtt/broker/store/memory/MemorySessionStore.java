package joey.mqtt.broker.store.memory;

import joey.mqtt.broker.config.CustomConfig;
import joey.mqtt.broker.core.client.ClientSession;
import joey.mqtt.broker.store.ISessionStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 内存session存储
 *
 * @author Joey
 * @date 2019/7/22
 */
public class MemorySessionStore implements ISessionStore {
    private ConcurrentHashMap<String, ClientSession> sessionCache = new ConcurrentHashMap<>();

    private LongAdder counter = new LongAdder();

    public MemorySessionStore(CustomConfig config) {

    }

    @Override
    public void add(ClientSession clientSession) {
        sessionCache.put(clientSession.getClientId(), clientSession);
        counter.increment();
    }

    @Override
    public ClientSession get(String clientId) {
        return sessionCache.get(clientId);
    }

    @Override
    public void remove(String clientId) {
        sessionCache.remove(clientId);
        counter.decrement();
    }

    @Override
    public long sessionCount() {
        return counter.longValue();
    }

    @Override
    public void close() {

    }
}
