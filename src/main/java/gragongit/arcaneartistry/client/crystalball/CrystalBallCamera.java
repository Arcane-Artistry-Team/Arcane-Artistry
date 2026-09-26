package gragongit.arcaneartistry.client.crystalball;

import net.minecraft.util.Mth;

public final class CrystalBallCamera {
  public static final float MIN_ZOOM = 0.4f;
  public static final float MAX_ZOOM = 600;

  private float panX;
  private float panY;
  private float zoom = 1;

  private Flight flight;

  private record Flight(float fromX, float fromY, float fromZoom, float toX, float toY, float toZoom, long startNanos, long durationNanos) {
  }

  public void drag(float dxPixels, float dyPixels) {
    flight = null;
    panX += dxPixels;
    panY += dyPixels;
  }

  public void zoomAt(float relX, float relY, float factor) {
    flight = null;
    float newZoom = Math.clamp(zoom * factor, MIN_ZOOM, MAX_ZOOM);
    float f = newZoom / zoom;
    panX = relX - (relX - panX) * f;
    panY = relY - (relY - panY) * f;
    zoom = newZoom;
  }

  public void reset() {
    flight = null;
    panX = 0;
    panY = 0;
    zoom = 1;
  }

  public void flyTo(float worldX, float worldY, float targetZoom, float seconds) {
    targetZoom = Math.clamp(targetZoom, MIN_ZOOM, MAX_ZOOM);
    if (seconds <= 0) {
      flight = null;
      setFocus(worldX, worldY, targetZoom);
      return;
    }
    flight = new Flight(focusX(), focusY(), zoom, worldX, worldY, targetZoom, System.nanoTime(), (long) (seconds * 1_000_000_000L));
  }

  public void update() {
    if (flight == null) {
      return;
    }
    float t = Math.min(1, (System.nanoTime() - flight.startNanos()) / (float) flight.durationNanos());
    float e = t * t * (3 - 2 * t);
    float x = Mth.lerp(e, flight.fromX(), flight.toX());
    float y = Mth.lerp(e, flight.fromY(), flight.toY());
    float z = (float) Math.exp(Mth.lerp(e, Math.log(flight.fromZoom()), Math.log(flight.toZoom())));
    setFocus(x, y, z);
    if (t >= 1) {
      flight = null;
    }
  }

  private float focusX() {
    return -panX / zoom;
  }

  private float focusY() {
    return -panY / zoom;
  }

  private void setFocus(float worldX, float worldY, float zoom) {
    this.zoom = zoom;
    this.panX = -worldX * zoom;
    this.panY = -worldY * zoom;
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
