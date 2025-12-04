package com.prthievinghelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.*;

@Slf4j
@PluginDescriptor(
	name = "Port Roberts Thieving Helper"
)
public class PRThievingHelperPlugin extends Plugin
{
	public enum StallTypes {
		FUR, SILK, GEM, CANNON, FISH, ORE, SPICE, VEG, SILVER
	}

	public Map<StallTypes, Boolean> watching = new HashMap<>();

	public final Map<StallTypes, WorldPoint> stallPositions = Map.of(
			StallTypes.FUR, new WorldPoint(1870, 3292, 0),
			StallTypes.SILK, new WorldPoint(1870, 3295, 0),
			StallTypes.GEM, new WorldPoint(1869, 3289, 0),
			StallTypes.CANNON, new WorldPoint(1867, 3296, 0),
			StallTypes.FISH, new WorldPoint(1861, 3292, 0),
			StallTypes.ORE, new WorldPoint(1861, 3295, 0),
			StallTypes.SPICE, new WorldPoint(1863, 3289, 0),
			StallTypes.VEG, new WorldPoint(1864, 3296, 0),
			StallTypes.SILVER, new WorldPoint(1866, 3289, 0)
	);

	private static final List<WorldPoint> furWatchPoints =
			Arrays.asList(
					new WorldPoint(1869, 3292, 0),
					new WorldPoint(1869, 3293, 0),
					new WorldPoint(1869, 3294, 0)
					);
	private static final List<WorldPoint> silkWatchPoints =
			Arrays.asList(
					new WorldPoint(1869, 3295, 0),
					new WorldPoint(1868, 3295, 0)
			);
	private static final List<WorldPoint> gemWatchPoints =
			Arrays.asList(
					new WorldPoint(1869, 3290, 0),
					new WorldPoint(1869, 3291, 0)
			);
	private static final List<WorldPoint> cannonWatchPoints =
			Arrays.asList(
					new WorldPoint(1867, 3295, 0),
					new WorldPoint(1866, 3295, 0)
			);
	private static final List<WorldPoint> fishWatchPoints =
			Arrays.asList(
					new WorldPoint(1863, 3292, 0),
					new WorldPoint(1863, 3291, 0)
			);
	private static final List<WorldPoint> oreWatchPoints =
			Arrays.asList(
					new WorldPoint(1863, 3294, 0),
					new WorldPoint(1863, 3293, 0)
			);
	private static final List<WorldPoint> spiceWatchPoints =
			Arrays.asList(
					new WorldPoint(1864, 3290, 0),
					new WorldPoint(1865, 3290, 0)
			);
	private static final List<WorldPoint> vegWatchPoints =
			Arrays.asList(
					new WorldPoint(1865, 3295, 0),
					new WorldPoint(1864, 3295, 0)
			);
	private static final List<WorldPoint> silverWatchPoints =
			Arrays.asList(
					new WorldPoint(1866, 3290, 0),
					new WorldPoint(1867, 3290, 0),
					new WorldPoint(1868, 3290, 0)
			);

	private static final Map<StallTypes, List<WorldPoint>> stallWatchPositions = Map.of(
			StallTypes.FUR, furWatchPoints,
			StallTypes.SILK, silkWatchPoints,
			StallTypes.GEM, gemWatchPoints,
			StallTypes.CANNON, cannonWatchPoints,
			StallTypes.FISH, fishWatchPoints,
			StallTypes.ORE, oreWatchPoints,
			StallTypes.SPICE, spiceWatchPoints,
			StallTypes.VEG, vegWatchPoints,
			StallTypes.SILVER, silverWatchPoints
	);

	private static final String GUARD_NAME = "Market Guard";
	private static final Set<Integer> GUARD_IDS = Set.of(
			14881, 14882, 14883
	);
	private static final int SOUND_ID_UNWATCHED = 8410;
	private static final int SOUND_ID_WATCHED = 3814;

	private static final Map<StallTypes, Boolean> notifiers = new HashMap<>();
	private static final Map<StallTypes, Boolean> watchNotifiers = new HashMap<>();
	private static final HashSet<Integer> activeGuards = new HashSet<>();
	private static final List<NPC> guards = new ArrayList<>();

	@Inject
	private Client client;

	@Inject
	private PRThievingHelperConfig config;

	@Inject
	public OverlayManager overlayManager;

	@Inject
	private PRThievingHelperOverlay overlay;

	@Inject
	private Notifier notifier;

