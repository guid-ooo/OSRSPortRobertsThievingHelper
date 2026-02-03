package com.prthievinghelper;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.TileObject;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.ChatMessageType;
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
        if (!config.enableStallHighlighting())
        {
            return null;
        }

        // Get stalls and their remaining thieves counts
        TileObject primaryStall = plugin.getPrimaryStallToHighlight();
        Integer primaryThieves = plugin.getPossibleThievesForDisplay(config.primaryStall());
        
        TileObject secondaryStall = plugin.getSecondaryStallToHighlight();
        Integer secondaryThieves = plugin.getPossibleThievesForDisplay(config.secondaryStall());

        // Determine colors (use config switch color for switch highlight when counter hits limit)
        Color primaryColor = config.primaryHighlightColor();
        Color secondaryColor = config.secondaryHighlightColor();
        Color switchColor = config.switchHighlightColor();
        
        // Check if we should switch away from either stall (they hit 4 completed thieves)
        boolean shouldSwitchAwayFromPrimary = plugin.shouldSwitchAwayFrom(config.primaryStall());
        boolean shouldSwitchAwayFromSecondary = plugin.shouldSwitchAwayFrom(config.secondaryStall());
        
        // When primary hits 4 completed, highlight secondary with switch color (signal to switch TO secondary)
        // When secondary hits 4 completed, highlight primary with switch color (signal to switch TO primary)
        if (shouldSwitchAwayFromPrimary && secondaryStall != null)
        {
            secondaryColor = switchColor;
        }
        
        if (shouldSwitchAwayFromSecondary && primaryStall != null)
        {
            primaryColor = switchColor;
        }

        // Only highlight one stall at a time
        // Prefer showing the one you should switch to (white/switch color highlight)
        if (secondaryColor == switchColor && secondaryStall != null)
        {
            renderStallHighlight(graphics, secondaryStall, secondaryColor, secondaryThieves);
        }
        else if (primaryColor == switchColor && primaryStall != null)
        {
            renderStallHighlight(graphics, primaryStall, primaryColor, primaryThieves);
        }
        else if (primaryStall != null)
        {
            renderStallHighlight(graphics, primaryStall, primaryColor, primaryThieves);
        }
        else if (secondaryStall != null)
        {
            renderStallHighlight(graphics, secondaryStall, secondaryColor, secondaryThieves);
        }

        return null;
    }

    private void renderStallHighlight(Graphics2D graphics, TileObject object, Color highlightColor, Integer possibleThieves)
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
        
        // Draw number of possible thieves above the stall
        // -1 means "ready for switch but watched" - show highlight but no number
        if (possibleThieves != null && possibleThieves > 0)
        {
            LocalPoint localPoint = object.getLocalLocation();
            if (localPoint != null)
            {
                Point textLocation = Perspective.getCanvasTextLocation(
                    client,
                    graphics,
                    localPoint,
                    String.valueOf(possibleThieves),
                    200 // Z-offset to position text above the object
                );
                
                if (textLocation != null)
                {
                    // Draw text shadow for better visibility
                    graphics.setFont(new Font("Arial", Font.BOLD, 16));
                    graphics.setColor(Color.BLACK);
                    graphics.drawString(String.valueOf(possibleThieves), textLocation.getX() + 1, textLocation.getY() + 1);
                    
                    // Draw the actual text
                    graphics.setColor(highlightColor);
                    graphics.drawString(String.valueOf(possibleThieves), textLocation.getX(), textLocation.getY());
                }
            }
        }
    }
}
