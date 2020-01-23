package com.pedro.encoder.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import androidx.annotation.NonNull;
import com.pedro.encoder.BaseEncoder;
import com.pedro.encoder.Frame;
import com.pedro.encoder.input.audio.GetMicrophoneData;
import com.pedro.encoder.utils.CodecUtil;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedro on 19/01/17.
 *
 * Encode PCM audio data to ACC and return in a callback
 * PCM 오디오 데이터를 ACC로 인코딩하고 콜백으로 반환
 */

public class AudioEncoder extends BaseEncoder implements GetMicrophoneData {

  private static final String TAG = "AudioEncoder";

  private GetAacData getAacData;
  private int bitRate = 64 * 1024;  //in kbps
  private int sampleRate = 32000; //in hz
  private boolean isStereo = true;

  public AudioEncoder(GetAacData getAacData) {
    this.getAacData = getAacData;
  }

  /**
   * Prepare encoder with custom parameters
   * 맞춤 매개 변수로 인코더 준비
   */
  public boolean prepareAudioEncoder(int bitRate, int sampleRate, boolean isStereo,
      int maxInputSize) {
    this.sampleRate = sampleRate;
    isBufferMode = true;
    try {
      List<MediaCodecInfo> encoders = new ArrayList<>();
      if (force == CodecUtil.Force.HARDWARE) {
        encoders = CodecUtil.getAllHardwareEncoders(CodecUtil.AAC_MIME);
      } else if (force == CodecUtil.Force.SOFTWARE) {
        encoders = CodecUtil.getAllSoftwareEncoders(CodecUtil.AAC_MIME);
      }

      if (force == CodecUtil.Force.FIRST_COMPATIBLE_FOUND) {
        MediaCodecInfo encoder = chooseEncoder(CodecUtil.AAC_MIME);
        if (encoder != null) {
          codec = MediaCodec.createByCodecName(encoder.getName());
        } else {
          Log.e(TAG, "Valid encoder not found");
          return false;
        }
      } else {
        if (encoders.isEmpty()) {
          Log.e(TAG, "Valid encoder not found");
          return false;
        } else {
          codec = MediaCodec.createByCodecName(encoders.get(0).getName());
        }
      }

      int channelCount = (isStereo) ? 2 : 1;
      MediaFormat audioFormat =
          MediaFormat.createAudioFormat(CodecUtil.AAC_MIME, sampleRate, channelCount);
      audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
      audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize);
      audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
          MediaCodecInfo.CodecProfileLevel.AACObjectLC);
      codec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
      running = false;
      Log.i(TAG, "prepared");
      return true;
    } catch (IOException | IllegalStateException e) {
      Log.e(TAG, "Create AudioEncoder failed.", e);
      return false;
    }
  }

  /**
   * Prepare encoder with default parameters
   * 기본 파라미터로 인코더 준비
   */
  public boolean prepareAudioEncoder() {
    return prepareAudioEncoder(bitRate, sampleRate, isStereo, 0);
  }

  @Override
  public void start(boolean resetTs) {
    presentTimeUs = System.nanoTime() / 1000;
    codec.start();
    running = true;
    Log.i(TAG, "started");
  }

  @Override
  protected void stopImp() {
    Log.i(TAG, "stopped");
  }

  @Override
  protected Frame getInputFrame() throws InterruptedException {
    return null;
  }

  @Override
  protected void checkBuffer(@NonNull ByteBuffer byteBuffer,
      @NonNull MediaCodec.BufferInfo bufferInfo) {

  }

  @Override
  protected void sendBuffer(@NonNull ByteBuffer byteBuffer,
      @NonNull MediaCodec.BufferInfo bufferInfo) {
    getAacData.getAacData(byteBuffer, bufferInfo);
  }

  /**
   * Set custom PCM data.
   * Use it after prepareAudioEncoder(int sampleRate, int channel).
   * Used too with microphone.
   *
   * 사용자 정의 PCM 데이터를 설정하십시오.
   * compareAudioEncoder (int sampleRate, int 채널) 후에 사용하십시오.
   * 마이크에도 사용됩니다.
   *
   */
  @Override
  public void inputPCMData(Frame frame) {
    if (running) {
      try {
        getDataFromEncoder(frame);
      } catch (IllegalStateException e) {
        Log.i(TAG, "Encoding error", e);
      }
    } else {
      Log.i(TAG, "frame discarded");
    }
  }

  @Override
  protected MediaCodecInfo chooseEncoder(String mime) {
    List<MediaCodecInfo> mediaCodecInfoList = CodecUtil.getAllEncoders(mime);
    for (MediaCodecInfo mediaCodecInfo : mediaCodecInfoList) {
      String name = mediaCodecInfo.getName().toLowerCase();
      if (!name.contains("omx.google")) return mediaCodecInfo;
    }
    if (mediaCodecInfoList.size() > 0) {
      return mediaCodecInfoList.get(0);
    } else {
      return null;
    }
  }

  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  @Override
  public void formatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
    getAacData.onAudioFormat(mediaFormat);
  }
}
