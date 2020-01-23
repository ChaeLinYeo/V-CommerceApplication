package com.pedro.rtpstreamer.openglexample;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.Rect;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.pedro.encoder.input.gl.SpriteGestureController;
import com.pedro.encoder.input.gl.render.filters.AnalogTVFilterRender;
import com.pedro.encoder.input.gl.render.filters.AndroidViewFilterRender;
import com.pedro.encoder.input.gl.render.filters.BasicDeformationFilterRender;
import com.pedro.encoder.input.gl.render.filters.BeautyFilterRender;
import com.pedro.encoder.input.gl.render.filters.BlackFilterRender;
import com.pedro.encoder.input.gl.render.filters.BlurFilterRender;
import com.pedro.encoder.input.gl.render.filters.BrightnessFilterRender;
import com.pedro.encoder.input.gl.render.filters.CartoonFilterRender;
import com.pedro.encoder.input.gl.render.filters.CircleFilterRender;
import com.pedro.encoder.input.gl.render.filters.ColorFilterRender;
import com.pedro.encoder.input.gl.render.filters.ContrastFilterRender;
import com.pedro.encoder.input.gl.render.filters.DuotoneFilterRender;
import com.pedro.encoder.input.gl.render.filters.EarlyBirdFilterRender;
import com.pedro.encoder.input.gl.render.filters.EdgeDetectionFilterRender;
import com.pedro.encoder.input.gl.render.filters.ExposureFilterRender;
import com.pedro.encoder.input.gl.render.filters.FireFilterRender;
import com.pedro.encoder.input.gl.render.filters.GammaFilterRender;
import com.pedro.encoder.input.gl.render.filters.GlitchFilterRender;
import com.pedro.encoder.input.gl.render.filters.GreyScaleFilterRender;
import com.pedro.encoder.input.gl.render.filters.HalftoneLinesFilterRender;
import com.pedro.encoder.input.gl.render.filters.Image70sFilterRender;
import com.pedro.encoder.input.gl.render.filters.LamoishFilterRender;
import com.pedro.encoder.input.gl.render.filters.MoneyFilterRender;
import com.pedro.encoder.input.gl.render.filters.NegativeFilterRender;
import com.pedro.encoder.input.gl.render.filters.NoFilterRender;
import com.pedro.encoder.input.gl.render.filters.PixelatedFilterRender;
import com.pedro.encoder.input.gl.render.filters.PolygonizationFilterRender;
import com.pedro.encoder.input.gl.render.filters.RGBSaturationFilterRender;
import com.pedro.encoder.input.gl.render.filters.RainbowFilterRender;
import com.pedro.encoder.input.gl.render.filters.RippleFilterRender;
import com.pedro.encoder.input.gl.render.filters.RotationFilterRender;
import com.pedro.encoder.input.gl.render.filters.SaturationFilterRender;
import com.pedro.encoder.input.gl.render.filters.SepiaFilterRender;
import com.pedro.encoder.input.gl.render.filters.SharpnessFilterRender;
import com.pedro.encoder.input.gl.render.filters.SnowFilterRender;
import com.pedro.encoder.input.gl.render.filters.SwirlFilterRender;
import com.pedro.encoder.input.gl.render.filters.TemperatureFilterRender;
import com.pedro.encoder.input.gl.render.filters.ZebraFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.GifObjectFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.ImageObjectFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.SurfaceFilterRender;
import com.pedro.encoder.input.gl.render.filters.object.TextObjectFilterRender;
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.encoder.utils.gl.TranslateTo;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.view.OpenGlView;
import com.pedro.rtpstreamer.MainActivity;
import com.pedro.rtpstreamer.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.ossrs.rtmp.ConnectCheckerRtmp;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;


//import org.videolan.libvlc.media.MediaPlayer;
import org.videolan.libvlc.MediaPlayer;
//import android.media.MediaPlayer;
import org.videolan.libvlc.util.VLCVideoLayout;
import org.w3c.dom.Text;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

//팝업창용 import
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface;

//sendbird
import com.sendbird.android.SendBird;
import com.sendbird.android.OpenChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 * {@link com.pedro.rtplibrary.rtmp.RtmpCamera1}
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OpenGlRtmpActivity extends AppCompatActivity
    implements ConnectCheckerRtmp, View.OnClickListener, SurfaceHolder.Callback,
    View.OnTouchListener {

  private RtmpCamera1 rtmpCamera1;
  private Button button;
  private Button bRecord;
  private EditText etUrl;

  private SurfaceFilterRender surfaceFilterRender;

  private String currentDateAndTime = "";
  private File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
          + "/rtmp-rtsp-stream-client-java");
  private OpenGlView openGlView;
  private SpriteGestureController spriteGestureController = new SpriteGestureController();
  VideoView videoView;
  Context context;
  private LibVLC mLibVLC = null;
  private MediaPlayer mMediaPlayer = null;

  private Uri mUri;

  private int count = 0;





  //sendbird APP ID
  private String APP_ID = "2651701A-6EE0-4519-A94D-F2286E7AAB01";
  // for sendbird connect
  private String USER_ID = "broadcaster";
  private String CHANNEL_URL = "";
  private OpenChannel mOpenChannel;
  //init title
  String init_t = null;
  // 송출자이므로 항상 방송 operator!
  List<User> operator = new ArrayList<>();




  ///로티 애니메이션 부분
  private int count_heart = 0;
  // 로티 애니메이션뷰 선언
  LottieAnimationView songLikeAnimButton;
  //LottieAnimationView Clicker01;
  // 좋아요 클릭 여부 확인용 텍스트뷰 선언
  TextView isSongLikeAnimButtonClickedTextView;
  //ImageView ClickIcon;
  // 좋아요 클릭 여부
  boolean isSongLikedClicked = false;

  //제목 수정 팝업용 변수
  TextView txt_dummy;

  //이벤트 쿠폰 팝업용 변수
  TextView coupon_name_dummy;
  TextView coupon_ect_dummy;
  TextView coupon_time_dummy;
  public String e_n = "";
  public String e_a="";
  public String e_t="";
  //TextView tv;

  //공지 수정 팝업용 변수
  TextView txt_dummy2;



  //n초뒤 사라지는 팝업을 위한 타이머용 변수
  public int timedown = 0;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    context = this;
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    setContentView(R.layout.activity_open_gl);
    openGlView = findViewById(R.id.surfaceView);
    button = findViewById(R.id.b_start_stop);
    button.setOnClickListener(this);
    bRecord = findViewById(R.id.b_record);
    bRecord.setOnClickListener(this);
    Button switchCamera = findViewById(R.id.switch_camera);
    switchCamera.setOnClickListener(this);

    ((Button) findViewById(R.id.surface_filter1)).setOnClickListener(this);
    ((Button) findViewById(R.id.surface_filter2)).setOnClickListener(this);
    ((Button) findViewById(R.id.surface_filter3)).setOnClickListener(this);

    etUrl = findViewById(R.id.et_rtp_url);
    etUrl.setHint(R.string.hint_rtmp);
