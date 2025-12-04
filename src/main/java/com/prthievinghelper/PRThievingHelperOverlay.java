package com.prthievinghelper;

import com.prthievinghelper.PRThievingHelperPlugin.StallTypes;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PRThievingHelperOverlay extends Overlay
{
    private final Client client;
    private final PRThievingHelperPlugin plugin;
    private final PRThievingHelperConfig config;

    private final Map<PRThievingHelperPlugin.StallTypes, Color> stallColors = new HashMap<>();

    @Inject
    public PRThievingHelperOverlay(Client client,
                                   PRThievingHelperPlugin plugin,
                                   PRThievingHelperConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(config.drawBoxes())
        {
            stallColors.put(StallTypes.FUR,
                    (plugin.watching.get(StallTypes.FUR)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.SILK,
                    (plugin.watching.get(StallTypes.SILK)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.GEM,
                    (plugin.watching.get(StallTypes.GEM)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.CANNON,
                    (plugin.watching.get(StallTypes.CANNON)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.FISH,
                    (plugin.watching.get(StallTypes.FISH)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.ORE,
                    (plugin.watching.get(StallTypes.ORE)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.SPICE,
                    (plugin.watching.get(StallTypes.SPICE)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.VEG,
                    (plugin.watching.get(StallTypes.VEG)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());
            stallColors.put(StallTypes.SILVER,
                    (plugin.watching.get(StallTypes.SILVER)) ?
                            config.watchedStallColor() : config.unwatchedStallColor());

            renderBox(graphics, plugin.stallPositions.get(StallTypes.FUR), 1, 2,
                    stallColors.get(StallTypes.FUR), plugin.watching.get(StallTypes.FUR));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.SILK), 1, 2,
                    stallColors.get(StallTypes.SILK), plugin.watching.get(StallTypes.SILK));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.GEM), 1, 2,
                    stallColors.get(StallTypes.GEM), plugin.watching.get(StallTypes.GEM));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.CANNON), 1, 2,
                    stallColors.get(StallTypes.CANNON), plugin.watching.get(StallTypes.CANNON));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.FISH), 1, 2,
                    stallColors.get(StallTypes.FISH), plugin.watching.get(StallTypes.FISH));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.ORE), 1, 2,
                    stallColors.get(StallTypes.ORE), plugin.watching.get(StallTypes.ORE));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.SPICE), 1, 2,
                    stallColors.get(StallTypes.SPICE), plugin.watching.get(StallTypes.SPICE));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.VEG), 1, 2,
                    stallColors.get(StallTypes.VEG), plugin.watching.get(StallTypes.VEG));
            renderBox(graphics, plugin.stallPositions.get(StallTypes.SILVER), 1, 2,
                    stallColors.get(StallTypes.SILVER), plugin.watching.get(StallTypes.SILVER));
        }

        float alpha = plugin.getFlashAlpha();
        if (alpha >= 0f)
        {
            Color flashColor = new Color(config.notifierFlashColor().getRed(),
                    config.notifierFlashColor().getGreen(),
                    config.notifierFlashColor().getBlue(),
                    (int)(alpha * config.notifierFlashStrength()));
            graphics.setColor(flashColor);
            graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        }

        return null;
    }

    private void renderBox(Graphics2D graphics, WorldPoint point, int width, int height,
                           Color color, boolean watched)
    {
        if (point == null) { return; }

        // Get the corners of the range square
        WorldPoint topLeft = new WorldPoint(
                point.getX() - width,
                point.getY() + height,
                point.getPlane()
        );
        WorldPoint topRight = new WorldPoint(
                point.getX() + width,
                point.getY() + height,
                point.getPlane()
        );
        WorldPoint bottomLeft = new WorldPoint(
                point.getX() - width,
                point.getY() - height,
                point.getPlane()
        );
        WorldPoint bottomRight = new WorldPoint(
                point.getX() + width,
                point.getY() - height,
                point.getPlane()
        );

        // Convert to local points
        LocalPoint topLeftLocal = LocalPoint.fromWorld(client, topLeft);
        LocalPoint topRightLocal = LocalPoint.fromWorld(client, topRight);
        LocalPoint bottomLeftLocal = LocalPoint.fromWorld(client, bottomLeft);
        LocalPoint bottomRightLocal = LocalPoint.fromWorld(client, bottomRight);

        if (topLeftLocal == null
                || topRightLocal == null
                || bottomLeftLocal == null
                || bottomRightLocal == null)
        {
            return;
        }

        renderSquareFromPoints(graphics,
                topLeftLocal, topRightLocal, bottomLeftLocal, bottomRightLocal,
                color, watched ? config.watchedStallBorderColor() : config.unwatchedStallBorderColor());
    }

    private void renderSquareFromPoints(Graphics2D graphics,
                                        LocalPoint topLeft, LocalPoint topRight,
                                        LocalPoint bottomLeft, LocalPoint bottomRight,
                                        Color color, Color strokeColor)
    {
        Polygon topLeftPoly = Perspective.getCanvasTilePoly(client, topLeft);
        Polygon topRightPoly = Perspective.getCanvasTilePoly(client, topRight);
        Polygon bottomLeftPoly = Perspective.getCanvasTilePoly(client, bottomLeft);
        Polygon bottomRightPoly = Perspective.getCanvasTilePoly(client, bottomRight);

        if (topLeftPoly == null
                || topRightPoly == null
                || bottomLeftPoly == null
                || bottomRightPoly == null)
        {
            return;
        }

        Polygon square = new Polygon();
        square.addPoint(topLeftPoly.xpoints[0], topLeftPoly.ypoints[0]);
        square.addPoint(topRightPoly.xpoints[1], topRightPoly.ypoints[1]);
        square.addPoint(bottomRightPoly.xpoints[2], bottomRightPoly.ypoints[2]);
        square.addPoint(bottomLeftPoly.xpoints[3], bottomLeftPoly.ypoints[3]);

        graphics.setColor(color);
        graphics.fillPolygon(square);

        graphics.setColor(strokeColor);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawPolygon(square);
    }
}
