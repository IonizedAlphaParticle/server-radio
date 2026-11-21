# Server Radio

## Description

Server Radio allows you and your friends in a server to listen to music together. 

## Features

The radio can queue songs that you add, auto-play after a song ends, and you can skip or stop a song at your will. Additionally, there is a volume slider in the voice chat menu to control music volume. Late joiners can begin to listen to the music playing in the server immediately after they join. The mod supports both MP3 and WAV audio files. 

The mod supports playlist looping and shuffling such that the songs do not stop playing completely at the end of the playlist and shuffles the order of the playlist (unless these features are turned off using the listed commands). Further, songs can be added without needing their file extensions. 

## Requirements

Minecraft 26.2, Fabric, Fabric API, Simple Voice Chat.

## Installation

1. Put `serverradio-x.x.x.jar` and Simple Voice Chat in the server's `mods` folder
2. Start the server once. It creates `config/serverradio/`
3. Drop `.mp3` or `.wav` files into `config/serverradio/`
4. Make sure UDP port **24454** is open. Simple Voice Chat streams audio over it

## Commands

| Command | What it does |
|---|---|
| `/radio list` | Shows every song in the songs folder |
| `/radio add <song>` | Adds a song to the queue (e.g. `timeless` or `timeless.mp3`, not case-sensitive)|
| `/radio size` | Shows how many songs are queued |
| `/radio play` | Starts playing the queue |
| `/radio skip` | Skips to the next song |
| `/radio stop` <br/>| Stops the music |
|`/radio loop` | Toggles playlist looping|
|`/radio shuffle`| Toggles playlist shuffle (turning it off restores original order)|






## How It Works
### Audio Pipeline

The voice chat mod requires 48 kHz, 16-bit, mono audio. Therefore, the AudioLoader decodes the file into 16-bit and stores it in a short[]. Stereo audio is converted into mono audio by adding up samples from every channel in a frame and dividing it by the number of channels. Finally, if the audio is not 48 kHz, it is resampled to 48 kHz currently using linear interpolation to fill in the gaps.

### Background Loading

To prevent the game from stalling when a song loaded, loading a song had to be done on a background thread. Originally, when loading the song on the server thread, the load made it so that the server could not do anything other than loading audio (i.e. mobs, commands, chat, etc.). For each 1-2 seconds taken to load a song, that is 20-40 ticks missed, causing the game to lag. 

This was fixed by loading the song on a background thread and using server.execute(...) to allow the song to play on the server thread.

## License

## License
MIT. See [LICENSE](LICENSE).