package dev.sripaadh.serverradio;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ServerRadio implements ModInitializer {
    public static final String MOD_ID = "serverradio";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Playlist PLAYLIST = new Playlist();
    public static final Path SONGS_DIR = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

    @Override
    public void onInitialize() {
        try {
            Files.createDirectories(SONGS_DIR);
            LOGGER.info("Songs folder: {}", SONGS_DIR);
        } catch (IOException e) {
            LOGGER.error("Couldn't create songs folder", e);
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    Commands.literal("radio")
                            .then(Commands.literal("next")
                                    .executes(context -> {
                                        String song = PLAYLIST.next();

                                        if (song == null) {
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

                            .then(Commands.literal("add")
                                    .then(Commands.argument("song", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                String song = StringArgumentType.getString(context, "song");

                                                if (!Files.exists(SONGS_DIR.resolve(song))) {
                                                    context.getSource().sendFailure(Component.literal("Song not found: " + song));
                                                    return 0;
                                                }


                                                PLAYLIST.add(song);
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal("Added: " + song), false);
                                                return 1;

                                            })))

                            .then(Commands.literal("list")
                                    .executes(context -> {
                                        List<String> names = new ArrayList<>();

                                        try (DirectoryStream<Path> files = Files.newDirectoryStream(SONGS_DIR, "*.{wav,mp3}")) {
                                            for (Path file : files) {
                                                names.add(file.getFileName().toString());
                                            }
                                        } catch (IOException e) {
                                            context.getSource().sendFailure(Component.literal("Couldn't read songs in the folder"));
                                            return 0;
                                        }

                                        if (names.isEmpty()) {
                                            context.getSource().sendFailure(Component.literal("No songs found"));
                                            return 0;
                                        } else {
                                            context.getSource().sendSuccess(() -> Component.literal("Songs: " + String.join(", ", names)), false);
                                            return 1;
                                        }
                                    }))

                            .then(Commands.literal("play")
                                    .executes(context -> playNext(context.getSource().getServer()) ? 1 : 0))

                            .then(Commands.literal("stop")
                                    .executes(context -> {
                                        if (!RadioVoicechatPlugin.stop()) {
                                            context.getSource().sendFailure(Component.literal("Nothing is playing"));
                                            return 0;
                                        }

                                        context.getSource().sendSuccess(
                                                () -> Component.literal("Stopped"), false);
                                        return 1;
                                    }))

                            .then(Commands.literal("skip")
                                    .executes(context -> playNext(context.getSource().getServer()) ? 1 : 0))
            );
        });
    }

    public static boolean playNext(MinecraftServer server) {
        String song = PLAYLIST.next();
        if (song == null) {
            broadcast(server, "Queue is empty, use /radio add <song>");
            return false;
        }

        broadcast(server, "Loading " + song + "...");

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return AudioLoader.load(SONGS_DIR.resolve(song));
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .whenComplete((samples, error) -> server.execute(() -> {
                    if (error != null) {
                        LOGGER.error("Failed to load " + song, error);
                        broadcast(server, "Couldn't play " + song);
                        return;
                    }
                    if (!RadioVoicechatPlugin.play(samples, server)) {
                        broadcast(server, "Nobody has voice chat connected");
                        return;
                    }
                    broadcast(server, "Now playing: " + song);
                }));

        return true;
    }

    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
