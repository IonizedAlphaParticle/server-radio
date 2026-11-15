package dev.sripaadh.serverradio;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

                                                if (!Files.exists(SONGS_DIR.resolve(song))){
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

                                        try (DirectoryStream<Path> files = Files.newDirectoryStream(SONGS_DIR, "*.wav")) {
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
            );
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
