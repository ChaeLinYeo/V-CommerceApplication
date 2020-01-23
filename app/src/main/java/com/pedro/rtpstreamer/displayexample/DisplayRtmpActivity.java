package com.pedro.rtpstreamer.displayexample;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.pedro.rtplibrary.rtmp.RtmpDisplay;
import com.pedro.rtpstreamer.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.ossrs.rtmp.ConnectCheckerRtmp;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.DisplayBase}
 * {@link com.pedro.rtplibrary.rtmp.RtmpDisplay}
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DisplayRtmpActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, View.OnClickListener {

  private static RtmpDisplay rtmpDisplay; //display option을 저장
  private Button button;
  private Button bRecord;
  private EditText etUrl;
  private final int REQUEST_CODE_STREAM = 179; //random num
  private final int REQUEST_CODE_RECORD = 180; //random num

  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
          + "/rtmp-rtsp-stream-client-java");
  private NotificationManager notificationManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_display);
    notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    button = findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp);
    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/84ea638dcc779f20647bb6a11cfa1fdf35f4152a");
    rtmpDisplay = getInstance();

    // 상태에 따라 버튼에 있는 문자 바꿔줌
    if (rtmpDisplay.isStreaming()) {
      button.setText(R.string.stop_button);
    } else {
      button.setText(R.string.start_button);
    }
    if (rtmpDisplay.isRecording()) {
      bRecord.setText(R.string.stop_record);
    } else {
      bRecord.setText(R.string.start_record);
    }
  }
  // RtmpDisplay 개체를 받아오는 함수
  private RtmpDisplay getInstance() {
    if (rtmpDisplay == null) {
      return new RtmpDisplay(this, false, this); // 없을시 새로 생성
    } else {
      return rtmpDisplay; // 있으면 있는거 넘겨줌
    }
  }

  /**
   * This notification is to solve MediaProjection problem that only render surface if something changed.
   * It could produce problem in some server like in Youtube that need send video and audio all time to work.
   */
  // 번역 : 이 알림은 무언가 변경되었을 때 표면만을 렌더링하는 MediaProjection 문제를 해결하기 위한 것입니다.
  //        Youtube와 같은 일부 서버에서 항상 비디오와 오디오를 보내야하는 문제가 발생할 수 있습니다.
  private void initNotification() {
    Notification.Builder notificationBuilder =
            new Notification.Builder(this).setSmallIcon(R.drawable.notification_anim)
                    .setContentTitle("Streaming")
                    .setContentText("Display mode stream")
                    .setTicker("Stream in progress");
    notificationBuilder.setAutoCancel(true); // notification 생성
    if (notificationManager != null) notificationManager.notify(12345, notificationBuilder.build());
  }

  // 위 함수로 생성된 알림이 있을 경우에만 알림을 멈춤
  private void stopNotification() {
    if (notificationManager != null) {
      notificationManager.cancel(12345);
    }
  }
  // 각 경우마다 메세지를 띄운다
  // 아래는 다른 예제에서와 기능이 같음
  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                .show();
        stopNotification();
        rtmpDisplay.stopStream();
        button.setText(R.string.start_button);
      }
    });
  }

  @Override
  public void onNewBitrateRtmp(long bitrate) {

  }

  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayRtmpActivity.this, "Disconnected11", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(DisplayRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_STREAM
            || requestCode == REQUEST_CODE_RECORD && resultCode == Activity.RESULT_OK) {
      if (rtmpDisplay.prepareAudio() && rtmpDisplay.prepareVideo()) {
        initNotification();
        rtmpDisplay.setIntentResult(resultCode, data);
        if (requestCode == REQUEST_CODE_STREAM) {
          rtmpDisplay.startStream(etUrl.getText().toString());
        } else {
          try {
            rtmpDisplay.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
          } catch (IOException e) {
            rtmpDisplay.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        }
      } else {
        Toast.makeText(this, "Error preparing stream, This device cant do it", Toast.LENGTH_SHORT)
                .show();
      }
    } else {
      Toast.makeText(this, "No permissions available", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.b_start_stop:
        if (!rtmpDisplay.isStreaming()) {
          if (rtmpDisplay.isRecording()) {
            button.setText(R.string.stop_button);
            rtmpDisplay.startStream(etUrl.getText().toString());
          } else {
            button.setText(R.string.stop_button);
            startActivityForResult(rtmpDisplay.sendIntent(), REQUEST_CODE_STREAM);
          }
        } else {
          button.setText(R.string.start_button);
          rtmpDisplay.stopStream();
        }
        if (!rtmpDisplay.isStreaming() && !rtmpDisplay.isRecording()) stopNotification();
        break;
      case R.id.b_record:
        if (!rtmpDisplay.isRecording()) {
          try {
            if (!folder.exists()) {
              folder.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
            if (!rtmpDisplay.isStreaming()) {
              bRecord.setText(R.string.stop_record);
              Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
              startActivityForResult(rtmpDisplay.sendIntent(), REQUEST_CODE_RECORD);
            } else {
              rtmpDisplay.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
              bRecord.setText(R.string.stop_record);
              Toast.makeText(this, "Recording... ", Toast.LENGTH_SHORT).show();
            }
          } catch (IOException e) {
            rtmpDisplay.stopRecord();
            bRecord.setText(R.string.start_record);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
          }
        } else {
          rtmpDisplay.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(this,
                  "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                  Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
        if (!rtmpDisplay.isStreaming() && !rtmpDisplay.isRecording()) stopNotification();
        break;
      default:
        break;
    }
  }
}