//    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/84ea638dcc779f20647bb6a11cfa1fdf35f4152a");
//    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/a1b02ee3f7cbbcabf757178219e88d6239ec4890");
    etUrl.setText("rtmp://ingest.bambuser.io/b-fme/f91d39f8095317d3b01e139691b4aa91c8fc447a");
    rtmpCamera1 = new RtmpCamera1(openGlView, this);
    openGlView.getHolder().addCallback(this);
    openGlView.setOnTouchListener(this);


    SendBird.init(APP_ID, context); ;
    SendBird.connect(USER_ID, new SendBird.ConnectHandler() {
      @Override
      public void onConnected(User user, SendBirdException e) {
        if (e != null)   return;// Error.
        operator.add(user);
      }
    });

//    videoView = (VideoView) findViewById(R.id.view);
//    MediaController controller = new MediaController(this);
//    videoView.setMediaController(controller);
//
//    //비디오뷰 포커스를 요청함
//    videoView.requestFocus();
////    Uri uri =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );
////            mMediaPlayer.setDataSource("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
////    videoView.setVideoURI(uri);
//    Log.d("DARAM","surfaceFilterRender 123");
////    videoView.setVideoPath("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
//    videoView.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
//      @Override
//      public void onPrepared(android.media.MediaPlayer mp) {
//        Toast.makeText(context,"동영상이 준비되었습니다. 시작' 버튼을 누르세요", Toast.LENGTH_SHORT).show();
////        videoView.seekTo(0);
////        videoView.start();
//      }
//    });
//
//    //동영상 재생이 완료된 걸 알 수 있는 리스너
//    videoView.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
//      @Override
//      public void onCompletion(android.media.MediaPlayer mp) {
//        //동영상 재생이 완료된 후 호출되는 메소드
//        Toast.makeText(context,
//                "동영상 재생이 완료되었습니다.", Toast.LENGTH_SHORT).show();
//      }
//    });

    String tag = "daram";
    Flowable.just("Hello World").subscribe(new Consumer<String>() {
      @Override
      public void accept(String s) throws Exception {
        Log.v("DARAM", s);
      }
    });
    Flowable.just("Hello World !").subscribe(s -> Log.v("DARAM2", s));


    List<Integer> valueList = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    for (int data : valueList) {
      String result = "value " + data;
      Log.v(tag, result);
    }

    Flowable.fromIterable(valueList)
            .map(new Function<Integer, String>() {
              @Override
              public String apply(Integer data) throws Exception {
                return "value : " + data;
              }
            })
            //.map(data -> "value : " + data)
            .subscribe(data -> Log.v(tag, data));






    //로티 애니메이션
    //super.onCreate(savedInstanceState);
    //setContentView(R.layout.activity_main);
    // 로티 애니메이션뷰 리소스 아이디연결
    songLikeAnimButton = (LottieAnimationView)findViewById(R.id.button_song_like_animation);
    //Clicker01 = (LottieAnimationView)findViewById(R.id.clicker);
    // 텍스트뷰 리소스 아이디 연결
    isSongLikeAnimButtonClickedTextView = (TextView)findViewById(R.id.text_is_song_like_clicked);
    ((ImageView)findViewById(R.id.HeartIcon)).setOnClickListener(this);




    //제목수정버튼
    //((Button) findViewById(R.id.edit_title)).setOnClickListener(this);
    //제목 텍스트뷰
    //EditText EditTitle = (EditText) findViewById(R.id.broadcast_title) ;
