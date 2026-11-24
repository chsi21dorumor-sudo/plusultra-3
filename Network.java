package com.plusultra.net;

import com.plusultra.PlusUltra;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PlusUltra.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Network {
    private Network() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC, UseAbilityPayload::handle);
        registrar.playToServer(EvolutionPayload.TYPE, EvolutionPayload.STREAM_CODEC, EvolutionPayload::handle);
        registrar.playToClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC, SyncPayload::handle);
    }
}
