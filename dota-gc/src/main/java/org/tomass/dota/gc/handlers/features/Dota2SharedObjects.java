package org.tomass.dota.gc.handlers.features;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.tomass.dota.gc.handlers.Dota2ClientGCMsgHandler;
import org.tomass.dota.gc.handlers.callbacks.PopupCallback;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectNewLobby;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectNewParty;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectRemovedLobby;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectRemovedParty;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectUpdatedLobby;
import org.tomass.dota.gc.handlers.callbacks.shared.SingleObjectUpdatedParty;
import org.tomass.dota.gc.util.CSOTypes;
import org.tomass.protobuf.dota.DotaGcmessagesClient.CMsgDOTAPopup;
import org.tomass.protobuf.dota.DotaGcmessagesMsgid.EDOTAGCMsg;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgClientWelcome;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOCacheSubscribed;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOCacheSubscribed.SubscribedType;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOCacheUnsubscribed;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOIDOwner;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOMultipleObjects;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOMultipleObjects.SingleObject;
import org.tomass.protobuf.dota.GcsdkGcmessages.CMsgSOSingleObject;
import org.tomass.protobuf.dota.Gcsystemmsgs.EGCBaseClientMsg;
import org.tomass.protobuf.dota.Gcsystemmsgs.ESOMsg;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientGCMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.util.compat.Consumer;

public class Dota2SharedObjects extends Dota2ClientGCMsgHandler {

    public enum ACTION {
        NEW, REMOVED, UPDATED
    };

    private Map<Integer, Consumer<IPacketGCMsg>> dispatchMap;

    private Map<CMsgSOIDOwner, List<SubscribedType>> cache;

    public Dota2SharedObjects() {
        dispatchMap = new HashMap<>();
        cache = new ConcurrentHashMap<>();
        dispatchMap.put(ESOMsg.k_ESOMsg_Create_VALUE, packetMsg -> handleCreate(packetMsg));
        dispatchMap.put(ESOMsg.k_ESOMsg_Update_VALUE, packetMsg -> handleUpdate(packetMsg));
        dispatchMap.put(ESOMsg.k_ESOMsg_Destroy_VALUE, packetMsg -> handleDestroy(packetMsg));
        dispatchMap.put(ESOMsg.k_ESOMsg_UpdateMultiple_VALUE, packetMsg -> handleUpdateMultiple(packetMsg));
        dispatchMap.put(ESOMsg.k_ESOMsg_CacheSubscribed_VALUE, packetMsg -> handleSubscribed(packetMsg));
        dispatchMap.put(ESOMsg.k_ESOMsg_CacheUnsubscribed_VALUE, packetMsg -> handleUnsubscibed(packetMsg));
        dispatchMap.put(EGCBaseClientMsg.k_EMsgGCClientWelcome_VALUE, packetMsg -> handleClientWelcome(packetMsg));
        dispatchMap.put(EDOTAGCMsg.k_EMsgGCPopup_VALUE, packetMsg -> handlePopup(packetMsg));
    }

    private void handleCreate(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOSingleObject.Builder> single = new ClientGCMsgProtobuf<>(CMsgSOSingleObject.class,
                packetMsg);
        handleSingleObject(ACTION.NEW, single.getBody().getTypeId(), single.getBody().getObjectData());
    }

    private void handleUpdate(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOSingleObject.Builder> single = new ClientGCMsgProtobuf<>(CMsgSOSingleObject.class,
                packetMsg);
        handleSingleObject(ACTION.UPDATED, single.getBody().getTypeId(), single.getBody().getObjectData());
    }

    private void handleDestroy(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOSingleObject.Builder> single = new ClientGCMsgProtobuf<>(CMsgSOSingleObject.class,
                packetMsg);
        handleSingleObject(ACTION.REMOVED, single.getBody().getTypeId(), single.getBody().getObjectData());
    }

    private void handleUpdateMultiple(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOMultipleObjects.Builder> multiple = new ClientGCMsgProtobuf<>(
                CMsgSOMultipleObjects.class, packetMsg);

        for (SingleObject single : multiple.getBody().getObjectsModifiedList()) {
            handleSingleObject(ACTION.UPDATED, single.getTypeId(), single.getObjectData());
        }

        for (SingleObject single : multiple.getBody().getObjectsAddedList()) {
            handleSingleObject(ACTION.NEW, single.getTypeId(), single.getObjectData());
        }

        for (SingleObject single : multiple.getBody().getObjectsRemovedList()) {
            handleSingleObject(ACTION.REMOVED, single.getTypeId(), single.getObjectData());
        }
    }