//    EditTitle.setText("The text of EditText is changed.") ;




    //제목 수정 팝업 후 적용됨
    txt_dummy=(TextView)findViewById(R.id.txt_dummytext);


    //이벤트 팝업용
    coupon_name_dummy=(TextView)findViewById(R.id.blabla01);
    coupon_ect_dummy=(TextView)findViewById(R.id.blabla02);
    coupon_time_dummy=(TextView)findViewById(R.id.blabla03);

    //공지 수정 팝업 후 적용됨
    txt_dummy2=(TextView)findViewById(R.id.txt_dummytext2);


  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.gl_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //Stop listener for image, text and gif stream objects.
    spriteGestureController.setBaseObjectFilterRender(null);
    switch (item.getItemId()) {
      case R.id.e_d_fxaa:
        rtmpCamera1.getGlInterface().enableAA(!rtmpCamera1.getGlInterface().isAAEnabled());
        Toast.makeText(this,
                "FXAA " + (rtmpCamera1.getGlInterface().isAAEnabled() ? "enabled" : "disabled"),
                Toast.LENGTH_SHORT).show();
        return true;
      //filters. NOTE: You can change filter values on fly without reset the filter.
      // Example:
      // ColorFilterRender color = new ColorFilterRender()
      // rtmpCamera1.setFilter(color);
      // color.setRGBColor(255, 0, 0); //red tint
      case R.id.no_filter:
        rtmpCamera1.getGlInterface().setFilter(new NoFilterRender());
        return true;
      case R.id.analog_tv:
        rtmpCamera1.getGlInterface().setFilter(new AnalogTVFilterRender());
        return true;
      case R.id.android_view:
        AndroidViewFilterRender androidViewFilterRender = new AndroidViewFilterRender();
        androidViewFilterRender.setView(findViewById(R.id.ly_back2));
        rtmpCamera1.getGlInterface().setFilter(androidViewFilterRender);
        return true;
      case R.id.basic_deformation:
        rtmpCamera1.getGlInterface().setFilter(new BasicDeformationFilterRender());
        return true;
      case R.id.beauty:
        rtmpCamera1.getGlInterface().setFilter(new BeautyFilterRender());
        return true;
      case R.id.black:
        rtmpCamera1.getGlInterface().setFilter(new BlackFilterRender());
        return true;
      case R.id.blur:
        rtmpCamera1.getGlInterface().setFilter(new BlurFilterRender());
        return true;
      case R.id.brightness:
        rtmpCamera1.getGlInterface().setFilter(new BrightnessFilterRender());
        return true;
      case R.id.cartoon:
        rtmpCamera1.getGlInterface().setFilter(new CartoonFilterRender());
        return true;
      case R.id.circle:
        rtmpCamera1.getGlInterface().setFilter(new CircleFilterRender());
        return true;
      case R.id.color:
        rtmpCamera1.getGlInterface().setFilter(new ColorFilterRender());
        return true;
      case R.id.contrast:
        rtmpCamera1.getGlInterface().setFilter(new ContrastFilterRender());
        return true;
      case R.id.duotone:
        rtmpCamera1.getGlInterface().setFilter(new DuotoneFilterRender());
        return true;
      case R.id.early_bird:
        rtmpCamera1.getGlInterface().setFilter(new EarlyBirdFilterRender());
        return true;
      case R.id.edge_detection:
        rtmpCamera1.getGlInterface().setFilter(new EdgeDetectionFilterRender());
        return true;
      case R.id.exposure:
        rtmpCamera1.getGlInterface().setFilter(new ExposureFilterRender());
        return true;
      case R.id.fire:
        rtmpCamera1.getGlInterface().setFilter(new FireFilterRender());
        return true;
      case R.id.gamma:
        rtmpCamera1.getGlInterface().setFilter(new GammaFilterRender());
        return true;
      case R.id.glitch:
        rtmpCamera1.getGlInterface().setFilter(new GlitchFilterRender());
        return true;
      case R.id.gif:
        setGifToStream();
        return true;
      case R.id.grey_scale:
        rtmpCamera1.getGlInterface().setFilter(new GreyScaleFilterRender());
        return true;
      case R.id.halftone_lines:
        rtmpCamera1.getGlInterface().setFilter(new HalftoneLinesFilterRender());
        return true;
      case R.id.image:
        setImageToStream();
        return true;
      case R.id.image_70s:
        rtmpCamera1.getGlInterface().setFilter(new Image70sFilterRender());
        return true;
      case R.id.lamoish:
        rtmpCamera1.getGlInterface().setFilter(new LamoishFilterRender());
        return true;
      case R.id.money:
        rtmpCamera1.getGlInterface().setFilter(new MoneyFilterRender());
        return true;
      case R.id.negative:
        rtmpCamera1.getGlInterface().setFilter(new NegativeFilterRender());
        return true;
      case R.id.pixelated:
        rtmpCamera1.getGlInterface().setFilter(new PixelatedFilterRender());
        return true;
      case R.id.polygonization:
        rtmpCamera1.getGlInterface().setFilter(new PolygonizationFilterRender());
        return true;
      case R.id.rainbow:
        rtmpCamera1.getGlInterface().setFilter(new RainbowFilterRender());
        return true;
      case R.id.rgb_saturate:
        RGBSaturationFilterRender rgbSaturationFilterRender = new RGBSaturationFilterRender();
        rtmpCamera1.getGlInterface().setFilter(rgbSaturationFilterRender);
        //Reduce green and blue colors 20%. Red will predominate.
        //녹색과 파란색을 20 % 줄입니다. 적색이 우세합니다.
        rgbSaturationFilterRender.setRGBSaturation(1f, 0.8f, 0.8f);
        return true;
      case R.id.ripple:
        rtmpCamera1.getGlInterface().setFilter(new RippleFilterRender());
        return true;
      case R.id.rotation:
        RotationFilterRender rotationFilterRender = new RotationFilterRender();
        rtmpCamera1.getGlInterface().setFilter(rotationFilterRender);
        rotationFilterRender.setRotation(90);
        return true;
      case R.id.saturation:
        rtmpCamera1.getGlInterface().setFilter(new SaturationFilterRender());
        return true;
      case R.id.sepia:
        rtmpCamera1.getGlInterface().setFilter(new SepiaFilterRender());
        return true;
      case R.id.sharpness:
        rtmpCamera1.getGlInterface().setFilter(new SharpnessFilterRender());
        return true;
      case R.id.snow:
        rtmpCamera1.getGlInterface().setFilter(new SnowFilterRender());
        return true;
      case R.id.swirl:
        rtmpCamera1.getGlInterface().setFilter(new SwirlFilterRender());
        return true;
      case R.id.surface_filter:

        Log.d("DARAM", "surfaceFilterRender.getSurface() surface_filter ");
        //You can render this filter with other api that draw in a surface. for example you can use VLC


//          final ArrayList<String> args = new ArrayList<>();
//          args.add("-vvv");
//          mLibVLC = new LibVLC(this, args);

//          final String ASSET_FILENAME = "bbb.m4v";

        surfaceFilterRender = new SurfaceFilterRender();
//

        rtmpCamera1.getGlInterface().setFilter(surfaceFilterRender);

//        VLCVideoLayout mVideoLayout = findViewById(R.id.video_layout);
//        mVideoLayout.setVisibility(View.GONE);
//        mMediaPlayer.attachViews(mVideoLayout, null, true, false);

        if (surfaceFilterRender.getSurface() == null) {
          Log.d("DARAM", "surfaceFilterRender.getSurface() == null ");
          handler.sendEmptyMessageDelayed(10, 10);
          return true;
        }

//          mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurface(), new SurfaceHolder() {
//              @Override
//              public void addCallback(Callback callback) {
//
//              }
//
//              @Override
//              public void removeCallback(Callback callback) {
//
//              }
//
//              @Override
//              public boolean isCreating() {
//                  return false;
//              }
//
//              @Override
//              public void setType(int type) {
//
//              }
//
//              @Override
//              public void setFixedSize(int width, int height) {
//
//              }
//
//              @Override
//              public void setSizeFromLayout() {
//
//              }
//
//              @Override
//              public void setFormat(int format) {
//
//              }
//
//              @Override
//              public void setKeepScreenOn(boolean screenOn) {
//
//              }
//
//              @Override
//              public Canvas lockCanvas() {
//                  return null;
//              }
//
//              @Override
//              public Canvas lockCanvas(Rect dirty) {
//                  return null;
//              }
//
//              @Override
//              public void unlockCanvasAndPost(Canvas canvas) {
//
//              }
//
//              @Override
//              public Rect getSurfaceFrame() {
//                  return null;
//              }
//
//              @Override
//              public Surface getSurface() {
//                  return null;
//              }
//          });
//          try {
//              final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
//              mMediaPlayer.setMedia(media);
//
//              media.release();
//
//          } catch (IOException e) {
//              throw new RuntimeException("Invalid asset folder");
//          }
//          mMediaPlayer.play();

//        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.big_bunny_240p);
//        mediaPlayer.setSurface(surfaceFilterRender.getSurface());
//        mediaPlayer.start();
        //Video is 360x240 so select a percent to keep aspect ratio (50% x 33.3% screen)

//        VLCVideoLayout mVideoLayout = findViewById(R.id.video_layout);
//        mVideoLayout.setVisibility(View.GONE);
//        mMediaPlayer.attachViews(mVideoLayout, null, true, false);

//        surfaceFilterRender.setScale(50f, 33.3f);
//        spriteGestureController.setBaseObjectFilterRender(surfaceFilterRender); //Optional
        return true;
      case R.id.temperature:
        rtmpCamera1.getGlInterface().setFilter(new TemperatureFilterRender());
        return true;
      case R.id.text:
        setTextToStream();
        return true;
      case R.id.zebra:
        rtmpCamera1.getGlInterface().setFilter(new ZebraFilterRender());
        return true;
      default:
        return false;
    }
  }

  @SuppressLint("HandlerLeak")
  public Handler handler = new Handler() {
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case 10:
          Log.d("DARAM", "surfaceFilterRender.getSurface() handler ");
          if (surfaceFilterRender.getSurface() == null) {
            Log.d("DARAM", "surfaceFilterRender.getSurface() == null ");
            handler.sendEmptyMessageDelayed(10, 10);
          }
          findViewById(R.id.tv_loading).setVisibility(View.GONE);

//          final ArrayList<String> args = new ArrayList<>();
//          args.add("-vvv");
//          mLibVLC = new LibVLC(context, args);
//
//          final String ASSET_FILENAME = "bbb.m4v";


//          mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurfaceTexture());
//          mMediaPlayer.getVLCVout().setSubtitlesSurface(surfaceFilterRender.getSurfaceTexture());
//          Log.d("DARAM","surfaceFilterRender.getSurface() setVideoSurface ");
//          mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurface(), new SurfaceHolder() {
//            @Override
//            public void addCallback(Callback callback) {
//                    Log.d("DARAM","surfaceFilterRender.getSurface() addCallback ");
//            }
//
//            @Override
//            public void removeCallback(Callback callback) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() removeCallback ");
//            }
//
//            @Override
//            public boolean isCreating() {
//              Log.d("DARAM","surfaceFilterRender.getSurface() isCreating ");
//              return false;
//            }
//
//            @Override
//            public void setType(int type) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() setType ");
//
//            }
//
//            @Override
//            public void setFixedSize(int width, int height) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() setFixedSize ");
//            }
//
//            @Override
//            public void setSizeFromLayout() {
//              Log.d("DARAM","surfaceFilterRender.getSurface() setSizeFromLayout ");
//            }
//
//            @Override
//            public void setFormat(int format) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() setFormat ");
//            }
//
//            @Override
//            public void setKeepScreenOn(boolean screenOn) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() setKeepScreenOn ");
//            }
//
//            @Override
//            public Canvas lockCanvas() {
//              Log.d("DARAM","surfaceFilterRender.getSurface() lockCanvas ");
//              return null;
//            }
//
//            @Override
//            public Canvas lockCanvas(Rect dirty) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() lockCanvas rect ");
//              return null;
//            }
//
//            @Override
//            public void unlockCanvasAndPost(Canvas canvas) {
//              Log.d("DARAM","surfaceFilterRender.getSurface() unlockCanvasAndPost ");
//            }
//
//            @Override
//            public Rect getSurfaceFrame() {
//              Log.d("DARAM","surfaceFilterRender.getSurface() getSurfaceFrame ");
//              return null;
//            }
//
//            @Override
//            public Surface getSurface() {
//              Log.d("DARAM","surfaceFilterRender.getSurface() getSurfaceFrame ");
//              return null;
//            }
//          });
          try {
            final ArrayList<String> args = new ArrayList<>();
            args.add("-vvv");
            args.add("-vvv");
            mLibVLC = new LibVLC(context, args);

            mMediaPlayer = new MediaPlayer(mLibVLC);
            final String ASSET_FILENAME = "bbb.m4v";
//            mMediaPlayer.
//            final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));

//            Uri uri =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );
            final Media media = new Media(mLibVLC, mUri);
//            VLCVideoLayout mVideoLayout = findViewById(R.id.video_layout);
//            mMediaPlayer.attachViews(mVideoLayout, null, true, false);
//            surfaceFilterRender.getSurface()
//            mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurfaceTexture());
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 1 259: " + MediaPlayer.Event.Buffering);
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 2 260: " + MediaPlayer.Event.Playing);
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 3 261: " + MediaPlayer.Event.Paused);
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 4 262: " + MediaPlayer.Event.Stopped);
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 5 266: " + MediaPlayer.Event.EncounteredError);
            Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent 6 265: " + MediaPlayer.Event.EndReached);

/*
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 1 : 259
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 2 : 260
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 3 : 261
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 4 : 262
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 5 : 266
2019-11-19 15:24:26.923 32081-32081/com.pedro.rtpstreamer D/DARAM: surfaceFilterRender.getSurface() onEvent 6 : 265
 */
            mMediaPlayer.getVLCVout().setVideoSurface(surfaceFilterRender.getSurface(), null);
            mMediaPlayer.getVLCVout().attachViews(null);
            mMediaPlayer.setEventListener(new MediaPlayer.EventListener() {
              @Override
              public void onEvent(MediaPlayer.Event event) {
//                Log.d("DARAM","surfaceFilterRender.getSurface() onEvent " + event.type);
                switch (event.type) {
//                    Opening             = 0x102;
//                    public static final int Buffering           = 0x103;
//                    public static final int Playing             = 0x104;
//                    public static final int Paused              = 0x105;
//                    public static final int Stopped             = 0x106;
                  case MediaPlayer.Event.Buffering:

                    break;
                  case MediaPlayer.Event.EncounteredError:
                  case MediaPlayer.Event.EndReached:

                    break;

                  case MediaPlayer.Event.Stopped:
                    Log.d("DARAM", "surfaceFilterRender.getSurface() onEvent Stopped ");
                    rtmpCamera1.getGlInterface().setFilter(new NoFilterRender());

//                      try {
//                        handler.sendEmptyMessageDelayed(10,1000);
//                      } catch (Exception e) {
//                        e.printStackTrace();
//                      }
//

                    break;
                }

              }
            });
            mMediaPlayer.setMedia(media);

            media.release();


//            Uri uri =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );


//            mMediaPlayer = MediaPlayer.create(context,uri);

//            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer = MediaPlayer.create(this, R.raw.big_bunny_240p);
//            mMediaPlayer = MediaPlayer.create(this, R.raw.big_bunny_240p);
//            Uri uri = new Uri("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
//            Uri uri =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );
//            mMediaPlayer.setDataSource("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
//            mMediaPlayer.setDataSource(context, uri);
//            final Media media = new Media(mLibVLC, getAssets().openFd(ASSET_FILENAME));
//
//            mMediaPlayer.setSurface(surfaceFilterRender.getSurface());
//            mMediaPlayer.release();
//            mMediaPlayer.setMedia(media);

            Log.d("DARAM", "surfaceFilterRender.getSurface() setMedia ");
//            media.release();
            Log.d("DARAM", "surfaceFilterRender.getSurface() release ");


            Log.d("DARAM", "surfaceFilterRender.getSurface() play ");

//          mMediaPlayer.start();
            Log.d("DARAM", "surfaceFilterRender.getSurface() play2 ");


//            Uri uri2 =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );
//            android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(context,uri2);
////            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.big_bunny_240p);
//            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
//              @Override
//              public boolean onError(MediaPlayer mp, int what, int extra) {
//                Log.d("DARAM","surfaceFilterRender.getSurface() onError ");
//                return false;
//              }
//            });
//            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
//              @Override
//              public boolean onInfo(MediaPlayer mp, int what, int extra) {
//                return false;
//              }
//            });
//
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//              @Override
//              public void onCompletion(MediaPlayer mp) {
//                Log.i("media player", "play next please!");
//                if (mp != null) {
//                  mp.release();
//                }
//////            play next video
////                currentMedia++;
////                if (currentMedia > playList.size() - 1) {
////                  currentMedia = 0;
////                }
////                try {
////                  playMedia(currentMedia);
////                } catch (IOException e) {
////                  e.printStackTrace();
////                }
//              }
//            });
//
//            mediaPlayer.setSurface(surfaceFilterRender.getSurface());
//            mediaPlayer.setLooping(true);
//
//

            //Video is 360x240 so select a percent to keep aspect ratio (50% x 33.3% screen)
          } catch (Exception e) {
            Log.d("DARAM", "surfaceFilterRender.getSurface() Exception ");
            e.printStackTrace();
            throw new RuntimeException("Invalid asset folder");
          }

          mMediaPlayer.play();
          if (mUri.toString().contains("test009.mp4"))
            surfaceFilterRender.setScale(100f, 100f);
          else
            surfaceFilterRender.setScale(50f, 33.3f);
          spriteGestureController.setBaseObjectFilterRender(surfaceFilterRender); //Optional

          break;
        default:
          throw new IllegalStateException("Unexpected value: " + msg.what);
      }
    }
  };


  private void setTextToStream() {
    TextObjectFilterRender textObjectFilterRender = new TextObjectFilterRender();
    rtmpCamera1.getGlInterface().setFilter(0, textObjectFilterRender);
    textObjectFilterRender.setText("Hello world", 22, Color.RED);
    textObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(),
            rtmpCamera1.getStreamHeight());
    textObjectFilterRender.setPosition(TranslateTo.CENTER);