	private float flashAlpha = 0f;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		for (StallTypes stall : StallTypes.values()) {
			watching.put(stall, false);
			notifiers.put(stall, false);
			watchNotifiers.put(stall, false);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		watching.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if(!isValidGuard(npc))
			return;

		int npcId = npc.getId();
		if(activeGuards.contains(npcId))
			return;

		activeGuards.add(npcId);
		guards.add(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if(!isValidGuard(npc))
			return;

		int npcId = npc.getId();

		if(activeGuards.contains(npcId))
		{
			activeGuards.remove(npcId);
			guards.remove(npc);
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		//System.out.println(client.getSelectedSceneTile().getWorldLocation());

		watching.put(StallTypes.FUR, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.FUR)));
		watching.put(StallTypes.SILK, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.SILK)));
		watching.put(StallTypes.GEM, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.GEM)));
		watching.put(StallTypes.CANNON, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.CANNON)));
		watching.put(StallTypes.FISH, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.FISH)));
		watching.put(StallTypes.ORE, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.ORE)));
		watching.put(StallTypes.SPICE, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.SPICE)));
		watching.put(StallTypes.VEG, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.VEG)));
		watching.put(StallTypes.SILVER, isAnyGuardAtPosition(stallWatchPositions.get(StallTypes.SILVER)));

		if (flashAlpha > 0f)
		{
			flashAlpha -= (float) config.notifierFlashSpeed();
			if (flashAlpha < 0f)
			{
				flashAlpha = 0f;
			}
		}

		if(config.notifyForFur())
		{
			checkNotificationTrigger(StallTypes.GEM, StallTypes.FUR);
		}
		if(config.notifyForSilk())
		{
			checkNotificationTrigger(StallTypes.FUR, StallTypes.SILK);
		}
		if(config.notifyForGem())
		{
			checkNotificationTrigger(StallTypes.SILVER, StallTypes.GEM);
		}
		if(config.notifyForCannon())
		{
			checkNotificationTrigger(StallTypes.SILK, StallTypes.CANNON);
		}
		if(config.notifyForFish())
		{
			checkNotificationTrigger(StallTypes.ORE, StallTypes.FISH);
		}
		if(config.notifyForOre())
		{
			checkNotificationTrigger(StallTypes.VEG, StallTypes.ORE);
		}
		if(config.notifyForSpice())
		{
			checkNotificationTrigger(StallTypes.FISH, StallTypes.SPICE);
		}
		if(config.notifyForSilver())
		{
			checkNotificationTrigger(StallTypes.SPICE, StallTypes.SILVER);
		}
		if(config.notifyForVeg())
		{
			checkNotificationTrigger(StallTypes.CANNON, StallTypes.VEG);
		}
	}

	public float getFlashAlpha()
	{
		return flashAlpha;
	}

	private boolean isValidGuard(NPC npc)
	{
		String npcName = npc.getName();
		if(npcName == null)
			return false;

		int npcId = npc.getId();
        return npcName.equals(GUARD_NAME) && GUARD_IDS.contains(npcId);
    }

	private boolean isAnyGuardAtPosition(List<WorldPoint> wps)
	{
		for(NPC npc: guards)
		{
			WorldPoint nwp = npc.getWorldLocation();
			int x = nwp.getX();
			int y = nwp.getY();

			for(WorldPoint wp: wps)
			{
				if(x == wp.getX() && y == wp.getY())
				{
					return true;
				}
			}
		}

		return false;
	}

	private void triggerFlash()
	{
		flashAlpha = 1f; // fully opaque at start
	}

	private void checkNotificationTrigger(StallTypes triggerStall, StallTypes stallType)
	{
		// since each stall is ordered, check whether the next stall is watched
		// if it is:
		// do any notifications for unwatched status of the previous stall

		// if the trigger stall is activated, the previous stall is unwatched
		if(watching.get(triggerStall) && !notifiers.get(stallType))
		{
			// if the flash config is on, do the flash
			if(config.flashForUnwatched())
			{
				triggerFlash();
				notifiers.put(stallType, true);
			}

			// if the os notif is on, do the notif
			if(config.notifyForUnwatched())
			{
				notifier.notify("Stall Unwatched!");
				notifiers.put(stallType, true);
			}

			if(config.soundForUnwatched())
			{
				client.playSoundEffect(SOUND_ID_UNWATCHED);
				notifiers.put(stallType, true);
			}
		}

		// reset the watch notifs
		if(!watching.get(stallType))
		{
			watchNotifiers.put(stallType, false);
		}

		// if the stall is watched, reset
		// also play watched sound if enabled
		if(watching.get(stallType))
		{
			notifiers.put(stallType, false);

			if(config.soundForWatched() && !watchNotifiers.get(stallType))
			{
				client.playSoundEffect(SOUND_ID_WATCHED);
				watchNotifiers.put(stallType, true);
			}
		}
	}

	@Provides
	PRThievingHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PRThievingHelperConfig.class);
	}
}
