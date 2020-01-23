package com.pedro.rtpstreamer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import com.pedro.rtpstreamer.backgroundexample.BackgroundActivity;
import com.pedro.rtpstreamer.customexample.RtmpActivity;
import com.pedro.rtpstreamer.customexample.RtspActivity;
import com.pedro.rtpstreamer.defaultexample.ExampleRtmpActivity;
import com.pedro.rtpstreamer.defaultexample.ExampleRtspActivity;
import com.pedro.rtpstreamer.displayexample.DisplayRtmpActivity;
import com.pedro.rtpstreamer.displayexample.DisplayRtspActivity;
import com.pedro.rtpstreamer.filestreamexample.RtmpFromFileActivity;
import com.pedro.rtpstreamer.filestreamexample.RtspFromFileActivity;
import com.pedro.rtpstreamer.openglexample.OpenGlRtmpActivity;
import com.pedro.rtpstreamer.openglexample.OpenGlRtspActivity;
import com.pedro.rtpstreamer.surfacemodeexample.SurfaceModeRtmpActivity;
import com.pedro.rtpstreamer.surfacemodeexample.SurfaceModeRtspActivity;
import com.pedro.rtpstreamer.texturemodeexample.TextureModeRtmpActivity;
import com.pedro.rtpstreamer.texturemodeexample.TextureModeRtspActivity;
import com.pedro.rtpstreamer.utils.ActivityLink;
import com.pedro.rtpstreamer.utils.ImageAdapter;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

  private GridView list;

  //ActivityLink : java>utils에 정의되어 있음
  private List<ActivityLink> activities;

  /////////////////////////////////////////////////////////////////////////////////
  //권한 리스트
  //[ref] https://developer.android.com/reference/android/Manifest.permission.html#ACCEPT_HANDOVER
  private final String[] PERMISSIONS = {
          Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  /////////////////////////////////////////////////////////////////////////////////

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ////////////////////////////////////////////////////////////////////////////
    //애니메이션 효과 / R.transition에 정의되어 있는 애니메이션 리소스를 불러왔다
    //[ref] overridePendingTransition(생성될 activity, 사라질 activity)
    //참고 사이트 : https://dwfox.tistory.com/26
    overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
    ////////////////////////////////////////////////////////////////////////////

    TextView tvVersion = findViewById(R.id.tv_version);
    tvVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

    list = findViewById(R.id.list);

    //createList : 아래에 구현됨
    createList();

    //setListAdapter : 아래에 구현됨
    setListAdapter(activities);

    //hasPermissions : 아래에 구현됨
    if (!hasPermissions(this, PERMISSIONS)) {
      ////////////////////////////////////////////////////////////////////////
      //[ref] public static void requestPermissions(Activity activity, String[] permissions, int requestCode)
      //ActivityCompat.onRequestPermissionsResult에 요청 결과를 콜백한다고 함
      //따라서 요청 결과를 리턴 받으려면 인터페이스인 onRequestPermissionResult를 구현해야 함
      ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
      ////////////////////////////////////////////////////////////////////////
    }

  }

  ////////////////////////////////////////////////////////////////////////////////
  //페이지 리스트 저장
  //ActivityLink - Intent, lable : stirng, minSdk : int
  private void createList() {
    activities = new ArrayList<>();
    activities.add(new ActivityLink(new Intent(this, RtmpActivity.class),
            getString(R.string.rtmp_streamer), JELLY_BEAN));
    activities.add(new ActivityLink(new Intent(this, RtspActivity.class),
            getString(R.string.rtsp_streamer), JELLY_BEAN));
    activities.add(new ActivityLink(new Intent(this, ExampleRtmpActivity.class),
            getString(R.string.default_rtmp), JELLY_BEAN));
    activities.add(new ActivityLink(new Intent(this, ExampleRtspActivity.class),
            getString(R.string.default_rtsp), JELLY_BEAN));
    activities.add(new ActivityLink(new Intent(this, RtmpFromFileActivity.class),
            getString(R.string.from_file_rtmp), JELLY_BEAN_MR2));
    activities.add(new ActivityLink(new Intent(this, RtspFromFileActivity.class),
            getString(R.string.from_file_rtsp), JELLY_BEAN_MR2));
    activities.add(new ActivityLink(new Intent(this, SurfaceModeRtmpActivity.class),
            getString(R.string.surface_mode_rtmp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, SurfaceModeRtspActivity.class),
            getString(R.string.surface_mode_rtsp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, TextureModeRtmpActivity.class),
            getString(R.string.texture_mode_rtmp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, TextureModeRtspActivity.class),
            getString(R.string.texture_mode_rtsp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, DisplayRtmpActivity.class),
            getString(R.string.display_rtmp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, DisplayRtspActivity.class),
            getString(R.string.display_rtsp), LOLLIPOP));
    activities.add(new ActivityLink(new Intent(this, OpenGlRtmpActivity.class),
            getString(R.string.opengl_rtmp), JELLY_BEAN_MR2));
    activities.add(new ActivityLink(new Intent(this, OpenGlRtspActivity.class),
            getString(R.string.opengl_rtsp), JELLY_BEAN_MR2));
    activities.add(new ActivityLink(new Intent(this, BackgroundActivity.class),
            getString(R.string.service_rtp), LOLLIPOP));
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //GridView list의 adapter 설정
  private void setListAdapter(List<ActivityLink> activities) {
    //GridView.setAdapter : set adapter providing the grid's data
    //ImageAdapter : java>utils에 정의되어 있음
    list.setAdapter(new ImageAdapter(activities));

    //GridView.setOnItemClickListener : GridView의 각 Grid를 클릭했을 때의 콜백 함수 셋팅
    //바로 아래에 대응할 onItemClick 함수 overriding 되어 있음
    list.setOnItemClickListener(this);
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //GridView의 click 콜백 함수
  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if (hasPermissions(this, PERMISSIONS)) {
      ActivityLink link = activities.get(i);
      int minSdk = link.getMinSdk();

      //해당 페이지의 최소 sdk 버전을 만족하는 지 검사
      if (Build.VERSION.SDK_INT >= minSdk) {
        //페이지 전환
        startActivity(link.getIntent());
        //애니메이션 실행
        overridePendingTransition(R.transition.slide_in, R.transition.slide_out);
      } else {
        //showMinSdkError : 아래에 구현됨
        showMinSdkError(minSdk);
      }
    } else {
      //showPermissionErrorAndRequest : 아래에 구현됨
      showPermissionsErrorAndRequest();
    }
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //최소 sdk 버전을 만족하지 못할 시 해당하는 에러 메세지를 띄우는 함수
  private void showMinSdkError(int minSdk) {
    String named;
    switch (minSdk) {
      case JELLY_BEAN_MR2:
        named = "JELLY_BEAN_MR2";
        break;
      case LOLLIPOP:
        named = "LOLLIPOP";
        break;
      default:
        named = "JELLY_BEAN";
        break;
    }
    Toast.makeText(this, "You need min Android " + named + " (API " + minSdk + " )",
            Toast.LENGTH_SHORT).show();
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //권한 에러 메세지를 띄우고 권한 재요청하는 함수
  private void showPermissionsErrorAndRequest() {
    Toast.makeText(this, "You need permissions before", Toast.LENGTH_SHORT).show();
    ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
  }
  ////////////////////////////////////////////////////////////////////////////////

  ////////////////////////////////////////////////////////////////////////////////
  //권한을 가지고 있는 지 검사하는 함수
  private boolean hasPermissions(Context context, String... permissions) {
    //권한 요청 필수는 안드로이드 버전 6부터 이므로 이보다 높을 시에만 검사
    //Build.VERSION_CODES.M이 안드로이드 버전 6의 버전 코드
    //참고 사이트 : https://mixup.tistory.com/59
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
      for (String permission : permissions) {
        //ActivityCompat.checkSelfPermission : 해당 permission을 가지고 있는 지 검사하는 함수
        //PackageManager.PERMISSION_GRANTED : int, constant value : 0
        if (ActivityCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
          return false;
        }
      }
    }
    return true;
  }
  ////////////////////////////////////////////////////////////////////////////////
}