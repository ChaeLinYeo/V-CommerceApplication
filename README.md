## app>java>com.pedro.rtpstreame>res>layout  
   * activity_main.xml
  
    앱 실행 시 가장 먼저 보이는 화면. 방송 촬영(Start Broadcast), 방송 시청(Watch Broadcast), 재방송 보기(Replay) 세 개의 버튼중 클릭하여 해당 기능의 화면으로 넘어간다.
  
   * broadcast_main.xml
  
    송출부 화면(방송자용 화면)
  
   * buylist_popup,xml
  
    fragment_player.xml(고객용 화면)의 화면에서 왼쪽 하단 쇼핑카트 이미지 버튼 클릭 시 나타나는 팝업
  
   * custom_dialog.xml
  
    broadcast_main.xml(방송자용 화면)의 화면에서 제목을 터치하여 수정 시 나타나는 팝업
  
   * declare_popup01.xml 
  
    fragment_player.xml(고객용 화면)의 화면에서 신고버튼 클릭 시 나타나는 팝업. 신고 종류 선택
  
   * declare_popup02.xml
  
    declare_popup01.xml의 화면에서 "다음"을 클릭 시 나타나는 팝업.신고 사유를 상세하게 입력
  
   * fragment_player.xml
  
    수신부 화면(고객용 화면)
  
   * init_channel.xml
  
    broadcast_main.xml(방송자용 화면)의 화면에서 "start"버튼을 눌러 방송 시작 시 방송 제목을 설정하는 팝업창. 방송의 제목을 설정해야 방송 시작이 가능하다.
  
   * kakao_strings.xml
  
    카카오톡으로 공유하기 기능을 구현하기 위해 만들어둔 것. 현재는 파이어베이스 링크공유 방법으로 구현되어 쓰이지 않지만, 혹시라도 나중에 sns별로 공유방식을 각기 다르게 구현할 수 있을까봐 남겨둠.
  
   * notification_custom_dialog.xml
  
    broadcast_main.xml(방송자용 화면)의 화면에서 공지사항 부분(각종 알림과 채팅창 아래)을 터치하면 나타나는 팝업
  
   * player_main.xml
  
    사용자가 앱을 켜서 방송을 시청할 경우 방송 채널을 선택하는 임시 화면
  
   * popup.xml
  
    방송자가 만든 쿠폰 이벤트가 띄워지는 팝업창. 고객의 방송 화면에 뜨는 팝업창
  
   * popup_category.xml
  
    broadcast_main.xml(방송자용 화면)의 화면에서 "카테고리"버튼을 눌렀을 때 나타나는 팝업. 
  
   * popup_coupon.xml
  
    방송자가 만든 쿠폰 이벤트가 띄워지는 팝업창. 방송자의 방송 화면에 띄워지는 팝업창
  
   * popup_custom_dialog.xml
  
    방송자가 방송 중에 쿠폰 이벤트를 생성하는 팝업창. 시청자 목록(눈 이미지 버튼)>이벤트 생성 을 클릭하면 나타나는 팝업창. 시청자 목록에서 선택한 사용자들에게 보내질 쿠폰 이벤트를 설정할 수 있음.
  
   * popup_people.xml
  
    broadcast_main.xml(방송자용 화면)의 화면에서 눈 이미지 버튼을 클릭 시 띄워지는 팝업. 시청자 목록을 보여줌. 
  
   * popup_timeline.xml
  
    replayer.xml 재방송 화면에서 하단의 "타임라인"버튼을 클릭 시  나타나는 팝업. 생방송때 판매되었던 상품들의 목록이 뜨며, 상품 클릭 시 해당 상품이 판매되던 시간대로 넘어간다.
  
   * replayer.xml
  
    재방송을 시청하는 화면. 
  
   * broadcast_main.xml
  
    (방송자용 화면)의 화면에서 "text"버튼을 클릭 시 나타나는 팝업. 방송 중간에 띄울 텍스를 입력하고 텍스트의 색을 선택하여 "적용하기"를 클릭 시 생방송 중 화면에 입력한 텍스트가 띄워짐.
  
  




