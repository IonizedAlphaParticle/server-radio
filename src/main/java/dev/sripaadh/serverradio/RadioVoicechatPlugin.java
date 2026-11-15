package dev.sripaadh.serverradio;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.StaticAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.opus.OpusEncoderMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

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

    public static void play(short[] samples, CommandSourceStack source){
        ServerLevel level = source.getLevel();

        VoicechatConnection first = API.getConnectionOf(source.getPlayer().getUUID());
        StaticAudioChannel channel = API.createStaticAudioChannel(
                UUID.randomUUID(), API.fromServerLevel(level), first
        );

        for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()){
            VoicechatConnection connection = API.getConnectionOf(p.getUUID());
            if (connection != null){
                channel.addTarget(connection);
            }

        }

        AudioPlayer player = API.createAudioPlayer(channel, API.createEncoder(OpusEncoderMode.AUDIO), samples);
        player.startPlaying();
    }
}

