package com.google.vrtoolkit.cardboard.hexistudios.vrsualiser.renderers;

import com.google.vrtoolkit.cardboard.hexistudios.vrsualiser.RenderParams;
import com.google.vrtoolkit.cardboard.hexistudios.vrsualiser.render_items.Triangle;

import java.util.ArrayList;

/**
 * Digital EQ-like bars.
 */
public class TestRenderer extends Renderer {
  ArrayList<Triangle> faces = new ArrayList<Triangle>();

  public TestRenderer(RenderParams renderParams) {
    super(renderParams);


    float size = 1;


    for (int x = (int) -size; x < size; x++) {
      for (int y = (int) -size; y < size; y++) {
        faces.add(new Triangle(   //Top1
            new float[]{x, size, y},
            new float[]{x + 1, size, y},
            new float[]{x + 1, size, y + 1},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x, size, y},
            new float[]{x, size, y + 1},
            new float[]{x + 1, size, y + 1},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x + 1, -size, y + 1},
            new float[]{x + 1, -size, y},
            new float[]{x, -size, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x + 1, -size, y + 1},
            new float[]{x, -size, y + 1},
            new float[]{x, -size, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{-size, x, y},
            new float[]{-size, x, y + 1},
            new float[]{-size, x + 1, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{-size, x + 1, y},
            new float[]{-size, x, y + 1},
            new float[]{-size, x + 1, y + 1},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{size, x, y},
            new float[]{size, x, y + 1},
            new float[]{size, x + 1, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{size, x + 1, y + 1},
            new float[]{size, x, y + 1},
            new float[]{size, x + 1, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x, y, -size},
            new float[]{x + 1, y, -size},
            new float[]{x, y + 1, -size},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x + 1, y + 1, -size},
            new float[]{x + 1, y, -size},
            new float[]{x, y + 1, -size},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x, y, size},
            new float[]{x + 1, y, size},
            new float[]{x, y + 1, size},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x + 1, y + 1, size},
            new float[]{x + 1, y, size},
            new float[]{x, y + 1, size},
            scene.renderParams));
      }
    }

    for (Triangle t : faces) {
      scene.add(t); //Add for rendering.
    }
  }

  /*

    */
    /*
        faces.add(new Triangle(   //Top1
            new float[]{x - 1, -size, y - 1},
            new float[]{x, -size, y - 1},
            new float[]{x, -size, y},
            scene.renderParams));

        faces.add(new Triangle(
            new float[]{x - 1, -size, y - 1},
            new float[]{x - 1, -size, y},
            new float[]{x, -size, y},
            scene.renderParams));

        bottom1 = new Triangle(
            new float[]{width, -height, depth},
            new float[]{width, -height, -depth},
            new float[]{-width, -height, -depth},
            scene.renderParams),

        bottom2 = new Triangle(
            new float[]{width, -height, depth},
            new float[]{-width, -height, depth},
            new float[]{-width, -height, -depth},
            scene.renderParams),

        left1 = new Triangle(
            new float[]{-width, -height, -depth},
            new float[]{-width, -height, depth},
            new float[]{-width, height, -depth},
            scene.renderParams),

        left2 = new Triangle(
            new float[]{-width, height, -depth},
            new float[]{-width, -height, depth},
            new float[]{-width, height, depth},
            scene.renderParams),

        right1 = new Triangle(
            new float[]{width, -height, -depth},
            new float[]{width, -height, depth},
            new float[]{width, height, -depth},
            scene.renderParams),

        right2 = new Triangle(
            new float[]{width, height, depth},
            new float[]{width, -height, depth},
            new float[]{width, height, -depth},
            scene.renderParams),

        front1 = new Triangle(
            new float[]{-width, -height, -depth},
            new float[]{width, -height, -depth},
            new float[]{-width, height, -depth},
            scene.renderParams),

        front2 = new Triangle(
            new float[]{width, height, -depth},
            new float[]{width, -height, -depth},
            new float[]{-width, height, -depth},
            scene.renderParams),

        back1 = new Triangle(
            new float[]{-width, -height, depth},
            new float[]{width, -height, depth},
            new float[]{-width, height, depth},
            scene.renderParams),

        back2 = new Triangle(
            new float[]{width, height, depth},
            new float[]{width, -height, depth},
            new float[]{-width, height, depth},
            scene.renderParams);*/


  public float[] spherify(float[] oldVerts) {
    float[] newVerts = new float[oldVerts.length];

    newVerts[0] = (float) (oldVerts[0] * Math.sqrt(1.0 - (oldVerts[1] * oldVerts[1] / 2.0) - (oldVerts[2] * oldVerts[2] / 2.0)
        + (oldVerts[1] * oldVerts[1] * oldVerts[2] * oldVerts[2] / 3.0)));

    newVerts[1] = (float) (oldVerts[1] * Math.sqrt(1.0 - (oldVerts[2] * oldVerts[2] / 2.0) - (oldVerts[0] * oldVerts[0] / 2.0)
        + (oldVerts[2] * oldVerts[2] * oldVerts[0] * oldVerts[0] / 3.0)));

    newVerts[2] = (float) (oldVerts[2] * Math.sqrt(1.0 - (oldVerts[0] * oldVerts[0] / 2.0) - (oldVerts[1] * oldVerts[1] / 2.0)
        + (oldVerts[0] * oldVerts[0] * oldVerts[1] * oldVerts[1] / 3.0)));

    return newVerts;
  }


  @Override
  public void updateVisualiserWave(byte[] waveBytes) {
    super.updateVisualiserWave(waveBytes);
  }

  @Override
  public void updateVisualiserFft(byte[] fftBytes) {
    super.updateVisualiserFft(fftBytes);

    int divisions = faces.size();
    for (int i = 0; i < divisions; i++) {

      Triangle face = faces.get(i);

      int index = (int)Math.floor(fftBytes.length/divisions);

      byte rfk = fftBytes[index * i];
      byte ifk = fftBytes[index * i + 1];
      float magnitude = (rfk * rfk + ifk * ifk);
      float dbValue = (float) (Math.log10(magnitude)/2);
      System.out.println(dbValue);
      if (dbValue < 0) {dbValue = 0.5f;} else if (dbValue > 1) { dbValue = 1;}

      float colours[] = new float[] {
          (float)0f, (float)dbValue, (float)dbValue, 1.0f,
          (float)0f, (float)dbValue, (float)dbValue, 1.0f,
          (float)0f, (float)dbValue, (float)dbValue, 1.0f,
          (float)0f, (float)dbValue, (float)dbValue, 1.0f
      };

      face.setColors(colours);
    }
  }






  @Override
  public void render() {
    super.render(); //Draw all visible objects in the object list.
  }
}

