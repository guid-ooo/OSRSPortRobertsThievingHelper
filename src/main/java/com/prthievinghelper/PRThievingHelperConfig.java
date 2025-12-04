package com.prthievinghelper;

import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("example")
public interface PRThievingHelperConfig extends Config
{
	@ConfigSection(
			name = "Box Colors",
			description = "Color Settings for Visual Indicators",
			position = 0
	)
	String boxColors = "boxColors";

	@ConfigItem(
			keyName = "drawBoxes",
			name = "Draw Boxes",
			description = "Toggle to enable/disable drawing of stall boxes.",
			section = notifierSettings,
			position = 1
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
			position = 2
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
			position = 3
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
			position = 4
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
			position = 5
	)
	@Alpha
	default Color watchedStallBorderColor()
	{
		return new Color(255, 0, 0, 64);
	}

	@ConfigSection(
			name = "Notifier Settings",
			description = "Notifier Settings for unwatched Stalls",
			position = 6
	)
	String notifierSettings = "notifierSettings";

	@ConfigItem(
			keyName = "notifyForUnwatched",
			name = "Idle Notify For Unwatched",
			description = "Sends an OS notification for toggled unwatched stalls.",
			section = notifierSettings,
			position = 7
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
			position = 8
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
			position = 9
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
			position = 10
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
			position = 11
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
			position = 12
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
			position = 13
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
			position = 14
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
			position = 15
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
			position = 16
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
			position = 17
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
			position = 18
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
			position = 19
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
			position = 20
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
			position = 21
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
			position = 22
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
