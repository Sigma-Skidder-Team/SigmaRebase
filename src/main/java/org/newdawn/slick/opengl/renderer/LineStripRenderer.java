package org.newdawn.slick.opengl.renderer;

public interface LineStripRenderer {
    boolean applyGLLineFixes();

    void start();

    void end();

    void vertex(float x, float y);

    void color(float r, float g, float b, float a);

    void setWidth(float width);

    void setAntiAlias(boolean antialias);

    void setLineCaps(boolean caps);
}
