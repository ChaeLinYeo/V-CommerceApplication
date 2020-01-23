package com.pedro.rtpstreamer.customexample;

import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.pedro.encoder.input.video.CameraHelper;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtpstreamer.R;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */
public class RtmpActivity extends AppCompatActivity implements Button.OnClickListener, ConnectCheckerRtmp, SurfaceHolder.Callback, View.OnTouchListener {

  private Integer[] orientations = new Integer[] { 0, 90, 180, 270 };
  private RtmpCamera1 rtmpCamera1;
  private Button bStartStop, bRecord;
  private EditText etUrl;
  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmp-rtsp-stream-client-java");
  //options menu
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  private ActionBarDrawerToggle actionBarDrawerToggle;
  private RadioGroup rgChannel;
  private RadioButton rbTcp;
  private Spinner spResolution;
  private CheckBox cbEchoCanceler, cbNoiseSuppressor, cbHardwareRotation;
  private EditText etVideoBitrate, etFps, etAudioBitrate, etSampleRate, etWowzaUser,
          etWowzaPassword;
  private String lastVideoBitrate;
  private TextView tvBitrate;

  @Override
  protected void onCreate(Bundle savedInstanceState) { // 여기는 null이 절 대 안됨
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_custom);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼, 디폴트로 true만 해도 백버튼이 생김
    getSupportActionBar().setHomeButtonEnabled(true); // 홈버튼

