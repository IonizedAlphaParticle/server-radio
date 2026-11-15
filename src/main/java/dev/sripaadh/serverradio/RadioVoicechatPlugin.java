package dev.sripaadh.serverradio;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

public class RadioVoicechatPlugin implements VoicechatPlugin {

    // Your "remote control" for Voice Chat. null until its server starts.
    public static VoicechatServerApi API;

    @Override
    public String getPluginId() {
        return ServerRadio.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        // Nothing needed here
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        // "When Voice Chat's server starts, call my onServerStarted method"
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        API = event.getVoicechat();
        ServerRadio.LOGGER.info("Connected to Simple Voice Chat!");
    }
}