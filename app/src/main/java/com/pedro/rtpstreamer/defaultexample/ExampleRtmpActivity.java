package com.pedro.rtpstreamer.defaultexample;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtpstreamer.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.ossrs.rtmp.ConnectCheckerRtmp;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class ExampleRtmpActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback {

  //RtmpCamera1 : rtplibrary/java/com.pedro.rtplibrary/rtmp에 정의되어 있음
  private RtmpCamera1 rtmpCamera1;

  private Button button;
  private Button bRecord;
  private EditText etUrl;

  private String currentDateAndTime = "";

  //저장소 경로+/rtmp-rtsp-stream-client-java
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
          + "/rtmp-rtsp-stream-client-java");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ////////////////////////////////////////////////////////////////////////////////
    //화면 플래그 셋팅
    //FLAG_KEEP_SCREEN_ON : 화면 꺼짐 방지 플래그
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    ////////////////////////////////////////////////////////////////////////////

    setContentView(R.layout.activity_example);

    SurfaceView surfaceView = findViewById(R.id.surfaceView);
    button = findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    Button switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);
    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp);
    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/84ea638dcc779f20647bb6a11cfa1fdf35f4152a");

    ////////////////////////////////////////////////////////////////////////////
    //ConnectCheckerRtmp: rtmp/java/net.ossrs.rtmp에 정의되어 있음
    rtmpCamera1 = new RtmpCamera1(surfaceView, this);

    //rtmp server url connection 재시도 횟수 설정
    rtmpCamera1.setReTries(10);
    ////////////////////////////////////////////////////////////////////////////

    //SurfaceHolder :
    // Abstract interface to someone holding a display surface.
    // Allows you to control the surface size and format,
    // edit the pixels in the surface, and monitor changes to the surface.
    //surfaceView의 callback setting
    surfaceView.getHolder().addCallback(this);
    ////////////////////////////////////////////////////////////////////////////
  }

  ////////////////////////////////////////////////////////////////////////////////
  //rtmpCamer1의 콜백 함수들

  //server connection 성공 시
  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(ExampleRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //server connection 실패 시
  //설정되어 있는 연결 재시도 횟수만큼 시도하고 종료
  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (rtmpCamera1.shouldRetry(reason)) {
          Toast.makeText(ExampleRtmpActivity.this, "Retry", Toast.LENGTH_SHORT)
                  .show();
          rtmpCamera1.reTry(5000);  //Wait 5s and retry connect stream
        } else {
          Toast.makeText(ExampleRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                  .show();
          rtmpCamera1.stopStream();
          button.setText(R.string.start_button);
        }
      }
    });
  }

  //ㅇㅅㅇ 뭐 하는 앨까?
  //rtmp/java/net.ossrs.rtmp/BitrateManager.calculateBitrate()에서 쓰이긴 함
  @Override
  public void onNewBitrateRtmp(long bitrate) {

  }

  //server connection 종료 시
  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(ExampleRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //인증 실패 시
  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(ExampleRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //인증 성공 시
  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(ExampleRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //버튼들 리스너
  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      //button
      case R.id.b_start_stop:
        //streaming 중인지 체크
        //streaming 중이 아니면 streaming 시작
        //streaming 중이면 streaming 종료
        if (!rtmpCamera1.isStreaming()) {
          //만약 recording 중이면 이미 오디오, 비디오가 준비 되어 있으므로 or 인 것
          if (rtmpCamera1.isRecording()
                  || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
            button.setText(R.string.stop_button);
            rtmpCamera1.startStream(etUrl.getText().toString());
          } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it",
                    Toast.LENGTH_SHORT).show();
          }
        } else {
          button.setText(R.string.start_button);
          rtmpCamera1.stopStream();
        }
        break;

      case R.id.switch_camera:
        try {
          rtmpCamera1.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;

      //bRecord
      case R.id.b_record:
        //sdk 버전 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          //녹화 중인지 체크
          //녹화 중이 아니면 녹화 시작 / 녹화 중이면 녹화 중지
          if (!rtmpCamera1.isRecording()) {
            try {
              //저장 공간 확인 / 해당 폴더 없으면 생성
              if (!folder.exists()) {
                folder.mkdir();
              }
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
              currentDateAndTime = sdf.format(new Date());

              //streaming 중인지 체크
              //streaming 중이 아니면 오디오 및 비디오 준비 후 녹화 시작
              //streaming 중이면 오디오 및 비디오가 준비되어 있으므로 그냥 녹화 시작
              if (!rtmpCamera1.isStreaming()) {
                if (rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
                  rtmpCamera1.startRecord(
                          folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                  bRecord.setText(R.string.stop_record);
                  Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(this, "Error preparing stream, This device cant do it",
                          Toast.LENGTH_SHORT).show();
                }
              } else {
                rtmpCamera1.startRecord(
                        folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
                bRecord.setText(R.string.stop_record);
                Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
              }
            } catch (IOException e) {
              rtmpCamera1.stopRecord();
              bRecord.setText(R.string.start_record);
              Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          } else {    //녹화 중지 후 영상 저장
            rtmpCamera1.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this,
                    "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
            currentDateAndTime = "";
          }
        } else {
          Toast.makeText(this, "You need min JELLY_BEAN_MR2(API 18) for do it...",
                  Toast.LENGTH_SHORT).show();
        }
        break;

      default:
        break;
    }
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //surfaceView 콜백 함수들

  //surface가 생성된 직수 호출됨
  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
  }

  //surface가 조정된 직후 호출됨
  //surfaceCreated 이후 한 번 이상은 꼭 호출됨
  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    //말 그대로 현재 카메라에 찍히는 영상의 프리뷰
    rtmpCamera1.startPreview();
  }

  //surface가 제거된 직후 호출됨
  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    //sdk 버전 체크 & 녹화 중이면 녹화 중지
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
      rtmpCamera1.stopRecord();
      bRecord.setText(R.string.start_record);
      Toast.makeText(this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }

    //streaming 중이면 streaming 중지
    if (rtmpCamera1.isStreaming()) {
      rtmpCamera1.stopStream();
      button.setText(getResources().getString(R.string.start_button));
    }
    rtmpCamera1.stopPreview();
  }
  ////////////////////////////////////////////////////////////////////////////////
}