//    spriteGestureController.setBaseObjectFilterRender(textObjectFilterRender); //Optional
  }

  private void setImageToStream() {
    ImageObjectFilterRender imageObjectFilterRender = new ImageObjectFilterRender();
    rtmpCamera1.getGlInterface().setFilter(0, imageObjectFilterRender);
    imageObjectFilterRender.setImage(
            BitmapFactory.decodeResource(getResources(), R.mipmap.homiimg));
    imageObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(),
            rtmpCamera1.getStreamHeight());
//    imageObjectFilterRender.setDefaultScale(50,
//            50);

    imageObjectFilterRender.setScale(50f, 33.3f);
    Log.d("DARAM", "getStreamsize " + rtmpCamera1.getStreamWidth() + " : " +
            rtmpCamera1.getStreamHeight());

    imageObjectFilterRender.setPosition(TranslateTo.RIGHT);

    spriteGestureController.setBaseObjectFilterRender(imageObjectFilterRender); //Optional
    spriteGestureController.setPreventMoveOutside(false); //Optional
  }

  private void setGifToStream() {
    try {
      GifObjectFilterRender gifObjectFilterRender = new GifObjectFilterRender();
      gifObjectFilterRender.setGif(getResources().openRawResource(R.raw.banana));
      rtmpCamera1.getGlInterface().setFilter(gifObjectFilterRender);
      gifObjectFilterRender.setDefaultScale(rtmpCamera1.getStreamWidth(),
              rtmpCamera1.getStreamHeight());

      gifObjectFilterRender.setPosition(TranslateTo.BOTTOM);
      spriteGestureController.setBaseObjectFilterRender(gifObjectFilterRender); //Optional
    } catch (IOException e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  //연결성공
  @Override
  public void onConnectionSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(OpenGlRtmpActivity.this, "Connection success", Toast.LENGTH_SHORT).show();
      }
    });
  }

  //연결실패
  @Override
  public void onConnectionFailedRtmp(final String reason) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(OpenGlRtmpActivity.this, "Connection failed. " + reason, Toast.LENGTH_SHORT)
                .show();
        rtmpCamera1.stopStream();
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
        Toast.makeText(OpenGlRtmpActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthErrorRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(OpenGlRtmpActivity.this, "Auth error", Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onAuthSuccessRtmp() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(OpenGlRtmpActivity.this, "Auth success", Toast.LENGTH_SHORT).show();
      }
    });
  }




  // 좋아요 로띠 애니메이션을 실행 시키는 메소드
  private boolean toggleSongLikeAnimButton(){
    // 애니메이션을 한번 실행시킨다.
    // Custom animation speed or duration.
    // ofFloat(시작 시간, 종료 시간).setDuration(지속시간)
    ValueAnimator animator = ValueAnimator.ofFloat(0f, 0.6f).setDuration(500);

    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        songLikeAnimButton.setProgress((Float) animation.getAnimatedValue());
      }
    });
    animator.start();

    return true;

  }





  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.b_start_stop:
        if (!rtmpCamera1.isStreaming()) {
          if (rtmpCamera1.isRecording()
                  || rtmpCamera1.prepareAudio() && rtmpCamera1.prepareVideo()) {
            button.setText(R.string.stop_button);
            rtmpCamera1.startStream(etUrl.getText().toString());
            // 방송 시작 -> 제목받고, 채팅 채널 생성 및 enter
            create_title(view);
          } else {
            Toast.makeText(this, "Error preparing stream, This device cant do it",
                    Toast.LENGTH_SHORT).show();
          }
        } else {
          button.setText(R.string.start_button);
          rtmpCamera1.stopStream();
          //방송 종료시 방송 채널 삭제
          mOpenChannel.delete(new OpenChannel.OpenChannelDeleteHandler() {
            @Override
            public void onResult(SendBirdException e) {
            }
          });
        }
        break;
      case R.id.switch_camera:
        try {
          rtmpCamera1.switchCamera();
        } catch (CameraOpenException e) {
          Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.surface_filter1:
        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
        mUri = Uri.parse("https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4");
        surfaceFilterRender = new SurfaceFilterRender();
        rtmpCamera1.getGlInterface().setFilter(surfaceFilterRender);
        Log.d("DARAM", "surfaceFilterRender.getSurface() == null ");
        handler.sendEmptyMessageDelayed(10, 100);
        //        setTextToStream();


//
////        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
//
//
//        class NewRunnable implements Runnable {
//
//
//          @Override
//          public void run() {
//            mUri =  Uri.parse("http://3pigs.iptime.org:19898/apk/test009.mp4");
//            surfaceFilterRender = new SurfaceFilterRender();
//            rtmpCamera1.getGlInterface().setFilter(surfaceFilterRender);
//            spriteGestureController.setBaseObjectFilterRender(surfaceFilterRender); //Optional
//            Log.d("DARAM","surfaceFilterRender.getSurface() == null ");
//            handler.sendEmptyMessageDelayed(10,1000);
//              try {
//                Thread.sleep(1000) ;
//              } catch (Exception e) {
//                e.printStackTrace() ;
//              }
//          }
//        }
//
//        NewRunnable nr = new NewRunnable() ;
//        Thread t = new Thread(nr) ;
//        t.start() ;


        break;
      case R.id.surface_filter2:
        AndroidViewFilterRender androidViewFilterRender = new AndroidViewFilterRender();

        View lyview = findViewById(R.id.ly_back2);
        lyview.setX(10000f);
        lyview.setVisibility(View.VISIBLE);
        androidViewFilterRender.setView(lyview);


        rtmpCamera1.getGlInterface().setFilter(androidViewFilterRender);
//        findViewById(R.id.tv_loading).setVisibility(View.VISIBLE);
//        mUri =  Uri.parse( "https://s3.ap-northeast-2.amazonaws.com/asset.solmaru.co.kr/BRD/qRusKMnmVg.mp4" );
//        surfaceFilterRender = new SurfaceFilterRender();
//        rtmpCamera1.getGlInterface().setFilter(surfaceFilterRender);
//        Log.d("DARAM","surfaceFilterRender.getSurface() == null ");
//        handler.sendEmptyMessageDelayed(10,1000);

        break;
      case R.id.b_record:
        if (!rtmpCamera1.isRecording()) {
          try {
            if (!folder.exists()) {
              folder.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            currentDateAndTime = sdf.format(new Date());
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
              rtmpCamera1.startRecord(folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
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
        break;
      case R.id.surface_filter3:
//        count++;
//        ((TextView)findViewById(R.id.tv_masage)).setText(count + ": 동해물과백두산이 마르고 닮도록 하느님이 보우하사 우리나라만세"  );
        setImageToStream();
        break;

        //로티 애니메이션 실행 스위치 케이스문
      case R.id.HeartIcon:
//        final String clapCountText = "총 " + String.valueOf(++count_heart) + "명이 하트를 눌렀어요!";
        final String clapCountText = String.valueOf(++count_heart);
        // 애니메이션을 발동시킨다.
        if(toggleSongLikeAnimButton()) {
          // 좋아요 상태이면
          isSongLikeAnimButtonClickedTextView.setText(clapCountText);
        }
        break;
//      case R.id.edit_title:
////        EditTitle.setText("The text of EditText is changed.") ;
//        new AlertDialog.Builder(OpenGlRtmpActivity.this)
//                .setTitle("알람 팝업")
//                .setMessage("팝업 창의 내용입니다.\n\n TEST!!")
//                .setNeutralButton("닫기", new DialogInterface.OnClickListener() {
//                  public void onClick(DialogInterface dlg, int sumthin) {
//                  }
//                })
//                .show(); // 팝업창 보여줌
//        break;

      default:
        break;
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {

  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    rtmpCamera1.startPreview();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (rtmpCamera1.isRecording()) {
      rtmpCamera1.stopRecord();
      bRecord.setText(R.string.start_record);
      Toast.makeText(this,
              "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
              Toast.LENGTH_SHORT).show();
      currentDateAndTime = "";
    }
    if (rtmpCamera1.isStreaming()) {
      rtmpCamera1.stopStream();
      button.setText(getResources().getString(R.string.start_button));
    }
    rtmpCamera1.stopPreview();
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    if (spriteGestureController.spriteTouched(view, motionEvent)) {
      spriteGestureController.moveSprite(view, motionEvent);
      spriteGestureController.scaleSprite(motionEvent);
      return true;
    }
    return false;
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    try {
      mMediaPlayer.release();
      mLibVLC.release();
    } catch (Exception e) {

    }
  }









  // 방송 시작 첫 제목 설정
  public void create_title(View view) {
    final AlertDialog.Builder alert = new AlertDialog.Builder(OpenGlRtmpActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.init_channel, null);

    final EditText newtitle = (EditText)mView.findViewById(R.id.init_title);
    Button btn_cancel = (Button)mView.findViewById(R.id.init_cancel);
    Button btn_ok = (Button)mView.findViewById(R.id.init_ok);

    alert.setView(mView);

    final AlertDialog alertDialog = alert.create();
    alertDialog.setCanceledOnTouchOutside(false);

    btn_cancel.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    btn_ok.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        //txt_dummy.setText(newtitle.getText().toString());
        init_t = newtitle.getText().toString();
        if(init_t != null){
          OpenChannel.createChannel(init_t, null, null, null, operator, new OpenChannel.OpenChannelCreateHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
              if (e != null) {
                return;
              }
              //방송 생성시에만 방송 url받아서 들어가기 위한 준비.
              CHANNEL_URL = openChannel.getUrl();
              mOpenChannel = openChannel;
              /*
              //예제에 있던 코드
              Intent intent = new Intent();
              setResult(RESULT_OK, intent);
              finish();*/
            }
          });
          OpenChannel.getChannel(CHANNEL_URL, new OpenChannel.OpenChannelGetHandler() {
            @Override
            public void onResult(OpenChannel openChannel, SendBirdException e) {
              if (e != null) {
                return;
              }// Error.
              openChannel.enter(new OpenChannel.OpenChannelEnterHandler() {
                @Override
                public void onResult(SendBirdException e) {
                  if (e != null) {
                    return;
                  }
                }
              });
            }
          });
        }
        txt_dummy.setText(newtitle.getText().toString());
        alertDialog.dismiss();
      }
    });

    alertDialog.show();
  }








  //제목 수정 팝업창
  public void btn_showDialog(View view) {
    final AlertDialog.Builder alert = new AlertDialog.Builder(OpenGlRtmpActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

    final EditText txt_inputText = (EditText)mView.findViewById(R.id.init_title);
    Button btn_cancel = (Button)mView.findViewById(R.id.btn_cancel);
    Button btn_ok = (Button)mView.findViewById(R.id.btn_ok);

    alert.setView(mView);

    final AlertDialog alertDialog = alert.create();
    alertDialog.setCanceledOnTouchOutside(false);

    btn_cancel.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    btn_ok.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view) {
        init_t = txt_inputText.getText().toString();
        txt_dummy.setText(txt_inputText.getText().toString());
        String coverUrl = mOpenChannel.getCoverUrl();
        mOpenChannel.updateChannel(init_t, coverUrl, "null", new OpenChannel.OpenChannelUpdateHandler() {
          @Override
          public void onResult(OpenChannel openChannel, SendBirdException e) {
            if(e != null) {}
          }
        });
        alertDialog.dismiss();
      }
    });

    alertDialog.show();
  }






  //쿠폰 이벤트 설정 팝업창
  public void btn_editPopUp(View view) {
    final AlertDialog.Builder alert = new AlertDialog.Builder(OpenGlRtmpActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.popup_custom_dialog, null);

    final EditText txt_coupon_name = (EditText)mView.findViewById(R.id.blabla01);
    final EditText txt_coupon_ect = (EditText)mView.findViewById(R.id.blabla02);
    final EditText txt_coupon_time = (EditText)mView.findViewById(R.id.blabla03);
    Button coupon_btn_ok_02 = (Button)mView.findViewById(R.id.coupon_btn_ok_02);
    Button coupon_btn_cancel_02 = (Button)mView.findViewById(R.id.coupon_btn_cancel_02);
//    final CheckBox cb1 = (CheckBox)findViewById(R.id.checkbox01);
//    final CheckBox cb2 = (CheckBox)findViewById(R.id.checkbox02);
//    final CheckBox cb3 = (CheckBox)findViewById(R.id.checkbox03);
//    final TextView tv = (TextView)findViewById(R.id.test01);
    String result = ""; // 문자열 초기화는 빈문자열로 하자

    alert.setView(mView);

    final AlertDialog alertDialog = alert.create();
    alertDialog.setCanceledOnTouchOutside(false);


//    cb1.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
//    cb2.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
//    cb3.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) this);
//    if(cb1.isChecked()) result += cb1.getText().toString() + ", ";
//    if(cb2.isChecked()) result += cb2.getText().toString() + ". ";
//    if(cb3.isChecked()) result += cb3.getText().toString() + ". ";
//    tv.setText("체크항목: " + result);

    coupon_btn_cancel_02.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    coupon_btn_ok_02.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view) {
//        coupon_name_dummy.setText(txt_coupon_name.getText().toString());
//        coupon_ect_dummy.setText(txt_coupon_ect.getText().toString());
//        coupon_time_dummy.setText(txt_coupon_time.getText().toString());
        e_n=txt_coupon_name.getText().toString();
        e_a=txt_coupon_ect.getText().toString();
        e_t=txt_coupon_time.getText().toString();
        alertDialog.dismiss();
      }
    });

    alertDialog.show();
  }




  //쿠폰 이벤트 생성 팝업창
  public void btn_showPopUp (View view){
    final AlertDialog.Builder alert01 = new AlertDialog.Builder(OpenGlRtmpActivity.this);
    View mView01 = getLayoutInflater().inflate(R.layout.popup, null);
    final EditText coupon_name_txt = (EditText)mView01.findViewById(R.id.blabla011);
    final EditText coupon_ect_txt = (EditText)mView01.findViewById(R.id.blabla022);
    final EditText coupon_time_txt = (EditText)mView01.findViewById(R.id.blabla033);
    Button coupon_btn_cancel_01 = (Button)mView01.findViewById(R.id.coupon_btn_cancel_01);
    Button coupon_btn_ok_01 = (Button)mView01.findViewById(R.id.coupon_btn_ok_01);

    coupon_name_txt.setText(e_n);
    coupon_ect_txt.setText(e_a);
    coupon_time_txt.setText(e_t);

    alert01.setView(mView01);

    //팝업 띄우기 타이머용 변수
    Integer i = Integer.parseInt( "" + coupon_time_txt.getText() );
    final int timer_time = i * 1000;

    final AlertDialog alertDialog = alert01.create();
    alertDialog.setCanceledOnTouchOutside(false);

    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        // 3초가 지나면 다이얼로그 닫기
        TimerTask task = new TimerTask(){
          @Override
          public void run() {
            alertDialog.dismiss();
          }
        };

        Timer timer = new Timer();
        timer.schedule(task, timer_time);
      }
    });
    thread.start();

    coupon_btn_cancel_01.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    coupon_btn_ok_01.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });


    alertDialog.show();
  }





  //공지 수정 팝업창
  public void btn_showDialog2(View view) {
    final AlertDialog.Builder alert03 = new AlertDialog.Builder(OpenGlRtmpActivity.this);
    View mView = getLayoutInflater().inflate(R.layout.notification_custom_dialog, null);

    final EditText txt_inputText2 = (EditText)mView.findViewById(R.id.txt_input2);
    Button btn_cancel2 = (Button)mView.findViewById(R.id.btn_cancel2);
    Button btn_ok2 = (Button)mView.findViewById(R.id.btn_ok2);

    alert03.setView(mView);

    final AlertDialog alertDialog = alert03.create();
    alertDialog.setCanceledOnTouchOutside(false);

    btn_cancel2.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    btn_ok2.setOnClickListener(new View.OnClickListener(){

      @Override
      public void onClick(View view) {
        txt_dummy2.setText(txt_inputText2.getText().toString());
        alertDialog.dismiss();
      }
    });

    alertDialog.show();
  }







  //시청자 목록 보는 팝업창
  public void btn_showPeople(View view) {
    View mView = getLayoutInflater().inflate(R.layout.popup_people, null);

    //ListView, ArrayList 및 Adapter 생성
    // 빈 데이터 리스트 생성.
    final ArrayList<String> items = new ArrayList<String>() ;
    // ArrayAdapter 생성. 아이템 View를 선택(multiple choice)가능하도록 만듦.
    final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, items) ;
    // listview 생성 및 adapter 지정.
    final ListView listview = (ListView) mView.findViewById(R.id.listview1) ;
    listview.setAdapter(adapter) ;

    final AlertDialog.Builder alert04 = new AlertDialog.Builder(OpenGlRtmpActivity.this);

