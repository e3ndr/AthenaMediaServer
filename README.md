# Athena Media Server

Cool little media server, WIP :^)

[TODO List](https://github.com/users/e3ndr/projects/1/views/1)

## Features

-   Can transcode to a wide variety of formats.
-   Doesn't stream unnecessary data to the client, saving bandwidth (e.g an unused 5.1 surround sound audio stream)
-   Fast and light-weight.
-   Can stream media over FTP or HTTP, with transcoding enabled on both.
-   Supports CUDA/NVENC acceleration.
-   Supports WiiMC.

### WiiMC

Just add these two lines to your `onlinemedia.xml` file:

```xml
<link name="Athena Media Server - Search" type="search" addr="http://localhost:8080/v1/athena/api/wiimc/search?q="/>
<link name="Athena Media Server" addr="http://localhost:8080/v1/athena/api/wiimc/list"/>
```

[Here's](https://i.e3ndr.xyz/1mBTjdAJwr8M__1666969284/WiiMC%20Support.mp4) what it looks like in action.

### How does FTP Streaming work?

Athena will generate a faux file list when a client connects using the encoding parameters in the `username` used to login. (If you want all of the files in SD quality set your username to `q_sd`, if you want SD quality AND h264 then use `q_sd.vc_h264`). This has successfully been tested in the VLC mobile app and work is being done to better support more FTP-streaming players (such as WiiMC).

## Parameters

| HTTP       | FTP | What is it used for?                                                              | Possible values                          |
| ---------- | --- | --------------------------------------------------------------------------------- | ---------------------------------------- |
| format     | f   | Selects a container to be used.                                                   | `MP4`, `SWF`,`FLV`, `OGG`, `WEBM`, `MKV` |
| quality    | q   | Sets the video quality.                                                           | `SOURCE`, `UHD`, `FHD`, `HD`, `SD`, `LD` |
| videoCodec | vc  | Sets the video codec to be used IF the video quality is anything except `SOURCE`. | `H264`, `HEVC`, `SPARK`, `THEORA`, `VP8` |
| audioCodec | ac  | Sets the audio codec to be used.                                                  | `SOURCE`, `AAC`, `MP3`, `OPUS`           |

### Generating Flash-compatible output

Some older clients may not support HTML5 video but they may still support Flash (e.g the Wii or PSP). Here's a table of parameters you can use to support different versions of flash:

| Version | Note                           | Parameters                                 |
| ------- | ------------------------------ | ------------------------------------------ |
| 6+      | Should be supported by the PSP | format=SWF&videoCodec=SPARK&audioCodec=MP3 |
| 7+      | Should be supported by the Wii | format=FLV&videoCodec=SPARK&audioCodec=MP3 |
| 9+      |                                | format=FLV&videoCodec=H264&audioCodec=AAC  |

Your mileage may vary, but in do play around with the parameters and let me know what you can get working with Athena! If you're unsure of what your client supports then you can open up [WebMediaTest](https://wmt.e3ndr.xyz/) on your device (or run it locally to bypass https) and start playing the samples.

## Prerequisites

-   Requires FFMPEG installed on your path, Google it ;)
-   Requires Java 11 or greater. You can grab that [here](https://adoptium.net/).