## app>java>com.pedro.rtpstreamer>broadcaster>BroadcastMain  
   * imgButton
  
    방송 중에 핸드폰의 로컬 갤러리에서 이미지를 선택하여 띄울 수 있음. 이미지는 하나만 선택 가능.
  
   * uriButton
  
    방송 중에 핸드폰의 로컬 갤러리에서 동영상을 선택하여 띄울 수 있음. 동영상은 하나만 선택 가능.
  
   * LikePlayer
  
    방송 화면에서 하트가 총 몇번 눌렸는지 보여준다. 하트 클릭 누적 횟수가 100회를 넘을 때마다 방송 화면의 각종 알림 부분의 배경을 holo_red_light색으로 바꾸며 하트 클릭 횟수를 알린다.

   * AlarmPlayer
  
    방송 화면의 각종 알림 부분에 표시되는 알림 종류에 따라 holo_blue_bright, holo_red
  
   * toggleSongLikeAnimation
  
    방송자가 보는 화면에서 하트 클릭 로티 애니메이션을 실행시킨다. ValueAnimator animator부분을 조절하여 로티 애니메이션의 시작시간, 종료시간, 애니메이션의 지속시간을 설정할 수 있다.
  




## app>java>com.pedro.rtpstreamer>player>Fragment_player  
   * FollowButton.setOnClickListener
  
    시청자가 방송중에 팔로우 버튼을 누르면 팔로우 상태에 따라 팔로우 취소/팔로우 로 버튼의 텍스트가 바뀌며, 팔로우를 할 경우 방송 화면의 각종 알림 부분에 팔로우를 했다는 정보가 뜬다.
  
   * title.setOnClickListener
  
    시청자가 방송 중 제목을 클릭하면 제목을 제외한 모든 화면 구성 요소가 사라지고 화면에 제목과 방송자의 영상만 남음.
  
   * mVideoSurfaceView.setOnClickListener
  
    시청자가 방송 중 방송 화면을 클릭하면 방송자의 영상을 제외한 모든 화면 구성 요소가 사라진다.
  
   * toggleSongLikeAnimButton
  
    시청자가 보는 화면에서 하트 클릭 로티 애니메이션을 실행시킨다. ValueAnimator animator부분을 조절하여 로티 애니메이션의 시작시간, 종료시간, 애니메이션의 지속시간을 설정할 수 있다.
  
   * playStart
  
    방송을 시작한다
  
   * buy_button
  
    방송 화면에서 왼쪽 하단 쇼핑카트 이미지 버튼. 클릭 시 PopupManager.java의 btn_buy함수를 실행시킨다.
  
   * menu_share
  
    방송 화면에서 공유 이미지 버튼 클릭 시 PopupManager.java의 btn함수를 실행시킨다.
  
   * declare
  
    방송 화면에서 신고 버튼 클릭 시 PopupManager.java의 select_Declare함수를 실행시킨다.
  
   * btn_sound
  
    방송 화면에서 음소거 이미지 버튼 클릭 시 SoundOnOff함수를 호출하여 방송 소리가 음소거된다.
  
   * LikePlayer
  
    방송 화면에서 하트가 총 몇번 눌렸는지 보여준다. 하트 클릭 누적 횟수가 100회를 넘을 때마다 방송 화면의 각종 알림 부분의 배경을 holo_red_light색으로 바꾸며 하트 클릭 횟수를 알린다.
  
   * AlarmPlayer
  
    방송 화면의 각종 알림 부분에 표시되는 알림 종류에 따라 holo_blue_bright, holo_red_light, holo_green_light색으로 각종 알림의 배경색을 바꿔준다.
  
   * SoundOnOff
  
    핸드폰의 상태가 벨소리/진동/무음 모드일때를 구분하여 음소거/음소거 해제를 해준다.
  
   * setReadMore
  
    방송자가 공지사항을 길게 입력했을 경우, "더보기"라는 텍스트가 공지사항 뒤에 붙으며 "더보기"를 클릭시 전체 공지사항을 볼 수 있음
  