//    final EditText txt_inputText2 = (EditText)mView.findViewById(R.id.txt_input2);
    Button btn_cancel = (Button)mView.findViewById(R.id.popup_cancel);
    Button addButton = (Button)mView.findViewById(R.id.for_test);
    Button deleteButton = (Button)mView.findViewById(R.id.ben);
    Button selectAllButton = (Button)mView.findViewById(R.id.select_all);

    alert04.setView(mView);

    final AlertDialog alertDialog = alert04.create();
    alertDialog.setCanceledOnTouchOutside(false);

    btn_cancel.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View view) {
        alertDialog.dismiss();
      }
    });

    //"Add" Button 클릭 시 아이템 추가.
    addButton.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        int count;
        count = adapter.getCount();
        // 아이템 추가.
        items.add("LIST" + Integer.toString(count + 1));
        // listview 갱신
        adapter.notifyDataSetChanged();
      }
    }) ;

    //"Delete" Button 클릭 시 선택 아이템 삭제.
    deleteButton.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        SparseBooleanArray checkedItems = listview.getCheckedItemPositions();
        int count = adapter.getCount() ;
        for (int i = count-1; i >= 0; i--) {
          if (checkedItems.get(i)) {
            items.remove(i) ;
          }
        }
        // 모든 선택 상태 초기화.
        listview.clearChoices() ;
        adapter.notifyDataSetChanged();
      }
    }) ;

    //"Select All" Button 클릭 시 모든 아이템 선택.
    selectAllButton.setOnClickListener(new Button.OnClickListener() {
      public void onClick(View v) {
        int count = 0 ;
        count = adapter.getCount() ;

        for (int i=0; i<count; i++) {
          listview.setItemChecked(i, true) ;
        }
      }
    }) ;


    alertDialog.show();
  }


}