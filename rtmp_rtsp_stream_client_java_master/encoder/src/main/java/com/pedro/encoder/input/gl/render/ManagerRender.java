package com.pedro.encoder.input.gl.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;
import com.pedro.encoder.input.gl.render.filters.NoFilterRender;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedro on 27/01/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ManagerRender {

    //Increase it to render more than 1 filter and set filter by position.
    //You must modify it before create your rtmp or rtsp object.
    public static int numFilters = 3;

    private CameraRender cameraRender;
    private List<BaseFilterRender> baseFilterRender = new ArrayList<>(numFilters);
    private ScreenRender screenRender;

    private int width;
    private int height;
    private int previewWidth;
    private int previewHeight;
    private Context context;

    public ManagerRender() {
        Log.d("DARAM","ManagerRender 생성자") ;
        cameraRender = new CameraRender();
        for (int i = 0; i < numFilters; i++) baseFilterRender.add(new NoFilterRender());
        screenRender = new ScreenRender();

        //
    }

    public void initGl(Context context, int encoderWidth, int encoderHeight, int previewWidth,
                       int previewHeight) {
        this.context = context;
        this.width = encoderWidth;
        this.height = encoderHeight;
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
        cameraRender.initGl(width, height, context, previewWidth, previewHeight);
        for (int i = 0; i < numFilters; i++) {
            int textId = i == 0 ? cameraRender.getTexId() : baseFilterRender.get(i - 1).getTexId();
            baseFilterRender.get(i).setPreviousTexId(textId);
            baseFilterRender.get(i).initGl(width, height, context, previewWidth, previewHeight);
            baseFilterRender.get(i).initFBOLink();
        }
        screenRender.setStreamSize(encoderWidth, encoderHeight);
        screenRender.setTexId(baseFilterRender.get(numFilters - 1).getTexId());
        screenRender.initGl(context);
    }

    public void drawOffScreen() {
        cameraRender.draw();
        for (BaseFilterRender baseFilterRender : baseFilterRender) baseFilterRender.draw();
    }

    public void drawScreen(int width, int height, boolean keepAspectRatio) {
        screenRender.draw(width, height, keepAspectRatio);
    }

    public void release() {
        cameraRender.release();
        for (int i = 0; i < this.baseFilterRender.size(); i++) {
            this.baseFilterRender.get(i).release();
            this.baseFilterRender.set(i, new NoFilterRender());
        }
        screenRender.release();
    }

    public void enableAA(boolean AAEnabled) {
        screenRender.setAAEnabled(AAEnabled);
    }

    public boolean isAAEnabled() {
        return screenRender.isAAEnabled();
    }

    public void updateFrame() {
        cameraRender.updateTexImage();
    }

    public SurfaceTexture getSurfaceTexture() {
        return cameraRender.getSurfaceTexture();
    }

    public Surface getSurface() {
        return cameraRender.getSurface();
    }

    //
//    public void removeFilter(int pos){
//        BaseFilterRender bf = baseFilterRender.get(1);
//        setFilterNoR(1, new NoFilterRender(1));
//        setFilterNoI(0, bf);
//    }

    public void removeFilter(int pos){
        if(pos == numFilters-1) {
            setFilter(pos, new NoFilterRender(1));
            return;
        }
        for(int i = pos; i<numFilters-1; i++){
            BaseFilterRender bf = baseFilterRender.get(i+1);
            setFilterNoR(i+1, new NoFilterRender(1));
            setFilterNoI(i, bf);
        }
    }

    public void setFilter(int position, BaseFilterRender baseFilterRender) {
        final int id = this.baseFilterRender.get(position).getPreviousTexId();
        final RenderHandler renderHandler = this.baseFilterRender.get(position).getRenderHandler();
        this.baseFilterRender.get(position).release();
        this.baseFilterRender.set(position, baseFilterRender);
        this.baseFilterRender.get(position).setPreviousTexId(id);
        this.baseFilterRender.get(position).initGl(width, height, context, previewWidth, previewHeight);
        this.baseFilterRender.get(position).setRenderHandler(renderHandler);
    }

    public void setFilterNoI(int position, BaseFilterRender baseFilterRender) {
        final int id = this.baseFilterRender.get(position).getPreviousTexId();
        final RenderHandler renderHandler = this.baseFilterRender.get(position).getRenderHandler();
        this.baseFilterRender.get(position).release();
        this.baseFilterRender.set(position, baseFilterRender);
        this.baseFilterRender.get(position).setPreviousTexId(id);
        this.baseFilterRender.get(position).setRenderHandler(renderHandler);
    }

    public void setFilterNoR(int position, BaseFilterRender baseFilterRender) {
        final int id = this.baseFilterRender.get(position).getPreviousTexId();
        final RenderHandler renderHandler = this.baseFilterRender.get(position).getRenderHandler();
        this.baseFilterRender.set(position, baseFilterRender);
        this.baseFilterRender.get(position).setPreviousTexId(id);
        this.baseFilterRender.get(position).initGl(width, height, context, previewWidth, previewHeight);
        this.baseFilterRender.get(position).setRenderHandler(renderHandler);
    }

    public void setCameraRotation(int rotation) {
        cameraRender.setRotation(rotation);
    }

    public void setCameraFlip(boolean isFlipHorizontal, boolean isFlipVertical) {
        cameraRender.setFlip(isFlipHorizontal, isFlipVertical);
    }
}