## app>java>com.pedro.rtpstreamer>replayer>Replayer  
   * title.setOnClickListener
  
    시청자가 방송 중 제목을 클릭하면 제목을 제외한 모든 화면 구성 요소가 사라지고 화면에 제목과 방송자의 영상만 남음.
  
   * surfaceView.setOnClickListener
  
    시청자가 방송 중 방송 화면을 클릭하면 방송자의 영상을 제외한 모든 화면 구성 요소가 사라진다. 
  
   * MuteAudio
  
    재방송의 소리를 음소거 시킨다.
  
   * UnMuteAudio
  
    재방송의 소리를 음소거 해제한다.
  
   * SoundOnOff
  
    재방송 화면에서 음소거 이미지 버튼을 클릭 시 음소거/음소거 해제 함수를 실행시키고 음소거 상태에 따른 버튼의 이미지를 바꾼다.
  
   * heartAni
  
    재방송 화면에서 하트 클릭 로티 애니메이션을 실행시킨다. ValueAnimator animator부분을 조절하여 로티 애니메이션의 시작시간, 종료시간, 애니메이션의 지속시간을 설정할 수 있다.
  
   * popTimeLine
  
    popup_timeline.xml을 띄운다. 생방송 때 방송되었던 상품들의 리스트를 보여준다. 상품 클릭 시 해당 상품이 방송되던 시점으로 넘어가며, 재방송 화면 하단에 어떤 상품이 판매중인지 뜬다. 
  
   * btn_follow
  
    재방송 시청 중 팔로우 버튼을 누르면 버튼 텍스트가 팔로우 상태에 따라 팔로우/팔로우 취소 로 바뀐다.
  




## app>java>com.pedro.rtpstreame>utils>PopupManager  
   * create_Category
  
    popup_category.xml을 띄운다. 해당 방송에서 방송할 상품명들을 목록으로 작성할 수 있다. 방송중에 해당 상품을 선택하고 "방송품목설정"을 클릭 시 타임라인이 기록되고 방송 화면에서 현재 판매중인 품목이 바뀌었음을 "각종 알림"영역에 표시한다. 타임라인 기록은 추후 재방송 시청 시 해당 카테고리의 품목을 판매하는 장면을 즉각 선택하여 볼 수 있는 용도로 쓰인다.
  
