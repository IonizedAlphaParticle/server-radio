package dev.sripaadh.serverradio;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRadio implements ModInitializer {
    public static final String MOD_ID = "serverradio";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Playlist PLAYLIST = new Playlist();

    @Override
    public void onInitialize() {
        PLAYLIST.add("song1.wav");
        PLAYLIST.add("song2.wav");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("radio")
                            .then(Commands.literal("next")
                                    .executes(context -> {
                                        String song = PLAYLIST.next();

                                        if (song == null){
                                            context.getSource().sendFailure(Component.literal("No more songs in the playlist!"));
                                            return 0;
                                        }

                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Next: " + song), false);
                                        return 1;
                                    }))
                            .then(Commands.literal("size")
                                    .executes(context -> {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Songs Queued: " + PLAYLIST.size()), false);
                                        return 1;

                                    }))
            );
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
