# Athena Media Server

Cool little media server, WIP :^)

[TODO List](https://github.com/users/e3ndr/projects/1/views/1)

## Features

- Can transcode to a wide variety of formats.
- Fast and light-weight.
- Can stream media over FTP or HTTP, with transcoding enabled on both.
- Supports CUDA/NVENC/QuickSync/v4lm2m acceleration.
- Supports WiiMC.

### WiiMC

In your config, enable `services.special`.

Then, add these two lines to your `onlinemedia.xml` file:

```xml
    <folder name="Athena">
		<link name="Athena Media Server - Search" type="search" addr="http://<ip>:8128/wiimc/search?q="/>
		<link name="Athena Media Server" addr="http://<ip>:8128/wiimc/list"/>
    </folder>
```

[Here's](https://i.e3ndr.xyz/1mBTjdAJwr8M__1666969284/WiiMC%20Support.mp4) what it looks like in action.

### How does FTP Streaming work?

Athena will generate a faux file list when a client connects using the encoding parameters in the `username` used to login. (If you want all of the files in SD quality set your username to `q_sd`, if you want SD quality AND h264 then use `q_sd.vc_h264`). This has successfully been tested in the VLC mobile app and work is being done to better support more FTP-streaming players (such as WiiMC).

## Parameters

| HTTP          | FTP | What is it used for?                                                              | Possible values                                                |
| ------------- | --- | --------------------------------------------------------------------------------- | -------------------------------------------------------------- |
| format        | f   | Selects a container to be used.                                                   | `MP4`, `TS`, `MKV`                                             |
| quality       | q   | Sets the video quality.                                                           | `UHD`, `FHD`, `HD`, `SD`, `LD`                                 |
| videoCodec    | vc  | Sets the video codec to be used IF the video quality is anything except `SOURCE`. | `SOURCE`, `H264_BASELINE`,, `H264_HIGH` `HEVC`, `SPARK`, `AV1` |
| audioCodec    | ac  | Sets the audio codec to be used.                                                  | `SOURCE`, `AAC`, `MP3`, `OPUS`                                 |
| subtitleCodec | sc  | Sets the subtitle codec to be used.                                               | `SOURCE`, `WEBVTT`, `SRT`, `ASS`                                 |

### Support for older devices

Your mileage may vary, but do play around with the parameters and let me know what you can get working with Athena! If you're unsure of what your client supports then you can open up [WebMediaTest](http://nossl.wmt.e3ndr.xyz/) on your device and use the tester tables to detect what your device supports.

## Prerequisites

- Requires FFMPEG installed on your path, Google it ;)
- Requires Java 11 or greater. You can grab that [here](https://adoptium.net/).
