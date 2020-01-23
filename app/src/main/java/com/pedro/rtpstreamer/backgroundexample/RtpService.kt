package com.pedro.rtpstreamer.backgroundexample //패키지 이름

import android.app.Notification   //안드로이드에서 알림 메세지를 띄우기 위해 필요한 import
import android.app.NotificationChannel  //안드로이드에서 알림 메세지를 띄우기 위해 필요한 import
import android.app.NotificationManager  //안드로이드에서 알림 메세지를 띄우기 위해 필요한 import
import android.app.Service  //서비스 관련 기능 사용
import android.content.Context  //어떤 Activity 혹은 어떤 application 인가에 대해서 구별하는 정보가 context. 현재 사용되고 있는 어플리케이션(또는 액티비티)에 대한 포괄적인 정보를 지니고 있는 객체
import android.content.Intent   //인텐트는 애플리케이션 구성 요소 간에 작업 수행을 위한 정보를 전달하는 역할을 한다.
import android.os.Build   //디바이스 기기의 고유 정보를 가져오기 위해서 씀
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.pedro.rtplibrary.base.Camera2Base
import com.pedro.rtplibrary.rtmp.RtmpCamera2
import com.pedro.rtplibrary.rtsp.RtspCamera2
import com.pedro.rtpstreamer.R


