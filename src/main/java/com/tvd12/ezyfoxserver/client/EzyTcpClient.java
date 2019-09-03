package com.tvd12.ezyfoxserver.client;

import com.tvd12.ezyfoxserver.client.setup.EzySetup;
import com.tvd12.ezyfoxserver.client.setup.EzySimpleSetup;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyCommand;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.entity.EzyApp;
import com.tvd12.ezyfoxserver.client.entity.EzyArray;
import com.tvd12.ezyfoxserver.client.entity.EzyData;
import com.tvd12.ezyfoxserver.client.entity.EzyEntity;
import com.tvd12.ezyfoxserver.client.entity.EzyMeAware;
import com.tvd12.ezyfoxserver.client.entity.EzyUser;
import com.tvd12.ezyfoxserver.client.entity.EzyZone;
import com.tvd12.ezyfoxserver.client.entity.EzyZoneAware;
import com.tvd12.ezyfoxserver.client.logger.EzyLogger;
import com.tvd12.ezyfoxserver.client.manager.EzyAppManager;
import com.tvd12.ezyfoxserver.client.manager.EzyHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzyPingManager;
import com.tvd12.ezyfoxserver.client.manager.EzySimpleHandlerManager;
import com.tvd12.ezyfoxserver.client.manager.EzySimplePingManager;
import com.tvd12.ezyfoxserver.client.request.EzyRequest;
import com.tvd12.ezyfoxserver.client.request.EzyRequestSerializer;
import com.tvd12.ezyfoxserver.client.request.EzySimpleRequestSerializer;
import com.tvd12.ezyfoxserver.client.socket.EzyPingSchedule;
import com.tvd12.ezyfoxserver.client.socket.EzySocketClient;
import com.tvd12.ezyfoxserver.client.socket.EzyTcpSocketClient;

import java.util.HashSet;
import java.util.Set;

import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientConnectable;
import static com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatuses.isClientReconnectable;

/**
 * Created by tavandung12 on 9/20/18.
 */

public class EzyTcpClient
        extends EzyEntity
        implements EzyClient, EzyMeAware, EzyZoneAware {

    protected EzyUser me;
    protected EzyZone zone;
    protected final String name;
    protected final EzySetup settingUp;
    protected final EzyClientConfig config;
    protected final EzyPingManager pingManager;
    protected final EzyHandlerManager handlerManager;
    protected final EzyRequestSerializer requestSerializer;

    protected EzyConnectionStatus status;
    protected final Set<Object> unloggableCommands;

    protected final EzySocketClient socketClient;
    protected final EzyPingSchedule pingSchedule;

    public EzyTcpClient(EzyClientConfig config) {
        this.config = config;
        this.name = config.getClientName();
        this.status = EzyConnectionStatus.NULL;
        this.pingManager = new EzySimplePingManager();
        this.pingSchedule = new EzyPingSchedule(this);
        this.handlerManager = new EzySimpleHandlerManager(this);
        this.requestSerializer = new EzySimpleRequestSerializer();
        this.settingUp = new EzySimpleSetup(handlerManager);
        this.unloggableCommands = newUnloggableCommands();
        this.socketClient = newSocketClient();
    }

    protected Set<Object> newUnloggableCommands() {
        Set<Object> set = new HashSet<Object>();
        set.add(EzyCommand.PING);
        set.add(EzyCommand.PONG);
        return set;
    }

    protected EzySocketClient newSocketClient() {
        EzyTcpSocketClient client = new EzyTcpSocketClient();
        client.setPingSchedule(pingSchedule);
        client.setPingManager(pingManager);
        client.setHandlerManager(handlerManager);
        client.setReconnectConfig(config.getReconnect());
        client.setUnloggableCommands(unloggableCommands);
        return client;
    }

    public EzySetup setup() {
        return settingUp;
    }

    public void connect(String host, int port) {
        try {
            if (!isClientConnectable(status)) {
                EzyLogger.warn("client has already connected to: " + host + ":" + port);
                return;
            }
            preconnect();
            socketClient.connectTo(host, port);
            setStatus(EzyConnectionStatus.CONNECTING);
        } catch (Exception e) {
            EzyLogger.error("connect to server error", e);
        }
    }

    public boolean reconnect() {
        if (!isClientReconnectable(status)) {
            String host = socketClient.getHost();
            int port = socketClient.getPort();
            EzyLogger.warn("client has already connected to: " + host + ":" + port);
            return false;
        }
        preconnect();
        boolean success = socketClient.reconnect();
        if (success)
            setStatus(EzyConnectionStatus.RECONNECTING);
        return success;
    }

    protected void preconnect() {
        this.me = null;
        this.zone = null;
    }

    public void disconnect(int reason) {
        socketClient.disconnect(reason);
    }

    public void send(EzyRequest request) {
        Object cmd = request.getCommand();
        EzyData data = request.serialize();
        send((EzyCommand) cmd, (EzyArray) data);
    }

    public void send(EzyCommand cmd, EzyArray data) {
        EzyArray array = requestSerializer.serialize(cmd, data);
        if (socketClient != null) {
            socketClient.sendMessage(array);
            printSentData(cmd, data);
        }
    }

    public void processEvents() {
        socketClient.processEventMessages();
    }

    public String getName() {
        return name;
    }

    public EzyClientConfig getConfig() {
        return config;
    }

    public EzyZone getZone() {
        return zone;
    }

    public void setZone(EzyZone zone) {
        this.zone = zone;
    }

    public EzyUser getMe() {
        return me;
    }

    public void setMe(EzyUser me) {
        this.me = me;
    }

    public EzyConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(EzyConnectionStatus status) {
        this.status = status;
    }

    public EzyApp getAppById(int appId) {
        if (zone != null) {
            EzyAppManager appManager = zone.getAppManager();
            EzyApp app = appManager.getAppById(appId);
            return app;
        }
        return null;
    }

    public EzyPingManager getPingManager() {
        return pingManager;
    }

    public EzyPingSchedule getPingSchedule() {
        return pingSchedule;
    }

    public EzyHandlerManager getHandlerManager() {
        return handlerManager;
    }

    private void printSentData(EzyCommand cmd, EzyArray data) {
        if (!unloggableCommands.contains(cmd))
            EzyLogger.debug("send command: " + cmd + " and data: " + data);
    }
}
