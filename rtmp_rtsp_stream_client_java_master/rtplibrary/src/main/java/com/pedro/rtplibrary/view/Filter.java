package com.pedro.rtplibrary.view;

import com.pedro.encoder.input.gl.render.filters.BaseFilterRender;

/**
 * Created by pedro on 30/07/18.
 */

public class Filter {

  private int position;
  private BaseFilterRender baseFilterRender;

  ////
  private int type;

  public Filter() {
  }

  public Filter(int position, BaseFilterRender baseFilterRender) {
    this.position = position;
    this.baseFilterRender = baseFilterRender;
  }

  public Filter(int position, BaseFilterRender baseFilterRender, int type) {
    this.position = position;
    this.baseFilterRender = baseFilterRender;
    this.type = type;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public BaseFilterRender getBaseFilterRender() {
    return baseFilterRender;
  }

  public void setBaseFilterRender(BaseFilterRender baseFilterRender) {
    this.baseFilterRender = baseFilterRender;
  }

  public void setType(int type){
    this.type = type;
  }

  public int getType(){
    return this.type;
  }
}