    private void handleSubscribed(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOCacheSubscribed.Builder> subs = new ClientGCMsgProtobuf<>(CMsgSOCacheSubscribed.class,
                packetMsg);

        getLogger().trace("handleSubscribed: " + subs.getBody().getOwnerSoid());
        cache.put(subs.getBody().getOwnerSoid(), subs.getBody().getObjectsList());
        for (SubscribedType sub : subs.getBody().getObjectsList()) {
            for (ByteString data : sub.getObjectDataList()) {
                handleSingleObject(ACTION.NEW, sub.getTypeId(), data);
            }
        }
    }

    private void handleUnsubscibed(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgSOCacheUnsubscribed.Builder> unsubs = new ClientGCMsgProtobuf<>(
                CMsgSOCacheUnsubscribed.class, packetMsg);

        getLogger().trace("handleUnsubscibed: " + unsubs.getBody().getOwnerSoid());
        List<SubscribedType> subs = cache.get(unsubs.getBody().getOwnerSoid());
        if (subs != null) {
            for (SubscribedType sub : subs) {
                for (ByteString data : sub.getObjectDataList()) {
                    handleSingleObject(ACTION.REMOVED, sub.getTypeId(), data);
                }
            }
        }
    }

    private void handleClientWelcome(IPacketGCMsg packetMsg) {
        ClientGCMsgProtobuf<CMsgClientWelcome.Builder> welcome = new ClientGCMsgProtobuf<>(CMsgClientWelcome.class,
                packetMsg);

        for (CMsgSOCacheSubscribed subs : welcome.getBody().getOutofdateSubscribedCachesList()) {
            getLogger().trace("handleClientWelcome: " + subs.getOwnerSoid());
            cache.put(subs.getOwnerSoid(), subs.getObjectsList());
            boolean lobbyExist = false;
            for (SubscribedType sub : subs.getObjectsList()) {
                for (ByteString data : sub.getObjectDataList()) {
                    handleSingleObject(ACTION.NEW, sub.getTypeId(), data);
                    if (CSOTypes.LOBBY_VALUE == sub.getTypeId()) {
                        lobbyExist = true;
                        // waiting for lobby and then continue
                        client.registerAndWait(sub.getTypeId());
                    }
                }
            }
            if (!lobbyExist && getGameCoordinator().getClient().getLobbyHandler().getLobby() != null) {
                client.postCallback(new SingleObjectRemovedLobby(CSOTypes.LOBBY_VALUE, ByteString.EMPTY));
            }
        }
    }

    private void handleSingleObject(ACTION action, int typeId, ByteString data) {
        switch (action) {
        case NEW:
            client.postCallback(new SingleObjectNewLobby(typeId, data));
            client.postCallback(new SingleObjectNewParty(typeId, data));
            break;
        case REMOVED:
            client.postCallback(new SingleObjectRemovedLobby(typeId, data));
            client.postCallback(new SingleObjectRemovedParty(typeId, data));
            break;
        case UPDATED:
            client.postCallback(new SingleObjectUpdatedLobby(typeId, data));
            client.postCallback(new SingleObjectUpdatedParty(typeId, data));
            break;
        }
    }

    private void handlePopup(IPacketGCMsg data) {
        ClientGCMsgProtobuf<CMsgDOTAPopup.Builder> protobuf = new ClientGCMsgProtobuf<>(CMsgDOTAPopup.class, data);
        getLogger().trace(">>handlePopup: " + protobuf.getBody() + "/" + data.getTargetJobID());
        client.postCallback(new PopupCallback(data.getTargetJobID(), protobuf.getBody()));
    }

    @Override
    public void handleGCMsg(IPacketGCMsg packetGCMsg) {
        Consumer<IPacketGCMsg> dispatcher = dispatchMap.get(packetGCMsg.getMsgType());
        if (dispatcher != null) {
            getLogger().trace(">>handleGCMsg shared object msg: " + packetGCMsg.getMsgType());
            dispatcher.accept(packetGCMsg);
        }
    }

}
