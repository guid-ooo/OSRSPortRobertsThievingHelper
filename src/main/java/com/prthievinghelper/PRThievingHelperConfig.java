package com.prthievinghelper;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("prthievinghelper")
public interface PRThievingHelperConfig extends Config
{
	enum StallSelection
	{
		CANNONBALL("Cannonball Stall"),
		VEG("Veg Stall"),
		ORE("Ore Stall"),
		FISH("Fish Stall"),
		SPICE("Spice Stall"),
		SILVER("Silver Stall"),
		GEM("Gem Stall"),
		FUR("Fur Stall"),
		SILK("Silk Stall"),
		NONE("None");

		private final String displayName;

		StallSelection(String displayName)
		{
			this.displayName = displayName;
		}

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	@ConfigSection(
			name = "Stall Highlighting",
			description = "Settings for Stall Highlighting",
			position = 0
	)
	String stallHighlighting = "stallHighlighting";

	@ConfigSection(
			name = "Box Colors",
			description = "Color Settings for Visual Indicators",
			position = 1
	)
	String boxColors = "boxColors";

	@ConfigItem(
			keyName = "enableStallHighlighting",
			name = "Enable Stall Highlighting",
			description = "Highlights stall objects white when they are safe to thieve from.",
			section = stallHighlighting,
			position = 1
	)
	default boolean enableStallHighlighting()
	{
		return false;
	}

	@ConfigItem(
			keyName = "primaryStall",
			name = "Primary Stall",
			description = "The primary stall to thieve from (4 cycles).",
			section = stallHighlighting,
			position = 2
	)
	default StallSelection primaryStall()
	{
		return StallSelection.CANNONBALL;
	}

	@ConfigItem(
			keyName = "secondaryStall",
			name = "Secondary Stall",
			description = "The secondary stall to thieve from (2 cycles).",
			section = stallHighlighting,
			position = 3
	)
	default StallSelection secondaryStall()
	{
		return StallSelection.ORE;
	}

	@ConfigItem(
			keyName = "primaryHighlightColor",
			name = "Primary Highlight Color",
			description = "The color to highlight the primary stall when safe.",
			section = stallHighlighting,
			position = 4
	)
	@Alpha
	default Color primaryHighlightColor()
	{
		return new Color(0, 255, 0, 255);
	}

	@ConfigItem(
			keyName = "secondaryHighlightColor",
			name = "Secondary Highlight Color",
			description = "The color to highlight the secondary stall when safe.",
			section = stallHighlighting,
			position = 5
	)
	@Alpha
	default Color secondaryHighlightColor()
	{
		return new Color(255, 0, 255, 255);
	}

	@ConfigItem(
			keyName = "highlightOnlyOneStall",
			name = "Highlight Only One Stall",
			description = "When enabled, only highlights one stall at a time (primary is preferred when both are safe).",
			section = stallHighlighting,
			position = 6
	)
	default boolean highlightOnlyOneStall()
	{
		return false;
	}

	@ConfigItem(
			keyName = "drawBoxes",
			name = "Draw Boxes",
			description = "Toggle to enable/disable drawing of stall boxes.",
			section = boxColors,
			position = 7
	)
	default boolean drawBoxes()
	{
		return true;
	}

	@ConfigItem(
			keyName = "unwatchedStallColor",
			name = "Unwatched Stall Color",
			description = "The Color of Unwatched Stalls",
			section = boxColors,
			position = 8
	)
	@Alpha
	default Color unwatchedStallColor()
	{
		return new Color(0, 255, 0, 32);
	}

	@ConfigItem(
			keyName = "unwatchedStallBorderColor",
			name = "Unwatched Stall Border Color",
			description = "The Border Color of Unwatched Stalls",
			section = boxColors,
			position = 9
	)
	@Alpha
	default Color unwatchedStallBorderColor()
	{
		return new Color(0, 0, 0, 128);
	}

	@ConfigItem(
			keyName = "watchedStallColor",
			name = "Watched Stall Color",
			description = "The Color of Watched Stalls",
			section = boxColors,
			position = 10
	)
	@Alpha
	default Color watchedStallColor()
	{
		return new Color(255, 0, 0, 32);
	}

	@ConfigItem(
			keyName = "watchedStallBorderColor",
			name = "Watched Stall Border Color",
			description = "The Border Color of Watched Stalls",
			section = boxColors,
			position = 11
	)
	@Alpha
	default Color watchedStallBorderColor()
	{
		return new Color(255, 0, 0, 64);
	}

	@ConfigSection(
			name = "Notifier Settings",
			description = "Notifier Settings for unwatched Stalls",
			position = 12
	)
	String notifierSettings = "notifierSettings";

