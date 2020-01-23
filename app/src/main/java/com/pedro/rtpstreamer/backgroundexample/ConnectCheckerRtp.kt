package com.pedro.rtpstreamer.backgroundexample //패키지 이름

import com.pedro.rtsp.utils.ConnectCheckerRtsp  //rtsp>java>com.pedro.rtsp>utils안에 있는 ConnectCheckerRtsp라는 인터페이스 파일을 임포트함.
import net.ossrs.rtmp.ConnectCheckerRtmp  //rtmp>java>net.ossrs.rtmp안에 있는 ConnectCheckerRtmp라는 파일을 임포트함.

//인터페이스는 100% 추상 클래스. 인스턴스를 만들 수 없는 클래스. 실제적으로 생성이 되어서 변수가 담기고 사용이 되는 것을 instance라고 함.
/**
 * (Only working in kotlin)
 * Mix both connect interfaces to support RTMP and RTSP in service with same code.
 * == 동일한 코드로 서비스에서 RTMP 및 RTSP를 지원하도록 두 연결 인터페이스를 혼합하십시오.
 */
interface ConnectCheckerRtp: ConnectCheckerRtmp, ConnectCheckerRtsp { //ConnectCheckerRtmp, ConnectCheckerRtsp인터페이스를 상속

  /**
   * Commons 공통된 함수   */
  //코틀린에서 fun은 함수를 뜻한다.
  fun onConnectionSuccessRtp()

  fun onConnectionFailedRtp(reason: String)

  fun onNewBitrateRtp(bitrate: Long)

  fun onDisconnectRtp()

  fun onAuthErrorRtp()

  fun onAuthSuccessRtp()

  /**
   * RTMP   //ConnectCheckerRtmp안에 있는 함수들을 오버라이드
   */
  //오버라이드 함수들. 상위 클래스가 가지고 있는 메서드를 하위 메서드에서 함수를 추가하여 재정의함.
  override fun onConnectionSuccessRtmp() {
    onConnectionSuccessRtp()
  }

  override fun onConnectionFailedRtmp(reason: String) { //void onConnectionFailedRtmp(String reason);로 정의되어 있었음.
    onConnectionFailedRtp(reason)
  }

  override fun onNewBitrateRtmp(bitrate: Long) {  //void onNewBitrateRtmp(long bitrate);로 정의되어 있었음.
    onNewBitrateRtp(bitrate)
  }

  override fun onDisconnectRtmp() {
    onDisconnectRtp()
  }

  override fun onAuthErrorRtmp() {
    onAuthErrorRtp()
  }

  override fun onAuthSuccessRtmp() {
    onAuthSuccessRtp()
  }

  /**
   * RTSP     //ConnectCheckerRtsp안에 있는 함수 오버라이드
   */
  //오버라이드 함수들. 상위 클래스가 가지고 있는 메서드를 하위 메서드에서 함수를 추가하여 재정의함.
  override fun onConnectionSuccessRtsp() {
    onConnectionSuccessRtp()
  }

  override fun onConnectionFailedRtsp(reason: String) { //void onConnectionFailedRtsp(String reason);로 정의되어 있었음
    onConnectionFailedRtp(reason)
  }

  override fun onNewBitrateRtsp(bitrate: Long) {  //void onNewBitrateRtsp(long bitrate);로 정의되어 있었음
    onNewBitrateRtp(bitrate)
  }

  override fun onDisconnectRtsp() {
    onDisconnectRtp()
  }

  override fun onAuthErrorRtsp() {
    onAuthErrorRtp()
  }

  override fun onAuthSuccessRtsp() {
    onAuthSuccessRtp()
  }
}