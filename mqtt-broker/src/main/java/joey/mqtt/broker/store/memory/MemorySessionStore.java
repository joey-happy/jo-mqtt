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

    public MemorySessionStore(CustomConfig config) {

    }

    @Override
    public void add(ClientSession clientSession) {
        sessionCache.put(clientSession.getClientId(), clientSession);
    }

    @Override
    public ClientSession get(String clientId) {
        return sessionCache.get(clientId);
    }

    @Override
    public void remove(String clientId) {
        sessionCache.remove(clientId);
    }

    @Override
    public int getSessionCount() {
        return sessionCache.size();
    }

    @Override
    public void close() {

    }
}