* create_title : init_channel.xml을 띄운다. 방송 시작 시 첫 제목을 설정한다. 
* btn_editPopUp : popup_custom_dialog.xml을 띄운다. 쿠폰 이벤트의 타이틀(제목), 쿠폰 이벤트의 상세 내용, 쿠폰 이벤트 팝업을 시청자들에게 보여줄 시간(시,분,초)를 설정할 수 있다. 해당 화면에서 "닫기"를 누르면 작성한 쿠폰 이벤트의 정보가 임시 저장된다. "적용"을 누르면 바로 쿠폰 이벤트가 실행되며 설정한 시간만큼만 쿠폰 이벤트 팝업창을 띄운다.
* btn_showPopUp : popup_coupon.xml을 띄운다. 쿠폰 이벤트를 발생시켰을 때 방송자에게 보이는 쿠폰 이벤트 팝업을 띄워준다.
* btn_showPeople : popup_people.xml을 띄운다. 시청자 목록을 보여주며, 쿠폰 이벤트 설정, 생성, 특정 시청자 벤, 벤 해제, 시청자 닉네임 검색을 할 수 있다. 스크롤하면 시청자 목록이 새로고침된다.
* searching : btn_showPeople함수에서 쓰이는 함수로, 시청자 목록에서 시청자를 검색하는 기능이다. 시청자 닉네임의 일부만 검색해도 시청자 검색이 가능하다.
* btn_buy : buylist_popup.xml을 띄운다. 해당 방송에서 방송이 끝난 제품, 방송중인 제품, 방송 할 제품을 구분해서(방송자가 판매 품목 카테고리를 설정하고 방송중에 방송품목선정을 하면, 자동으로 구분해준다) 목록으로 보여준다. 
* select_Declare : declare_popup01.xml을 띄운다. 6까지 신고 분류 항목 카테고리 중 복수 선택 가능하고 is_declare변수로 선택된 항목을 인식한다.
* write_Declare : declare_popup02.xml을 띄운다. select_Declare에서 선택된 신고 항목에 대한 상세 신고 사유를 입력하여 제출한다. 
* btn : 파이어베이스 공유기능 함수이다. 주석처리된 //카카오 공유기능 은 sns별로 각각 공유기능을 만들 때를 고려해서 만들었으나, 파이어베이스로 사용자 핸드폰 내의 모든 sns앱에 공유가 가능해지면서 주석처리한 것이다. 각종 sns를 통해 공유링크를 보낼 수 있고, 링크 클릭 시 사용자의 핸드폰에 앱이 설치되어 있다면 앱이 실행되고 설치되어 있지 않다면 구글 플레이스토어로 이동한다. 아직 스토어에 올라오지 않았으므로 구글 플레이스토어 메인 페이지로 이동하도록 설정해두었다.
* declareSelectListner : select_Declare함수에서 선택된 신고 항목의 선택 여부를 불리언으로 구분해주고 선택된 항목은 declare_popup01.xml에서 다른 색으로 표시된다.
* CouponPlayer: popup.xml을 띄운다. 시청자가 보는 방송 화면에 방송자가 적용한 쿠폰 이벤트 팝업창을 방송자가 지정한 시간만큼 띄우고 없앤다.
* btn_showDialog2 : notification_custom_dialog.xml을 띄운다. 공지사항을 수정하면 방송 화면에 반영된다. 데이터베이스가 있으면 사용자의 쿠폰함에 일련 쿠폰 번호를 저장하는 기능을 구현해야 할 것이다.
* btn_Text : text_setup,xml을 띄운다. 방송 중 텍스트를 화면에 띄울 때 실행된다. 텍스트의 색을 바꿀 수 있다. 
* btn_Category : popup_category.xml을 띄운다. 방송자가 방송 시작 시, 또는 방송 중에 판매할 상품을 추가하거나 삭제할 수 있다. 상품을 선택해서 "방송품목선정"을 클릭하면 방송 화면에 선택한 상품이 판매중임이 각종 알림 부분에 뜬다. 
* btn_showDialog : custom_dialog.xml을 띄운다. 방송 중에 방송자가 제목을 수정할 수 있다. 




* BroadcastManager : 방송 송출 전반을 관리
* BrodacastManager.setBroadcastManager : 각종 초기화 작업
* BroadcastManager.managerBroadcast : 방송 컨트롤
* BroadcastManager.startBroadcast : 오디오, 카메라 셋팅 및 송출 시작
* BroadcastManager.stopeBroadcast : 송출 및 녹화 중단
* BroadcastManager.recordViedo : 방송 화면 녹화 시작 / 
* 기본 path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/rtmp-rtsp-stream-client-java/"
* 기본 명칭 = 현재시간.mp4
* BroadcastManager.uploadFile : 녹화된 영상을 클라우드(AWS)로 업로드
* BroadcastManager.setTexture : 이미지, 텍스트 등 방송 화면 설정
* BroadcastManager.removeUri : 띄웠던 동영상을 중단하고 제거

* PlayerMain : 방송 수신 전반 관리
* PlayerMain.setBroadcast : 각 방송 채널의 수신용 url을 bambuser 서버에서 얻어와 리스트로 저장 (생방송만 해당)
* PlayerMain.startBroadcastPlay : 눌린 버튼에 해당하는 fragment를 띄우고 방송 수신 시작
* PlayerMain.getBroadcast : 각 채널의 수신용 url을 얻는 함수
* PlayerMain.setFull : 방송 화면 fragment 리스트를 담은 fragment 생성

* FullVideoFragment : 방송 화면 fragment들을 관리
* FullVideoFragment.initViewPager : 방송화면 fragment를 담을 viewpager 초기화
* FullVideoFragment.startFull : 선택된 채널 화면 띄움
* FullVideoFragment.playStart : 방송 수신 및 재생 시작
* FullVideoFragment.closeFull : fragment 리스트 제거 및 수신 종료

* Fragment_player : 방송 수신 화면 관리
* Fragment_player.playStart : 해당 url을 통해 방송 화면 수신 및 재생

