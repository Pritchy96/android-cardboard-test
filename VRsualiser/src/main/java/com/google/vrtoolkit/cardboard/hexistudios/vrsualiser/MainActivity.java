/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vrtoolkit.cardboard.hexistudios.vrsualiser;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.google.vrtoolkit.cardboard.hexistudios.vrsualiser.render_items.Cube;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {
  private static final String TAG = "MainActivity";

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100.0f;

  private static final float CAMERA_Z = 0.01f;

  private static final float YAW_LIMIT = 0.12f;
  private static final float PITCH_LIMIT = 0.12f;

  // We keep the light always position just above the user.
  private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

  private static final float MIN_MODEL_DISTANCE = 3.0f;
  private static final float MAX_MODEL_DISTANCE = 7.0f;

  private FloatBuffer floorVertices;
  private FloatBuffer floorColors;
  private FloatBuffer floorNormals;

  private int renderProgram;

  private Scene scene;
  private float[] camera;
  private float[] headView;
  private float[] modelViewProjection;
  private float[] modelView;
  private float[] modelLocal;
  
  private float[] headRotation;

  private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
  private float floorDepth = 20f;

  private CardboardOverlayView overlayView;

  /**
   * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
   *
   * @param type The type of shader we will be creating.
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The shader object handler.
   */
  private int loadGLShader(int type, int resId) {
    String code = readRawTextFile(resId);
    int shader = GLES20.glCreateShader(type);
    GLES20.glShaderSource(shader, code);
    GLES20.glCompileShader(shader);

    // Get the compilation status.
    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    // If the compilation failed, delete the shader.
    if (compileStatus[0] == 0) {
      Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
      GLES20.glDeleteShader(shader);
      shader = 0;
    }

    if (shader == 0) {
      throw new RuntimeException("Error creating shader.");
    }

    return shader;
  }

  /**
   * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
   *
   * @param label Label to report in case of error.
   */
  private static void checkGLError(String label) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      Log.e(TAG, label + ": glError " + error);
      throw new RuntimeException(label + ": glError " + error);
    }
  }

  /**
   * Sets the view to our CardboardView and initializes the transformation matrices we will use
   * to render our scene.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.common_ui);
    CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
    cardboardView.setRestoreGLStateEnabled(false);
    cardboardView.setRenderer(this);
    setCardboardView(cardboardView);

    //Init vars.
    camera = new float[16];
    modelViewProjection = new float[16];
    modelView = new float[16];
    modelLocal = new float[16];
    headRotation = new float[4];
    headView = new float[16];

    overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
    overlayView.show3DToast("3D Toast example.");
  }

  @Override
  public void onSurfaceChanged(int width, int height) {
    Log.i(TAG, "onSurfaceChanged");
  }

  /**
   * Creates the buffers we use to store information about the 3D world.
   *
   * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
   * Hence we use ByteBuffers.
   *
   * @param config The EGL configuration used when creating the surface.
   */
  @Override
  public void onSurfaceCreated(EGLConfig config) {
    Log.i(TAG, "onSurfaceCreated");
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

    // make a floor
    ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
    bbFloorVertices.order(ByteOrder.nativeOrder());
    floorVertices = bbFloorVertices.asFloatBuffer();
    floorVertices.put(WorldLayoutData.FLOOR_COORDS);
    floorVertices.position(0);

    ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
    bbFloorNormals.order(ByteOrder.nativeOrder());
    floorNormals = bbFloorNormals.asFloatBuffer();
    floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
    floorNormals.position(0);

    ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
    bbFloorColors.order(ByteOrder.nativeOrder());
    floorColors = bbFloorColors.asFloatBuffer();
    floorColors.put(WorldLayoutData.FLOOR_COLORS);
    floorColors.position(0);

    int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
    int fragShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
    int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);


    renderProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(renderProgram, vertexShader);
    GLES20.glAttachShader(renderProgram, fragShader);
    GLES20.glLinkProgram(renderProgram);
    GLES20.glUseProgram(renderProgram);

    checkGLError("Render program init");

    //Set up object to cut down constructor size.
    RenderParams renderParams = new RenderParams(
        GLES20.glGetUniformLocation(renderProgram, "u_LightPos"),
        GLES20.glGetUniformLocation(renderProgram, "u_Model"),
        GLES20.glGetUniformLocation(renderProgram, "u_MVMatrix"),
        GLES20.glGetUniformLocation(renderProgram, "u_MVP"),
        GLES20.glGetAttribLocation(renderProgram, "a_Position"),
        GLES20.glGetAttribLocation(renderProgram, "a_Normal"),
        GLES20.glGetAttribLocation(renderProgram, "a_Color"));


    scene = new Scene(renderParams);  //Init scene.

    GLES20.glEnableVertexAttribArray(scene.renderParams.vertexParam);
    GLES20.glEnableVertexAttribArray(scene.renderParams.normalParam);
    GLES20.glEnableVertexAttribArray(scene.renderParams.colourParam);

    checkGLError("Render program params");

    Matrix.setIdentityM(modelLocal, 0);
    Matrix.translateM(modelLocal, 0, 0, -floorDepth, 0); // Floor appears below user.

    for (int i = 0; i < 10; i++) {
      scene.add(new Cube(2, 2, 2, new float[]{0, 2*i, -objectDistance * 10f},  scene.renderParams));
    }
    checkGLError("cube init");

    checkGLError("onSurfaceCreated");
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onRendererShutdown() {
    Log.i(TAG, "onRendererShutdown");
  }



  /**
   * Converts a raw text file into a string.
   *
   * @param resId The resource ID of the raw text file about to be turned into a shader.
   * @return The context of the text file, or null in case of error.
   */
  private String readRawTextFile(int resId) {
    InputStream inputStream = getResources().openRawResource(resId);
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
      return sb.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Prepares OpenGL ES before we draw a frame.
   *
   * @param headTransform The head transformation in the new frame.
   */
  @Override
  public void onNewFrame(HeadTransform headTransform) {

    // Build the camera matrix and apply it to the ModelView.
    Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

    headTransform.getHeadView(headView, 0);

    // Update the 3d audio engine with the most recent head rotation.
    headTransform.getQuaternion(headRotation, 0);

    checkGLError("onReadyToDraw");
  }

  /**
   * Draws a frame for an eye.
   *
   * @param eye The eye to render. Includes all required transformations.
   */
  @Override
  public void onDrawEye(Eye eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    checkGLError("colourParam");

    // Apply the eye transformation to the camera.
    Matrix.multiplyMM(scene.view, 0, eye.getEyeView(), 0, camera, 0);

    // Set the position of the light
    Matrix.multiplyMV(scene.lightPosInEyeSpace, 0, scene.view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);


    // Build the ModelView and ModelViewProjection matrices
    // for calculating cube position and light.
    scene.perspective = eye.getPerspective(Z_NEAR, Z_FAR);
    Matrix.multiplyMM(modelViewProjection, 0, scene.perspective, 0, modelView, 0);

    // Set modelView for the floor, so we draw floor in the correct location
    Matrix.multiplyMM(modelView, 0, scene.view, 0, modelLocal, 0);
    Matrix.multiplyMM(modelViewProjection, 0, scene.perspective, 0, modelView, 0);

    GLES20.glUseProgram(renderProgram);
    drawFloor();
    scene.redraw(); //Render the scene.
  }

  @Override
  public void onFinishFrame(Viewport viewport) {}

  /**
   * Draw the floor.
   *
   * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
   * position of the light, so if we rewrite our code to draw the floor first, the lighting might
   * look strange.
   */
  public void drawFloor() {

    // Set ModelView, MVP, position, normals, and colour.
    GLES20.glUniform3fv(scene.renderParams.lightPosParam, 1, scene.lightPosInEyeSpace, 0);
    checkGLError("LightPos");
    GLES20.glUniformMatrix4fv(scene.renderParams.modelLocalParam, 1, false, modelLocal, 0);
    checkGLError("LocalPos");
    GLES20.glUniformMatrix4fv(scene.renderParams.modelViewParam, 1, false, modelView, 0);
    checkGLError("VIewPos");
    GLES20.glUniformMatrix4fv(scene.renderParams.modelViewProjectionParam, 1, false, modelViewProjection, 0);
    checkGLError("ViewProjPos");
    GLES20.glVertexAttribPointer(scene.renderParams.vertexParam, 3, GLES20.GL_FLOAT, false, 0, floorVertices);
    checkGLError("Verts");
    GLES20.glVertexAttribPointer(scene.renderParams.normalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
    checkGLError("Normals");
    GLES20.glVertexAttribPointer(scene.renderParams.colourParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);
    checkGLError("Colour");

    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

    checkGLError("drawing floor");
  }

  /**
   * Called when the Cardboard trigger is pulled.
   */
  @Override
  public void onCardboardTrigger() {
    Log.i(TAG, "onCardboardTrigger");
  }


  /**
   * Check if user is looking at object by calculating where the object is in eye-space.
   *
   * @return true if the user is looking at the object.
   */
  private boolean isLookingAtObject() {
    float[] initVec = {0, 0, 0, 1.0f};
    float[] objPositionVec = new float[4];

    // Convert object space to camera space. Use the headView from onNewFrame.
    Matrix.multiplyMV(objPositionVec, 0, modelView, 0, initVec, 0);

    float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
    float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

    return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
  }
}