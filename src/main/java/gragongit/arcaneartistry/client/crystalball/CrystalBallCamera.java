package gragongit.arcaneartistry.client.crystalball;

import net.minecraft.util.Mth;

public final class CrystalBallCamera {

  @FunctionalInterface
  public interface FocusBounds {
    CrystalBallRenderer.WorldPosition clamp(double focusX, double focusY, double zoom);
  }

  private static final double FLIGHT_STAGGER = 0.3;

  private double minZoom = 0;
  private double maxZoom = Double.MAX_VALUE;
  private FocusBounds focusBounds = (x, y, zoom) -> new CrystalBallRenderer.WorldPosition(x, y);

  private double panX;
  private double panY;
  private double zoom = 1;

  private Flight flight;

  public void setZoomRange(double minZoom, double maxZoom) {
    this.minZoom = minZoom;
    this.maxZoom = maxZoom;
    setFocus(focusX(), focusY(), Math.clamp(zoom, minZoom, maxZoom));
    clampFocus();
  }

  public void setFocusBounds(FocusBounds focusBounds) {
    this.focusBounds = focusBounds;
    clampFocus();
  }

  private record Flight(double fromX, double fromY, double fromZoom, double toX, double toY, double toZoom, double stagger, long startNanos,
      long durationNanos) {
  }

  public void drag(double dxPixels, double dyPixels) {
    flight = null;
    panX += dxPixels;
    panY += dyPixels;
    clampFocus();
  }

  public void zoomAt(double relX, double relY, double factor) {
    flight = null;
    double newZoom = Math.clamp(zoom * factor, minZoom, maxZoom);
    double f = newZoom / zoom;
    panX = relX - (relX - panX) * f;
    panY = relY - (relY - panY) * f;
    zoom = newZoom;
    clampFocus();
  }

  private void clampFocus() {
    CrystalBallRenderer.WorldPosition clamped = focusBounds.clamp(focusX(), focusY(), zoom);
    setFocus(clamped.x(), clamped.y(), zoom);
  }

  public void jumpTo(double worldX, double worldY, double zoom) {
    flight = null;
    setFocus(worldX, worldY, zoom);
  }

  public boolean isFlying() {
    return flight != null;
  }

  public void flyTo(double worldX, double worldY, double targetZoom, float seconds, boolean staggered) {
    targetZoom = Math.clamp(targetZoom, minZoom, maxZoom);
    if (seconds <= 0) {
      flight = null;
      setFocus(worldX, worldY, targetZoom);
      return;
    }
    double stagger = staggered ? FLIGHT_STAGGER : 0;
    flight =
        new Flight(focusX(), focusY(), zoom, worldX, worldY, targetZoom, stagger, System.nanoTime(), (long) (seconds * 1_000_000_000L));
  }

  public void update() {
    if (flight == null) {
      return;
    }
    double t = Math.min(1, (System.nanoTime() - flight.startNanos()) / (double) flight.durationNanos());
    double leading = ease(t / (1 - flight.stagger()));
    double trailing = ease((t - flight.stagger()) / (1 - flight.stagger()));
    boolean zoomingIn = flight.toZoom() > flight.fromZoom();
    double ePos = zoomingIn ? leading : trailing;
    double eZoom = zoomingIn ? trailing : leading;
    double x = Mth.lerp(ePos, flight.fromX(), flight.toX());
    double y = Mth.lerp(ePos, flight.fromY(), flight.toY());
    double z = Math.exp(Mth.lerp(eZoom, Math.log(flight.fromZoom()), Math.log(flight.toZoom())));
    setFocus(x, y, z);
    if (t >= 1) {
      flight = null;
    }
  }

  private static double ease(double t) {
    return Mth.smoothstep((float) Math.clamp(t, 0, 1));
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
