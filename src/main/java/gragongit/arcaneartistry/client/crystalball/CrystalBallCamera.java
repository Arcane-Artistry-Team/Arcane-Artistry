package gragongit.arcaneartistry.client.crystalball;

public final class CrystalBallCamera {
  public static final float MIN_ZOOM = 0.4f;
  public static final float MAX_ZOOM = 600;

  private float panX;
  private float panY;
  private float zoom = 1;

  public void drag(float dxPixels, float dyPixels) {
    panX += dxPixels;
    panY += dyPixels;
  }

  public void zoomAt(float relX, float relY, float factor) {
    float newZoom = Math.clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
    float f = newZoom / zoom;
    panX = relX - (relX - panX) * f;
    panY = relY - (relY - panY) * f;
    zoom = newZoom;
  }

  public void reset() {
    panX = 0;
    panY = 0;
    zoom = 1;
  }

  public float panX() {
    return panX;
  }

  public float panY() {
    return panY;
  }

  public float zoom() {
    return zoom;
  }
}
