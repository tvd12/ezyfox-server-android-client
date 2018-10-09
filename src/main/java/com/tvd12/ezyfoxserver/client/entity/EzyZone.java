package com.tvd12.ezyfoxserver.client.entity;

import com.tvd12.ezyfoxserver.client.EzyClient;
import com.tvd12.ezyfoxserver.client.handler.EzyAppDataHandlers;
import com.tvd12.ezyfoxserver.client.manager.EzyAppGroup;

/**
 * Created by tavandung12 on 10/2/18.
 */

public interface EzyZone extends EzyAppGroup {

    int getId();

    String getName();

    EzyUser getMe();

    EzyClient getClient();

    EzyAppDataHandlers getAppDataHandlers(String appName);

}