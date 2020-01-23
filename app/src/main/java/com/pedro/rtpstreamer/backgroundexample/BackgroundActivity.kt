package com.pedro.rtpstreamer.backgroundexample //패키지 이름

import android.app.ActivityManager  //시스템에 실행되고 있는 서비스 목록, 앱의 액티비티가 화면의 상단을 차지하고 있는지에 대한 정보 등을 획득할 목적으로 사용
import android.content.Context  //어떤 Activity 혹은 어떤 application 인가에 대해서 구별하는 정보가 context. 현재 사용되고 있는 어플리케이션(또는 액티비티)에 대한 포괄적인 정보를 지니고 있는 객체
import android.content.Intent //인텐트는 애플리케이션 구성 요소 간에 작업 수행을 위한 정보를 전달하는 역할을 한다.
import android.os.Bundle  //Activity간에 데이터를 주고 받을 때 Bundle 클래스를 사용하여 데이터를 전송
import androidx.appcompat.app.AppCompatActivity //안드로이드 하위버전을 지원하는 액티비티. API level 11(안드로이드 3.0)미만의 단말기에서도 실행 가능
import com.pedro.rtpstreamer.R  //R파일은 여러 리소스들 즉 이미지, 아이콘, 레이아웃 등등에 접근할 주소값을 자동생성해서 가지고 있는 파일
import kotlinx.android.synthetic.main.activity_background.* //Kotlin Android Extension사용

class BackgroundActivity : AppCompatActivity() {  //코틀린은 상속을 extends가 아닌 :으로 표시

  override fun onCreate(savedInstanceState: Bundle?) {  //onCreate.  ? 가 있으면 이 변수는 무조건 null이 될 수 있음 android 에서 @Nullable 과 같은 annotation
    super.onCreate(savedInstanceState)  //상위 클래스의 onCreate 메소드를 먼저 호출하여 먼저 실행 되게 하고 오버라이드된 메소드를 처리 한다
    setContentView(R.layout.activity_background)  //Layout XML의 Background로 이미지를 포함시키는 작업
    b_start_stop.setOnClickListener { //버튼의 클릭 리스너 (버튼 이벤트 발생)
      if (isMyServiceRunning(RtpService::class.java)) { //코틀린에서 :: 를 사용해서 프로퍼티나 메소드를 참조해서 값으로 저장
        stopService(Intent(applicationContext, RtpService::class.java)) //서비스를 종료.applicationContext는 어플리케이션에 전역적으로 하나만 존재하는 객체
        b_start_stop.setText(R.string.start_button) //버튼 text view의 텍스트 설정
      } else {
        val intent = Intent(applicationContext, RtpService::class.java) //인텐트에 데이터를 담는다.
        intent.putExtra("endpoint", et_rtp_url.text.toString()) //액티비티 이동과 동시에 이전 액티비티에서 이동하는 액티비티로 "endpoint"라는 키값과 et_rtp_url.text.toString()라는 값을 넘김
        startService(intent)  //서비스를 시작(수동시작, 컴포넌트와 통신하진 않음)
        b_start_stop.setText(R.string.stop_button)  //버튼 text view의 텍스트 설정
      }
    }
  }

  override fun onResume() { //fun은 함수를 뜻함. 코틀린 함수의 기본 형태. 오버라이드 함수. 실행 재개에 대한 함수이다. 부모를 오버라이드 받아 사용
    super.onResume()  //실행 재개
    if (isMyServiceRunning(RtpService::class.java)) { //서비스가 실행중이면
      b_start_stop.setText(R.string.stop_button)  //버튼 text view의 텍스트 설정
    } else {  //서비스가 실행중이 아니면
      b_start_stop.setText(R.string.start_button) //버튼 text view의 텍스트 설정
    }
  }

  @Suppress("DEPRECATION")  //이 문구가 사용된 클래스나 매서드는 서드파티 서비스에서 사용되지 않는다.
  private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
    //서비스가 실행중인지를 판단하는 함수. 불리언으로 참/거짓 출력. onCreate, onResume에 쓰임
    //액티비티에서 서비스의 상태를 컸다 껐다할 때, 백그라운드 서비스가 돌고 있는지 확인한다.
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    //시스템 서비스는 startService ( ) 함수로 실행되는 게 아니라, getSystemService ( ) 함수를 이용합니다.
    // getSystemService ( ) 함수의 매개변수에 문자열 상수를 대입하고 이 값으로 식별되는 시스템 서비스를 획득하여 이용하는 구조입니다.
    // 이렇게 획득한 ActivityManager의 getRunningServices ( ) 함수를 이용하여 현재 시스템에서 동작 중인 모든 서비스 목록을 얻을 수 있습니다.
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.name == service.service.className) { //백그라운드 서비스가 돌고있으면 true반환
        return true
      }
    }
    return false  //백그라운드 서비스가 돌고 있지 않으면 false 반환
  }
}