* Replayer : 재방송 관리 / proto type
* Replayer.setUri : 다시 보기할 영상이 저장된 url 셋팅
* handler : uri를 통해 영상 재생 / 현재 영상의 시간에 맞춰 채팅, 타임라인 등 재생

* AWSConnection AWS : 업로드 및 다운로드 / 현재 public으로 저장 중

* Data : 파일 관리
* Data.setAws : Aws 접속

* StaticVariable : 각종 권한, 계정 id, 채널 별 접속 url 및 id, aws 주소 저장

#### bambuser자체에서 각 방송의 영상 url을 받아오는 기능이 없으므로 채널을 임의로 10개 생성하여 mainchannel에서 방송 시작과 종료를 관리한다.
#### mainchannel은 단순히 메타 데이터에 key로 방송 방 번호, value로 방송 종료시 true, 시작시 sendbird url로 두어 방송이 가능한지를 판별한다.
#### sendbird에서 자신이 보낸 메세지는 자신이 받지 못한다.
#### sendbird api : https://docs.sendbird.com/ref/android/index.html


## .../rtmpstreamer/broadcaster/BroadcastMain

* public void LikePlayer(){}
  시청자들이 하트 버튼을 누를 때 마다 하트 애니메이션이 플레이 되고 100/200/300/... 순으로 채팅 위 영역에 형광 빨강으로 숫자 + 회 돌파라는 문구가 뜬다.
* public void AlarmPlayer(String data, int type){}
  이 함수를 호출 하면 받은 type별로 data를 textview에 settext한다.

#### sendbirdListner
  sendbird에 관련된 함수 관리
   * public void getCtrlComplete();
  
     방송 종료시 sendbird의 mainchannel에 끝났음을 알려줌.(메인 채널의 메타 데이터를 true로 업데이트)
   * public vid updateUser(int inout);
  
    시청자들이 나가거나 들어올 때마다 시청인원을 현재 시청인원 textview에 settext해준다.
    sendbird의 userlist를 받아와 직접 확인한다.
   * public getChannelComplete(boolean success);
  
    방송 시작 성공시에만 방송자의 내부 저장소에 방송 텍스트 로그(채팅, 제목변경 등 방송 내용과 방송자가 시간마다 방송한 상품에 대한 타임라인. 총 2개 생성)가 생성된다.
    실패시 방송이 불가하다는 토스트 메세지가 뜬다.
   * public messageReceived(String cutomType, String data, long messagetime);
  
    sendbird 메세지에 담긴 data영역을 customtype별로 보여준다.
    chat일 경우 채팅창에 띄우는데, 방송자 쪽에서 로그를 저장해야 하므로 savechat()을 통해 저장한다. 채팅의 시간이 언제인지 정확히 알기위해 받았을 때를 기준으로 로그에 같이 저장한다.
   * public metaCounterUpdated(int heart);
  
    각 채널의 메타 카운터에 저장된 heart수를 불러와서 보여준다.
   * public channelcreateComplete();
  
    방송 시작을 위한 각 설정을 한다. 
   * public getUserListComplete(String peopleNum);
  
    유저 리스트를 받아와서 직접 센 후 시청인원을 현재 시청인원 textview에 settext해준다.
#### AWSListner
  방송의 로그를 AWS에 올리는 함수 관리
   * public startUpload();
  
    로딩 중 창을 띄워 현재 업로드 중임을 시각적으로 표시
   * public uploadComplete();
  
    방송 영상, 방송 텍스트 로그를 올려야 하므로 총 3개가 올라간다. 3개가 모두 올라갔는지 확인해준다.

## .../rtpstreamer/server/LocalfileManager.java

방송자 내부 저장소에 저장될 로그 파일들을 다루는 곳이다. 
방송 내용은 시간 / 타입 / 내용이 기본적인 틀이다.
like만 예외로 내용이 없다.
savetimeline은 방송 타임라인만을 담는 로그 파일에 대한 것이다.
방송 종료시 removeFile(String localPath);로 파일을 지운다.

## .../rtpstreamer/server/Pair.java

