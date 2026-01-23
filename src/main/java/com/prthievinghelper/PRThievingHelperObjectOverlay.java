package com.prthievinghelper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.TileObject;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;

public class PRThievingHelperObjectOverlay extends Overlay
{
    private final Client client;
    private final PRThievingHelperPlugin plugin;
    private final PRThievingHelperConfig config;

    @Inject
    public PRThievingHelperObjectOverlay(Client client,
                                         PRThievingHelperPlugin plugin,
                                         PRThievingHelperConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableObjectHighlighting())
        {
            return null;
        }

        // Highlight primary stall with primary color
        TileObject primaryStall = plugin.getPrimaryStallToHighlight();
        if (primaryStall != null)
        {
            renderStallHighlight(graphics, primaryStall, config.primaryHighlightColor());
        }

        // Highlight secondary stall with secondary color
        TileObject secondaryStall = plugin.getSecondaryStallToHighlight();
        if (secondaryStall != null)
        {
            renderStallHighlight(graphics, secondaryStall, config.secondaryHighlightColor());
        }

        return null;
    }

    private void renderStallHighlight(Graphics2D graphics, TileObject object, Color highlightColor)
    {
        
        // Draw clickbox/outline
        Shape objectClickbox = object.getClickbox();
        if (objectClickbox != null)
        {
            // Draw fill
            graphics.setColor(new Color(highlightColor.getRed(), 
                                        highlightColor.getGreen(), 
                                        highlightColor.getBlue(), 
                                        20)); // Semi-transparent fill
            graphics.fill(objectClickbox);
            
            // Draw outline
            graphics.setColor(new Color(highlightColor.getRed(), 
                                        highlightColor.getGreen(), 
                                        highlightColor.getBlue(), 
                                        highlightColor.getAlpha()));
            graphics.setStroke(new BasicStroke(2));
            graphics.draw(objectClickbox);
        }
    }
}
