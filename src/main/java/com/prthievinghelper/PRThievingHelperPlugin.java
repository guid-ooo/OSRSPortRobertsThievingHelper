package com.prthievinghelper;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.*;
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

	// Stall object IDs (based on comments in config file)
	private static final int FUR_STALL_ID = 58102;
	private static final int SILK_STALL_ID = 58101;
	private static final int GEM_STALL_ID = 58106;
	private static final int CANNON_STALL_ID = 58108;
	private static final int FISH_STALL_ID = 58103;
	private static final int ORE_STALL_ID = 58107;
	private static final int SPICE_STALL_ID = 58105;
	private static final int VEG_STALL_ID = 58100;
	private static final int SILVER_STALL_ID = 58104;

	private static final Map<StallTypes, Integer> stallObjectIds = Map.of(
			StallTypes.FUR, FUR_STALL_ID,
			StallTypes.SILK, SILK_STALL_ID,
			StallTypes.GEM, GEM_STALL_ID,
			StallTypes.CANNON, CANNON_STALL_ID,
			StallTypes.FISH, FISH_STALL_ID,
			StallTypes.ORE, ORE_STALL_ID,
			StallTypes.SPICE, SPICE_STALL_ID,
			StallTypes.VEG, VEG_STALL_ID,
			StallTypes.SILVER, SILVER_STALL_ID
	);

	public Map<StallTypes, Boolean> watching = new HashMap<>();
	
	// Track when guards arrive at stalls to predict when they'll leave
	private final Map<StallTypes, Integer> stallWatchTicksRemaining = new HashMap<>();
	private static final int GUARD_WATCH_DURATION = 10; // Guards watch for 10 ticks
	private static final int THIEVING_DURATION = 5; // Thieving takes 5 ticks
	private static final int SAFE_BUFFER_TICKS = 2; // Extra buffer for safety (highlight when ≤2 ticks remain)

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
	private PRThievingHelperObjectOverlay objectOverlay;

	@Inject
	private Notifier notifier;

	private float flashAlpha = 0f;

	// Object highlighting tracking
	private final Map<Integer, TileObject> stallObjects = new HashMap<>();
	private static final int THIEVING_ANIMATION = 881; // Standard thieving animation

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		overlayManager.add(objectOverlay);

		for (StallTypes stall : StallTypes.values()) {
			watching.put(stall, false);
			notifiers.put(stall, false);
			watchNotifiers.put(stall, false);
			stallWatchTicksRemaining.put(stall, 0);
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		overlayManager.remove(objectOverlay);
		watching.clear();
		guards.clear();
		stallObjects.clear();
		stallWatchTicksRemaining.clear();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		checkForStallObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		stallObjects.remove(event.getGameObject().getId());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		checkForStallObject(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		stallObjects.remove(event.getGroundObject().getId());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		checkForStallObject(event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		stallObjects.remove(event.getDecorativeObject().getId());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		checkForStallObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		stallObjects.remove(event.getWallObject().getId());
	}

	private void checkForStallObject(TileObject object)
	{
		if (object == null)
		{
			return;
		}
		
		int id = object.getId();
		// Check if this is one of our stall objects
		if (stallObjectIds.containsValue(id))
		{
			stallObjects.put(id, object);
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		if(!isValidGuard(npc))
			return;

		if(guards.contains(npc))
			return;

		guards.add(npc);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		if(!isValidGuard(npc))
			return;

        guards.remove(npc);
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (flashAlpha > 0f)
		{
			flashAlpha -= (float) config.notifierFlashSpeed();
			if (flashAlpha < 0f)
			{
				flashAlpha = 0f;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		//System.out.println(client.getSelectedSceneTile().getWorldLocation());

		// Update guard watching status and track timing
		for (StallTypes stall : StallTypes.values())
		{
			boolean wasWatching = watching.get(stall);
			boolean isWatching = isAnyGuardAtPosition(stallWatchPositions.get(stall));
			watching.put(stall, isWatching);
			
			// Track guard watch duration
			if (isWatching && !wasWatching)
			{
				// Guard just arrived at this stall
				stallWatchTicksRemaining.put(stall, GUARD_WATCH_DURATION);
			}
			else if (isWatching && wasWatching)
			{
				// Guard is still watching, decrement timer
				int ticksRemaining = stallWatchTicksRemaining.get(stall);
				if (ticksRemaining > 0)
				{
					stallWatchTicksRemaining.put(stall, ticksRemaining - 1);
				}
			}
			else if (!isWatching)
			{
				// No guard at this stall
				stallWatchTicksRemaining.put(stall, 0);
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

	/**
	 * Gets the tile object to highlight for a given stall selection.
	 * Returns the object if it's safe to click (not watched, or guard leaving soon).
	 */
	private TileObject getStallObjectToHighlight(PRThievingHelperConfig.StallSelection stallSelection)
	{
		if (!config.enableStallHighlighting())
		{
			return null;
		}

		StallTypes stallType = configStallToStallType(stallSelection);
		if (stallType == null)
		{
			return null;
		}

		// Highlight if safe to click (not watched, or guard leaving soon)
		if (isStallSafeToClick(stallType))
		{
			Integer objectId = stallObjectIds.get(stallType);
			if (objectId != null)
			{
				return stallObjects.get(objectId);
			}
		}
		
		return null;
	}

	public TileObject getPrimaryStallToHighlight()
	{
		return getStallObjectToHighlight(config.primaryStall());
	}

	public TileObject getSecondaryStallToHighlight()
	{
		return getStallObjectToHighlight(config.secondaryStall());
	}

	private boolean isValidGuard(NPC npc)
	{
		String npcName = npc.getName();
		if(npcName == null)
			return false;

		int npcId = npc.getId();
        return npcName.equals(GUARD_NAME) && GUARD_IDS.contains(npcId);
    }

	/**
	 * Checks if a stall is safe to highlight for clicking.
	 * A stall is safe if:
	 * - No guard is watching it, OR
	 * - A guard is watching but will leave within SAFE_BUFFER_TICKS ticks
	 *   This gives players time to click right before the guard leaves
	 */
	private boolean isStallSafeToClick(StallTypes stall)
	{
		if (stall == null)
		{
			return false;
		}
		
		boolean isWatched = watching.get(stall);
		
		if (!isWatched)
		{
			// No guard watching, definitely safe
			return true;
		}
		
		// Guard is watching - check if it will leave very soon
		int ticksRemaining = stallWatchTicksRemaining.get(stall);
		
		// Only highlight when guard is about to leave (≤ SAFE_BUFFER_TICKS remaining)
		// This ensures the guard leaves before/as the player clicks
		return ticksRemaining > 0 && ticksRemaining <= SAFE_BUFFER_TICKS;
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

	private StallTypes configStallToStallType(PRThievingHelperConfig.StallSelection selection)
	{
		switch (selection)
		{
			case CANNONBALL:
				return StallTypes.CANNON;
			case VEG:
				return StallTypes.VEG;
			case ORE:
				return StallTypes.ORE;
			case FISH:
				return StallTypes.FISH;
			case SPICE:
				return StallTypes.SPICE;
			case SILVER:
				return StallTypes.SILVER;
			case GEM:
				return StallTypes.GEM;
			case FUR:
				return StallTypes.FUR;
			case SILK:
				return StallTypes.SILK;
			case NONE:
			default:
				return null;
		}
	}
}
