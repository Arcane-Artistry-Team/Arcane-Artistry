package gragongit.arcaneartistry.client.crystalball;

import net.minecraft.util.Mth;

public final class CrystalBallCamera {
  public static final double MIN_ZOOM = 0.4;

  private double maxZoom = Double.MAX_VALUE;

  private double panX;
  private double panY;
  private double zoom = 1;

  private Flight flight;

  public void setMaxZoom(double maxZoom) {
    this.maxZoom = maxZoom;
  }

  private record Flight(double fromX, double fromY, double fromZoom, double toX, double toY, double toZoom, long startNanos,
      long durationNanos) {
  }

  public void drag(double dxPixels, double dyPixels) {
    flight = null;
    panX += dxPixels;
    panY += dyPixels;
  }

  public void zoomAt(double relX, double relY, double factor) {
    flight = null;
    double newZoom = Math.clamp(zoom * factor, MIN_ZOOM, maxZoom);
    double f = newZoom / zoom;
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

  public void flyTo(double worldX, double worldY, double targetZoom, float seconds) {
    targetZoom = Math.clamp(targetZoom, MIN_ZOOM, maxZoom);
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
    double t = Math.min(1, (System.nanoTime() - flight.startNanos()) / (double) flight.durationNanos());
    double e = t * t * (3 - 2 * t);
    double x = Mth.lerp(e, flight.fromX(), flight.toX());
    double y = Mth.lerp(e, flight.fromY(), flight.toY());
    double z = Math.exp(Mth.lerp(e, Math.log(flight.fromZoom()), Math.log(flight.toZoom())));
    setFocus(x, y, z);
    if (t >= 1) {
      flight = null;
    }
  }

  public double focusX() {
    return -panX / zoom;
  }

  public double focusY() {
    return -panY / zoom;
  }

  private void setFocus(double worldX, double worldY, double zoom) {
    this.zoom = zoom;
    this.panX = -worldX * zoom;
    this.panY = -worldY * zoom;
  }

  public double panX() {
    return panX;
  }

  public double panY() {
    return panY;
  }

  public double zoom() {
    return zoom;
  }
}
