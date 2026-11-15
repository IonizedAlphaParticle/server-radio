package dev.sripaadh.serverradio;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RadioVoicechatPlugin implements VoicechatPlugin {

    // Your "remote control" for Voice Chat. null until its server starts.
    public static VoicechatServerApi API;

    public static AudioPlayer current;

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
        VolumeCategory category = API.volumeCategoryBuilder()
                .setId("serverradio")
                .setName("Server Radio")
                .setDescription("Music from the server playlist")
                .build();
        API.registerVolumeCategory(category);
        ServerRadio.LOGGER.info("Connected to Simple Voice Chat!");
    }

    public static boolean play(short[] samples, MinecraftServer server) {
        stop();

        // Collect everyone's voice connection
        List<VoicechatConnection> listeners = new ArrayList<>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            VoicechatConnection connection = API.getConnectionOf(p.getUUID());
            if (connection != null) {
                listeners.add(connection);
            }
        }
        if (listeners.isEmpty()) return false;   // nobody to play to

        StaticAudioChannel channel = API.createStaticAudioChannel(
                UUID.randomUUID(), API.fromServerLevel(server.overworld()), listeners.get(0));
        if (channel == null) return false;

        channel.setCategory("serverradio");

        for (VoicechatConnection c : listeners) {
            channel.addTarget(c);
        }

        AudioPlayer player = API.createAudioPlayer(channel, API.createEncoder(OpusEncoderMode.AUDIO), samples);

        // CALLBACK: Voice Chat runs this when the song stops
        player.setOnStopped(() -> {
            if (current == player) {          // only if it ended on its own (not /radio stop)
                current = null;
                server.execute(() -> ServerRadio.playNext(server));   // hop back to the server thread
            }
        });

        current = player;
        player.startPlaying();
        return true;
    }

    public static boolean stop() {
        AudioPlayer old = current;
        if (old == null || !old.isPlaying()) return false;
        current = null;        // clear FIRST, so the callback knows this was a manual stop
        old.stopPlaying();
        return true;
    }
}