/*
  public void spherify(Triangle t) {
    float[] newVerts = new float[t.getVertices().length];
    float[] oldVerts = t.getVertices();

    //Loop through each vert (divide by three because float[] = x, y, z of each vert in series.
    for (int i = 0; i < newVerts.length / 3; i++) {
      newVerts[i * 3] = (float) (oldVerts[i * 3] * Math.sqrt(1.0 - (oldVerts[i * 3 + 1] * oldVerts[i * 3 + 1] / 2.0) - (oldVerts[i * 3 + 2] * oldVerts[i * 3 + 2] / 2.0)
          + (oldVerts[i * 3 + 1] * oldVerts[i * 3 + 1] * oldVerts[i * 3 + 2] * oldVerts[i * 3 + 2] / 3.0)));

      newVerts[i * 3 + 1] = (float) (oldVerts[i * 3 + 1] * Math.sqrt(1.0 - (oldVerts[i * 3 + 2] * oldVerts[i * 3 + 2] / 2.0) - (oldVerts[i * 3] * oldVerts[i * 3] / 2.0)
          + (oldVerts[i * 3 + 2] * oldVerts[i * 3 + 2] * oldVerts[i * 3] * oldVerts[i * 3] / 3.0)));

      newVerts[i * 3 + 2] = (float) (oldVerts[i * 3 + 2] * Math.sqrt(1.0 - (oldVerts[i * 3] * oldVerts[i * 3] / 2.0) - (oldVerts[i * 3 + 1] * oldVerts[i * 3 + 1] / 2.0)
          + (oldVerts[i * 3] * oldVerts[i * 3] * oldVerts[i * 3 + 1] * oldVerts[i * 3 + 1] / 3.0)));
    }

    t.setVertices(newVerts);
  }

 */