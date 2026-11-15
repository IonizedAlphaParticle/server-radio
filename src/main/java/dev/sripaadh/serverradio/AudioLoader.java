package dev.sripaadh.serverradio;

import de.maxhenkel.voicechat.api.mp3.Mp3Decoder;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioLoader {

    public static short[] loadWav(Path file) throws Exception {
        try (AudioInputStream source = AudioSystem.getAudioInputStream(file.toFile())) {
            AudioFormat in = source.getFormat();
            int channels = in.getChannels();

            // 1. Convert to 16-bit signed PCM
            AudioFormat pcm16 = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    in.getSampleRate(), 16, channels, channels * 2, in.getSampleRate(), false);
            byte[] bytes;
            try (AudioInputStream converted = AudioSystem.getAudioInputStream(pcm16, source)) {
                bytes = converted.readAllBytes();
            }

            // 2. Stereo → mono: average left and right for each moment in time
            int frames = bytes.length / (2 * channels);
            short[] mono = new short[frames];
            for (int f = 0; f < frames; f++) {
                int sum = 0;
                for (int c = 0; c < channels; c++) {
                    int i = (f * channels + c) * 2;
                    sum += (short) ((bytes[i] & 0xFF) | (bytes[i + 1] << 8));
                }
                mono[f] = (short) (sum / channels);
            }

            // 3. Resample to 48 kHz using linear interpolation
            return resample(mono, in.getSampleRate(), 48000);
        }
    }

    private static short[] resample(short[] input, float fromRate, int toRate) {
        if (fromRate == toRate) return input;
        double ratio = fromRate / toRate;
        short[] out = new short[(int) (input.length / ratio)];
        for (int i = 0; i < out.length; i++) {
            double pos = i * ratio;
            int i0 = (int) pos;
            int i1 = Math.min(i0 + 1, input.length - 1);
            double t = pos - i0;
            out[i] = (short) Math.round(input[i0] * (1 - t) + input[i1] * t);
        }
        return out;
    }

    // Entry point: pick a decoder based on the file extension
    public static short[] load(Path file) throws Exception {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".mp3")) {
            return loadMp3(file);
        }
        return loadWav(file);
    }

    private static short[] loadMp3(Path file) throws Exception {
        try (InputStream in = Files.newInputStream(file)) {
            Mp3Decoder decoder = RadioVoicechatPlugin.API.createMp3Decoder(in);
            if (decoder == null) {
                throw new IOException("MP3 decoder not available");
            }
            short[] pcm = decoder.decode();
            AudioFormat format = decoder.getAudioFormat();
            short[] mono = toMono(pcm, format.getChannels());
            return resample(mono, format.getSampleRate(), 48000);
        }
    }

    // Average the channels together. Stereo samples come interleaved: L, R, L, R...
    private static short[] toMono(short[] interleaved, int channels) {
        if (channels == 1) return interleaved;
        short[] mono = new short[interleaved.length / channels];
        for (int f = 0; f < mono.length; f++) {
            int sum = 0;
            for (int c = 0; c < channels; c++) {
                sum += interleaved[f * channels + c];
            }
            mono[f] = (short) (sum / channels);
        }
        return mono;
    }
}