/**
 * Basic RTMP/RTSP service streaming implementation with camera2
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)  //필요한 최소 API 레벨을 나타낸다. 여기서는 롤리팝이 최소 레벨.
/////////////////////////////////////////////////////////////////////////////////////////////
//IP 네트워크 상에서 오디오와 비디오를 전달하기 위한 실시간 전송 프로토콜 서비스를 수행하는 함수
class RtpService : Service(), ConnectCheckerRtp { //RtpService는 Service(), ConnectCheckerRtp를 상속받는 크래스 RtpService
  //private : 해당 클래스 or 인터페이스 내부에서만 사용 가능.
  // val : value의 약어, 변경 불가능(immutable)한 변수 (자바로 치면 final에 해당한다.)
  // var : variable의 약어, 변경이 가능(mutable)한 변수 (자바로 치면 일반 변수와 같다.)
  private val TAG = "RtpService"
  private val channelId = "rtpStreamChannel"
  private val notifyId = 123456
  //코틀린은 기본적으로 null을 사용하지 않도록 되어 있으나 null 을 허용하려면 자료형 뒤에 ?(물음표) 기호를 붙여서 선언하면 null 을 허용
  private var notificationManager: NotificationManager? = null
  //notificationManager이라는 variable에 NotificationManager을 상속하고(위에 import되어있음), 이 값을 null로 할당.
  private var endpoint: String? = null  //endpoint라는 variable에 String을 상속하고, 이 값을 null로 할당.
  private var camera2Base: Camera2Base? = null  //camera2Base라는 variable에 Camera2Base를 상속하고(위에 import되어있음), 이 값을 null로 할당.

  //알림채널(Notification Channel) 만드는 함수 onCreate()
  override fun onCreate() {
    super.onCreate()
    Log.e(TAG, "RTP service create")  //error 용 로그. 보통 exception 이 발생하거나 Error 가 발생할 경우 system이 이것을 활용.
    notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    //Context.NoTIFICATION_SERVICE라는 문자열 상수 값으로 식별되는 시스템 서비스를 획득하여,
    // NotificationManager를 이용해 현재 시스템에서 동작 중인 모든 서비스 목록을 얻을 수 있다.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //해당 기기의 버전 정보가 오레오 이상이면
      val channel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH)
      //Notification Channel을 만들어주고 필요한 설정을 해준 뒤,
      notificationManager?.createNotificationChannel(channel)
      //NotificationManager의 createNotificationChannel()을 호출해준다.
    }
    keepAliveTrick()  //바로 아래에 구현되어 있음.
  }

  private fun keepAliveTrick() {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {  //해당 기기의 버전 정보가 오레오 이상이라면
      val notification = NotificationCompat.Builder(this, channelId)  //빌더 constructor. context와 channel id를 받는다.
        .setOngoing(true) //이 notation(알림)이 진행 중인지 여부를 설정.
        .setContentTitle("")  //notification의 첫 행의 타이틀을 설정함.
        .setContentText("").build()
      startForeground(1, notification)

    } else {  //해당 기기의 버전 정보가 오레오 이상이 아니라면
      startForeground(1, Notification())
    }
  }

  override fun onBind(p0: Intent?): IBinder? {  // 서비스 바인딩 객체를 생성하는 콜백 메소
    //IBinder 를 반환하는데, 바로 이 객체가 '서비스'와  '클라이언터' 사이의 인터페이스 역할을 한다.
    //Intent와 IBinder은 null일 수 있다.
    return null
  }


  //Service가 처음에 실행되면 onCreate() -> onStartCommand()순으로 실행되지만
  // 그 후 실행되고 있는 Service에 startService()호출시 Service의 onCreate()메서드가 아닌 onStartCommand()메서드가 실행됨.
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Log.e(TAG, "RTP service started")//error 용 로그.
    endpoint = intent?.extras?.getString("endpoint")
    if (endpoint != null) { //endpoint가 null이 아니라면
      prepareStreamRtp()  //아래에 구현되어 있음
      startStreamRtp(endpoint!!)  //endpoint가 null이 아니라고 선언해줌. Rtp 스트리밍 시작
    }
    return START_STICKY
  }

  //액티비티가 종료되거나 앱 프로세스 자체가 종료되면 호출됨
  override fun onDestroy() {
    super.onDestroy()
    Log.e(TAG, "RTP service destroy")//error 용 로그.
    stopStreamRtp()
  }

  //Rtp 스트리밍 준비
  private fun prepareStreamRtp() {
    if (endpoint!!.startsWith("rtmp")) {  //endpoint는 null이 아님.
      camera2Base = RtmpCamera2(baseContext, true, this)
      //RtmpCamera2.java에 있는 RtmpCamera2사용. Rtmp의 연결 확인
    } else {
      camera2Base = RtspCamera2(baseContext, true, this)
    }
  }

  //Rtp 스트리밍 시작하는 함수
  private fun startStreamRtp(endpoint: String) {
    if (!camera2Base!!.isStreaming) {//camera2Base는 null이 아님.
      if (camera2Base!!.prepareVideo() && camera2Base!!.prepareAudio()) {
        //prepareVideo, prepareAudio는 성공하면 true,실패하면 false 반환
        camera2Base!!.startStream(endpoint) //성공 시 startStream
      }
    } else {
      showNotification("You are already streaming :(")  //다음 텍스트에 해당하는 알람을 띄움.
      //showNotification은 아래 구현되어 있음.
    }
  }

  //Rtp 스트리밍 중지
  private fun stopStreamRtp() {
    if (camera2Base != null) {
      if (camera2Base!!.isStreaming) {//camera2Base는 null이 아님. 이것이 스트리밍 중일 때
        camera2Base!!.stopStream()  //스트리밍을 중지신킨다.
      }
    }
  }

  //알림을 보여줌.
  private fun showNotification(text: String) {
    val notification = NotificationCompat.Builder(this, channelId)  //생성자
      .setSmallIcon(R.mipmap.ic_launcher) //아이콘 설정
      .setContentTitle("RTP Stream")  //타이틀 설정
      .setContentText(text).build()
    notificationManager?.notify(notifyId, notification) //?은 null이 될 수 있음을 의미.
  }

  //알림을 보여주는 것을 중지함.
  private fun stopNotification() {
    notificationManager?.cancel(notifyId)
  }

  //Rtp연결이 성공했다는 것을 알리고, 에러 발생할 경우 에러 처리하는 함수.
  override fun onConnectionSuccessRtp() {
    showNotification("Stream started")  //스트리밍이 시작했다는 알람을 띄움
    Log.e(TAG, "RTP service destroy")//error 용 로그.
  }

  override fun onNewBitrateRtp(bitrate: Long) {

  }

  //Rtp연결이 실패했다는 것을 알리고, 에러 발생할 경우 에러 처리하는 함수.
  override fun onConnectionFailedRtp(reason: String) {
    showNotification("Stream connection failed")  //스트리밍이 실패했다는 알람을 띄움
    Log.e(TAG, "RTP service destroy")//error 용 로그.
  }

  //Rtp연결이 끊어짐을 알림
  override fun onDisconnectRtp() {
    showNotification("Stream stopped")
  }

  //서버 인증 오류가 발생함을 알림
  override fun onAuthErrorRtp() {
    showNotification("Stream auth error")
  }

  //서버 인증이 성공했음을 알림
  override fun onAuthSuccessRtp() {
    showNotification("Stream auth success")
  }
}