방송 로그를 가지고 재방송시 쉽게 로그를 다루기위해 생성한 클래스이다.
Pair는 각 로그 한줄에 대한 time과 data를 가진다.
여기에서 Pair타입의 정보를 slice한다.

## .../rtpstreamer/server/SendbirdConnection.java

sendbird에 대한 모든 것을 다룬다.

   * setupSendbird(Context context, boolean isOperator1, SendbirdListner sendbirdListner1);
  
    sendbird를 사용하기 위해 맨처음 하는 함수.
    isOperator1로 방송자인지 확인한다. 방송자라면 그 방송의 채팅방에 operator로 넣어서 채팅 관리 권한을 얻는다.
   * setUserId();
  
    아직 로그인을 하지 못하므로 sendbird 자체 채팅 서버에 참여하기 위해 임의의 아이디를 생성한다.
   * getCtrl();
  
    mainchannel의 url을 받아온다.
   * getBroadcastChannel();
  
    mainchannel의 메타 데이터인 방번호화 방별 방송 가능 여부를 받아온다.
    방송 시작시 이 함수가 호출되므로 그 방송자에게 그 시점에서 방송가능한 가장 작은 방 번호를 배정한다.
   * getPlayChannel(int channelNum);
  
    방송 시청을 원할때 부르는 함수
    방 번호 버튼을 누를 시 그 번호에 맞는 sendbird url을 받아오고 enter하게 된다.
   * SendBird.addChannelHandler(StaticVariable.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler());
  
      sendbrid 서버와 정보를 주고 받기 위한 handler.
      유저가 나가고 들어오는 것부터 하트의 갯수 증가를 알기위한 메타카운터 업데이트 함수등 여러 핸들러가 사용되었다. 그 방에서만 유효하도록 설계.
   * increaseMetaCounters();
   
    시청자가 하트 버튼을 누를시 호출된다.
    그 방의 메타카운터에 저장된 하트 개수를 1씩 증가시킨다.
   * getLiveChannelUrlList();
  
    시청자가 방을 고를 때 들어갈 수 있는 여부를 따져준다.
   * createChannel(String title);
  
    방송자가 방을 만들고 있을 때 불린다.
    방송자에 맞는 sendbird 서버와의 handler가 따로 존재한다.
   * sendUserMessage(String text, String type);
  
    sendbird의 api를 담고 있어서 이 함수를 호출시 방에 들어와있는 타인에게 메세지를 보낼 수 있다.
   * getUserListFromServer();
  
    방송자가 현재 시청하는 사람들의 목록을 새로고침 하였을 때 서버로부터 유저리스트를 받아오기 위한 함수이다.
   * setUserList(List<User> userList);
  
    방송자의 아이디는 제외한 유저리스트를 볼 수 있다.
   * banUser(int position);
  
    방송에 해를 끼치는 사용자를 무기한 뮤트한다. 하트는 누를수 있지만 애니메이션은 남에게 보이지 않고, 채팅에도 참여할 수 없다.
   * unbanUser(int position);
  
    뮤트된 사용자를 뮤트를 풀어줄 수 있다.
   * loadInitialMessageList(int numMessages);
  
    sendbird api를 통해 과거 방송의 채팅을 볼 수 있다. 나중에 들어오더라도 채팅의 내용을 확인할수 있기 위해 만들었다.
   * ...Category(...); (add, remove, select)
  
    방송자가 timeline을 남기기 위해 카테고리를 설정, 선택할 때 쓰이는 함수
   * getAllMetaData();
  
    각 방송 채팅방의 메타 데이터에는 카테고리만 저장되기 때문에 모든 메타데이터를 받아서 카테고리 리스트를 생성할 때 쓰인다.
   * setCategory(Map<String, String> map);
  
    시청자가 카테고리를 확인할 때 이미 방송이 끝난 상품, 현재 방송중인 상품, 방송 예정인 상품으로 나누어서 볼 수 있는데, 이를 위해 카테고리를 미리 리스트에 담아주는 함수이다.
   * updateMetaData(OpenChannel openChannel, String key, String value);
  
    카테고리의 value를 select로 update해 각 카테고리별 방송 여부를 확인하기 쉽게 한다.