    SurfaceView surfaceView = findViewById(R.id.surfaceView); /* 잡 설명 : SurfaceView는 그리기를 시스템에 맡기는 것이 아니라 (view)*/
    surfaceView.getHolder().addCallback(this);                //  스레드를 이용해 강제로 화면에 그림으로써 원하는 시점에 바로 화면에 그릴 수 있다. */
    surfaceView.setOnTouchListener(this); // 터치시 반응
    rtmpCamera1 = new RtmpCamera1(surfaceView, this);  /*  public RtmpCamera1(TextureView textureView, ConnectCheckerRtmp connectChecker) {
                                                            super(textureView); srsFlvMuxer = new SrsFlvMuxer(connectChecker);}*/
    prepareOptionsMenuViews(); // 밑에 함수 따로 구현되어 있음
    tvBitrate = findViewById(R.id.tv_bitrate); //findviewbyid로 Activity에서 XML의 View를 매칭
    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp); // 텍스트 입력창에 힌트로 보여줄 것 세팅해줌
    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/84ea638dcc779f20647bb6a11cfa1fdf35f4152a");// 텍스트뷰에 텍스트 세팅
    bStartStop = findViewById(R.id.b_start_stop);
    bStartStop.setOnClickListener(this); // 방송 시작 종료 버튼(?)클릭 이벤트
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this); // record버튼 클릭 이벤트
    Button switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this); // 카메라 변경 버튼 클릭 이벤트
  }

  // 네비게이션 메뉴를 그린다.
  private void prepareOptionsMenuViews() {
    drawerLayout = findViewById(R.id.activity_custom);
    navigationView = findViewById(R.id.nv_rtp);
    // Inflate a menu to be displayed in the toolbar
    navigationView.inflateMenu(R.menu.options_rtmp);
    actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.rtsp_streamer, R.string.rtsp_streamer) {

      public void onDrawerOpened(View drawerView) {
        actionBarDrawerToggle.syncState(); // 상태를 동기화 시킴(아이콘 모양 변경 등에 쓰임)
        lastVideoBitrate = etVideoBitrate.getText().toString(); // textarea에 쓴것을 getText()로 받고 이걸 다시 String으로
      }

      public void onDrawerClosed(View view) {
        actionBarDrawerToggle.syncState(); //열려 있을 때랑 같음

        //코드 원작자 설명
        /*You can select manually your video bitrate on fly with this method: rtmpCamera1.setVideoBitrateOnFly(bitrate); Also you can set auto adaptive video bitrate.
         근데 버전이 19이상일때만 가능, 가장 최근 비디오 bitrate는 null이 아니어야하고 텍스트 영역에서 받아온 etVideoBitrate와 최근 비디오 bitrate는 달라야함. 현재 방송중이어야 함*/
        if (lastVideoBitrate != null && !lastVideoBitrate.equals(etVideoBitrate.getText().toString()) && rtmpCamera1.isStreaming()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int bitrate = Integer.parseInt(etVideoBitrate.getText().toString()) * 1024;
            rtmpCamera1.setVideoBitrateOnFly(bitrate);
            Toast.makeText(RtmpActivity.this, "New bitrate: " + bitrate, Toast.LENGTH_SHORT).
                    show();
          } else {
            Toast.makeText(RtmpActivity.this, "Bitrate on fly ignored, Required min API 19",
                    Toast.LENGTH_SHORT).show();
          }
        }
      }

    };

    drawerLayout.addDrawerListener(actionBarDrawerToggle);

    //checkboxs
    cbEchoCanceler =
            (CheckBox) navigationView.getMenu().findItem(R.id.cb_echo_canceler).getActionView();
    cbNoiseSuppressor =
            (CheckBox) navigationView.getMenu().findItem(R.id.cb_noise_suppressor).getActionView();
    cbHardwareRotation =
            (CheckBox) navigationView.getMenu().findItem(R.id.cb_hardware_rotation).getActionView();

    //radiobuttons
    rbTcp =
            (RadioButton) navigationView.getMenu().findItem(R.id.rb_tcp).getActionView();
    rgChannel = (RadioGroup) navigationView.getMenu().findItem(R.id.channel).getActionView();
    rbTcp.setChecked(true);

    //spinners
    spResolution = (Spinner) navigationView.getMenu().findItem(R.id.sp_resolution).getActionView();

    //ArrayAdapter 초기화 , ArrayAdapter에 대한 내용 추가 정보 : https://armful-log.tistory.com/26
    ArrayAdapter<Integer> orientationAdapter =
            new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
    orientationAdapter.addAll(orientations); //위에 선언했던 Integer형 배열 orientation 여기에 사용

    ArrayAdapter<String> resolutionAdapter =
            new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);

    List<String> list = new ArrayList<>();
    for (Camera.Size size : rtmpCamera1.getResolutionsBack()) {
      list.add(size.width + "X" + size.height);
    }
    resolutionAdapter.addAll(list);
    spResolution.setAdapter(resolutionAdapter);
    //edittexts
    etVideoBitrate =
            (EditText) navigationView.getMenu().findItem(R.id.et_video_bitrate).getActionView();
    etFps = (EditText) navigationView.getMenu().findItem(R.id.et_fps).getActionView();
    etAudioBitrate =
            (EditText) navigationView.getMenu().findItem(R.id.et_audio_bitrate).getActionView();
    etSampleRate = (EditText) navigationView.getMenu().findItem(R.id.et_samplerate).getActionView();
    etVideoBitrate.setText("2500");
    etFps.setText("30");
    etAudioBitrate.setText("128");
    etSampleRate.setText("44100");
    etWowzaUser = (EditText) navigationView.getMenu().findItem(R.id.et_wowza_user).getActionView();
    etWowzaPassword =
            (EditText) navigationView.getMenu().findItem(R.id.et_wowza_password).getActionView();
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) { // null 가능, 전에 받아놓은
    super.onPostCreate(savedInstanceState);
    actionBarDrawerToggle.syncState(); // 전의 데이터를 반영하겠다
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  // 메뉴에서 선택된 항목에 대해 반응해줌
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
          drawerLayout.openDrawer(GravityCompat.START);
        } else {
          drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
      case R.id.microphone:
        if (!rtmpCamera1.isAudioMuted()) {
          item.setIcon(getResources().getDrawable(R.drawable.icon_microphone_off));
          rtmpCamera1.disableAudio();
        } else {
          item.setIcon(getResources().getDrawable(R.drawable.icon_microphone));
          rtmpCamera1.enableAudio();
        }
        return true;
      default:
        return false;
    }
  }

  //각 버튼 클릭했을 때 반응, switch문으로 각 버튼 클릭해서 넘어온 id가지고 실행
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.b_start_stop:
        Log.d("TAG_R", "b_start_stop: ");
        if (!rtmpCamera1.isStreaming()) {
          bStartStop.setText(getResources().getString(R.string.stop_button));
          String user = etWowzaUser.getText().toString();
          String password = etWowzaPassword.getText().toString();
          if (!user.isEmpty() && !password.isEmpty()) {
            rtmpCamera1.setAuthorization(user, password);
          }
          if (rtmpCamera1.isRecording() || prepareEncoders()) {
            rtmpCamera1.startStream(etUrl.getText().toString());
          } else {
            //If you see this all time when you start stream,
            //it is because your encoder device dont support the configuration
            //in video encoder maybe color format.
            //If you have more encoder go to VideoEncoder or AudioEncoder class,
            //change encoder and try
            Toast.makeText(this, "Error preparing stream, This device cant do it",
                    Toast.LENGTH_SHORT).show();
            bStartStop.setText(getResources().getString(R.string.start_button));
          }
        } else {
          bStartStop.setText(getResources().getString(R.string.start_button));
          rtmpCamera1.stopStream();
        }
        break;
      case R.id.b_record:
        Log.d("TAG_R", "b_start_stop: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
          if (!rtmpCamera1.isRecording()) {
            try {
              if (!folder.exists()) {
                folder.mkdir();
              }
              SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
              currentDateAndTime = sdf.format(new Date());
              if (!rtmpCamera1.isStreaming()) {
                if (prepareEncoders()) {
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
          } else {
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
      case R.id.switch_camera:
        try {
          rtmpCamera1.switchCamera();
        } catch (final CameraOpenException e) {
          Toast.makeText(RtmpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show(); //어디에서 무엇을 얼마나 보여줄건지
        }
        break;
      default:
        break;
    }
  }
  // 위에서  Record 버튼 눌렸을 때 기능을 수행할지 말지 선택. 부울 형!!
  private boolean prepareEncoders() {
    Camera.Size resolution =
            rtmpCamera1.getResolutionsBack().get(spResolution.getSelectedItemPosition());
    int width = resolution.width;
    int height = resolution.height;
    return rtmpCamera1.prepareVideo(width, height, Integer.parseInt(etFps.getText().toString()),
            Integer.parseInt(etVideoBitrate.getText().toString()) * 1024,
            cbHardwareRotation.isChecked() // 기기 회전을 감지하는듯
            , CameraHelper.getCameraOrientation(this))
            && rtmpCamera1.prepareAudio(Integer.parseInt(etAudioBitrate.getText().toString()) * 1024,
            Integer.parseInt(etSampleRate.getText().toString()),
            rgChannel.getCheckedRadioButtonId() == R.id.rb_stereo, cbEchoCanceler.isChecked(),
            cbNoiseSuppressor.isChecked());
    // prepareVideo라는 함수에서 현재 비디오가 적합한지 확인해서 bool형으로 넘겨주고
    // 사용자가 입력한 경우에 대해 따로 확인해서 둘다 true일때만 record가능
  }

  // rtmp 접속 확인
  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }
  // 접속 실패 했을때 메세지
  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT).show();
        rtmpCamera1.stopStream();
        bStartStop.setText(getResources().getString(R.string.start_button)); // 버튼을 시작 버튼으로 교체
        // Build.VERSION_CODES.JELLY_BEAN_MR2 = Android 4.3 : Jelly Bean : 18, 버전이 18이상.
        // 카메라가 기록중이면 기록을 멈추고 현재 날짜와 시간을 이름으로 가지는 파일 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
          rtmpCamera1.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(RtmpActivity.this,
                  "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                  Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
      }
    });
  }

  @Override
  public void onNewBitrateRtmp(final long bitrate) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        tvBitrate.setText(bitrate + " bps");
      }
    });
  }
  // 접속 종료
  @Override
  public void onDisconnectRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
          rtmpCamera1.stopRecord();
          bRecord.setText(R.string.start_record);
          Toast.makeText(RtmpActivity.this,
                  "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
                  Toast.LENGTH_SHORT).show();
          currentDateAndTime = "";
        }
      }
    });
  }
  // 권한 에러
  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }
  // 권한 성공
  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(RtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    drawerLayout.openDrawer(GravityCompat.START);
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtmpCamera1.startPreview(); // startPreview가 어디서 나왔는지 모르겠는데 아무튼 화면을 바꿈
    // optionally:
    //rtmpCamera1.startPreview(CameraHelper.Facing.BACK);
    //or
    //rtmpCamera1.startPreview(CameraHelper.Facing.FRONT);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && rtmpCamera1.isRecording()) {
      rtmpCamera1.stopRecord();
      bRecord.setText(R.string.start_record);
      Toast.makeText(this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
    if (rtmpCamera1.isStreaming()) {
      rtmpCamera1.stopStream();
      bStartStop.setText(getResources().getString(R.string.start_button));
    }
    rtmpCamera1.stopPreview();
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    int action = motionEvent.getAction();
    if (motionEvent.getPointerCount() > 1) {
      if (action == MotionEvent.ACTION_MOVE) {
        rtmpCamera1.setZoom(motionEvent);
      }
    } else {
      if (action == MotionEvent.ACTION_UP) {
        // todo place to add autofocus functional.
      }
    }
    return true;
  }
}