	@ConfigItem(
			keyName = "notifyForUnwatched",
			name = "Idle Notify For Unwatched",
			description = "Sends an OS notification for toggled unwatched stalls.",
			section = notifierSettings,
			position = 13
	)
	default boolean notifyForUnwatched()
	{
		return false;
	}

	@ConfigItem(
			keyName = "flashForUnwatched",
			name = "Flash For Unwatched",
			description = "Flashes the Screen for toggled unwatched stalls.",
			section = notifierSettings,
			position = 14
	)
	default boolean flashForUnwatched()
	{
		return false;
	}

	@ConfigItem(
			keyName = "soundForUnwatched",
			name = "Sound For Unwatched",
			description = "Plays a sound for the toggled unwatched stalls.",
			section = notifierSettings,
			position = 15
	)
	default boolean soundForUnwatched()
	{
		return false;
	}

	@ConfigItem(
			keyName = "soundForWatched",
			name = "Sound For Watched",
			description = "Plays a sound for the toggled watched stalls.",
			section = notifierSettings,
			position = 16
	)
	default boolean soundForWatched()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifierFlashColor",
			name = "Screen Flash Color",
			description = "The Screen Flash Color",
			section = notifierSettings,
			position = 17
	)
	@Alpha
	default Color notifierFlashColor()
	{
		return new Color(0, 255, 0, 64);
	}

	@ConfigItem(
			keyName = "notifierFlashSpeed",
			name = "Screen Flash Speed",
			description = "The Screen Flash Speed",
			section = notifierSettings,
			position = 18
	)
	default double notifierFlashSpeed()
	{
		return 0.05f;
	}

	@ConfigItem(
			keyName = "notifierFlashStrength",
			name = "Screen Flash Strength",
			description = "The Screen Flash Color Strength",
			section = notifierSettings,
			position = 19
	)
	@Range(
			min = 1,
			max = 255
	)
	default int notifierFlashStrength()
	{
		return 150;
	}

	@ConfigItem(
			keyName = "notifyForFur",
			name = "[ Fur Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Fur Stalls.",
			section = notifierSettings,
			position = 20
	)
	default boolean notifyForFur()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForSilk",
			name = "[ Silk Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Silk Stalls.",
			section = notifierSettings,
			position = 21
	)
	default boolean notifyForSilk()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForGem",
			name = "[ Gem Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Gem Stalls.",
			section = notifierSettings,
			position = 22
	)
	default boolean notifyForGem()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForCannon",
			name = "[ Cannon Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Cannon Stalls.",
			section = notifierSettings,
			position = 23
	)
	default boolean notifyForCannon()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForFish",
			name = "[ Fish Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Fish Stalls.",
			section = notifierSettings,
			position = 24
	)
	default boolean notifyForFish()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForOre",
			name = "[ Ore Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Ore Stalls.",
			section = notifierSettings,
			position = 25
	)
	default boolean notifyForOre()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForSpice",
			name = "[ Spice Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Spice Stalls.",
			section = notifierSettings,
			position = 26
	)
	default boolean notifyForSpice()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForVeg",
			name = "[ Veg Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Veg Stalls.",
			section = notifierSettings,
			position = 27
	)
	default boolean notifyForVeg()
	{
		return false;
	}

	@ConfigItem(
			keyName = "notifyForSilver",
			name = "[ Silver Stall ] Notify",
			description = "Toggles the Notification/Flash/Sound for unwatched Silver Stalls.",
			section = notifierSettings,
			position = 28
	)
	default boolean notifyForSilver()
	{
		return false;
	}

	// stall positions:
	// 58102[Fur stall] - WorldPoint(x=1870, y=3291, plane=0)
	// 58101[Silk stall] - WorldPoint(x=1870, y=3294, plane=0)
	// 58106[Gem stall] - WorldPoint(x=1869, y=3288, plane=0)
	// 58108[Cannonball stall] - WorldPoint(x=1867, y=3296, plane=0)
	// 58103[Fish stall] - WorldPoint(x=1861, y=3291, plane=0)
	// 58107[Ore stall] - WorldPoint(x=1861, y=3294, plane=0)
	// 58105[Spice stall] - WorldPoint(x=1863, y=3288, plane=0)
	// 58100[Veg stall] - WorldPoint(x=1864, y=3296, plane=0)
	// 58104[Silver stall] - WorldPoint(x=1866, y=3288, plane=0)

	// guard watch positions:
	// fur stall = 1869, 3292
	// fish stall = 1863, 3292
	// ore stall = 1863, 3294
	// veg stall = 1865, 3295
	// cannon stall = 1867, 3295
	// silk stall = 1869, 3295
	// gem stall = 1869, 3290
	// silver stall = 1866, 3290
	// spice stall = 1864, 3290
}
