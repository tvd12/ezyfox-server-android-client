package com.tvd12.ezyfoxserver.client.handler;

import android.util.Log;

import com.tvd12.ezyfoxserver.client.EzyConnectionStatusAware;
import com.tvd12.ezyfoxserver.client.config.EzyClientConfig;
import com.tvd12.ezyfoxserver.client.config.EzyReconnectConfig;
import com.tvd12.ezyfoxserver.client.constant.EzyConnectionStatus;
import com.tvd12.ezyfoxserver.client.event.EzyDisconnectionEvent;

/**
 * Created by tavandung12 on 10/3/18.
 */

public class EzyDisconnectionHandler extends EzyAbstractEventHandler<EzyDisconnectionEvent> {

    @Override
    public final void handle(EzyDisconnectionEvent event) {
        Log.i("ezyfox-client", "handle disconnection, reason = " + event.getReason());
        preHandle(event);
        EzyClientConfig config = client.getConfig();
        EzyReconnectConfig reconnectConfig = config.getReconnect();
        boolean shouldReconnect = shouldReconnect(event);
        boolean mustReconnect = reconnectConfig.isEnable() && shouldReconnect;
        boolean reconnecting = false;
        if(mustReconnect)
            reconnecting = client.reconnect();
        if(!reconnecting) {
            setSatus();
            control(event);
        }
    }

    protected void preHandle(EzyDisconnectionEvent event) {
    }

    protected boolean shouldReconnect(EzyDisconnectionEvent event) {
        return true;
    }

    protected void control(EzyDisconnectionEvent event) {
    }

    private void setSatus() {
        ((EzyConnectionStatusAware)client).setStatus(EzyConnectionStatus.DISCONNECTED);
    }
}
