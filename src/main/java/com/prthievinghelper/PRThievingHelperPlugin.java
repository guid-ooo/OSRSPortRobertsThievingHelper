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
import net.runelite.api.events.StatChanged;
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
	
	// Strategic thieving limits
	private static final int PRIMARY_THIEVE_LIMIT = 4;
	private static final int SECONDARY_THIEVE_LIMIT = 2;
	private static final int BASE_THIEVES_PER_STALL = 4;
	
	// Track completed thieves per stall (resets when stall becomes unwatched)
	private final Map<StallTypes, Integer> completedThievesAtStall = new HashMap<>();
	
	// Track which stall should switch away (show switch highlight on other stall)
	private final Map<StallTypes, Boolean> stallShouldSwitchAway = new HashMap<>();
	
	// XP values for each stall type (for detecting which stall was thieved)
	// Some stalls give fractional XP, so we check a range (±0.5)
	private static final Map<StallTypes, Double> STALL_XP_VALUES = Map.of(
			StallTypes.VEG, 5.0,
			StallTypes.SILK, 24.0,
			StallTypes.FUR, 38.5,
			StallTypes.FISH, 49.5,
			StallTypes.SILVER, 80.0,
			StallTypes.SPICE, 110.0,
			StallTypes.GEM, 129.5,
			StallTypes.ORE, 191.0,
			StallTypes.CANNON, 223.0
	);
	
	private int previousThievingXp = -1;

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
			completedThievesAtStall.put(stall, 0);
			stallShouldSwitchAway.put(stall, false);
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
		completedThievesAtStall.clear();
		stallShouldSwitchAway.clear();
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
	public void onStatChanged(StatChanged statChanged)
	{
		// Check if thieving XP changed
		if (statChanged.getSkill() != Skill.THIEVING)
		{
			return;
		}
		
		int currentXp = statChanged.getXp();
		
		// Initialize on first check
		if (previousThievingXp == -1)
		{
			previousThievingXp = currentXp;
			return;
		}
		
		// Check if XP increased (successful thieve)
		if (currentXp > previousThievingXp)
		{
			int xpGained = currentXp - previousThievingXp;
			previousThievingXp = currentXp;
			
			// Determine which stall was thieved based on XP gained
			// Use ±0.5 range to account for fractional XP that alternates rounding
			StallTypes thievedStall = null;
			for (Map.Entry<StallTypes, Double> entry : STALL_XP_VALUES.entrySet())
			{
				double expectedXp = entry.getValue();
				// Check if gained XP is within 0.5 of expected (handles rounding)
				if (xpGained >= expectedXp - 0.5 && xpGained <= expectedXp + 0.5)
				{
					thievedStall = entry.getKey();
					break;
				}
			}
			
			// Increment completed thieves counter
			if (thievedStall != null)
			{
				int current = completedThievesAtStall.getOrDefault(thievedStall, 0);
				completedThievesAtStall.put(thievedStall, current + 1);
				
				// Check if we should switch away from this stall
				int limit = getStrategicLimit(thievedStall);
				if (current + 1 >= limit)
				{
					stallShouldSwitchAway.put(thievedStall, true);
				}
				
				// Clear the switch flag for OTHER stalls (we're actively thieving this one now)
				for (StallTypes otherStall : StallTypes.values())
				{
					if (otherStall != thievedStall && stallShouldSwitchAway.get(otherStall))
					{
						stallShouldSwitchAway.put(otherStall, false);
					}
				}
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
			
			// Reset completed thieves counter when stall becomes unwatched (fresh opportunity)
			if (!isWatching && wasWatching)
			{
				completedThievesAtStall.put(stall, 0);
				// Don't clear stallShouldSwitchAway here - let it persist until we thieve the other stall
			}
			
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
	 * Helper methods for primary/secondary stall resolution
	 */
	private StallTypes getPrimaryStallType()
	{
		return configStallToStallType(config.primaryStall());
	}

	private StallTypes getSecondaryStallType()
	{
		return configStallToStallType(config.secondaryStall());
	}

	/**
	 * Gets the strategic thieve limit for a stall based on its role.
	 * Primary: 4 thieves, Secondary: 2 thieves, Others: 4 thieves
	 */
	private int getStrategicLimit(StallTypes stall)
	{
		if (stall == getPrimaryStallType())
		{
			return PRIMARY_THIEVE_LIMIT;
		}
		else if (stall == getSecondaryStallType())
		{
			return SECONDARY_THIEVE_LIMIT;
		}
		return BASE_THIEVES_PER_STALL;
	}

	/**
	 * Checks if we should show switch highlight on the target stall.
	 * Returns true if the OTHER stall (not target) has hit its limit.
	 */
	private boolean shouldShowSwitchHighlightOn(StallTypes targetStall)
	{
		StallTypes primaryType = getPrimaryStallType();
		StallTypes secondaryType = getSecondaryStallType();
		
		// Show switch on secondary if primary hit its limit
		if (targetStall == secondaryType && primaryType != null)
		{
			return stallShouldSwitchAway.getOrDefault(primaryType, false);
		}
		// Show switch on primary if secondary hit its limit
		if (targetStall == primaryType && secondaryType != null)
		{
			return stallShouldSwitchAway.getOrDefault(secondaryType, false);
		}
		return false;
	}

	/**
	 * Gets the tile object to highlight for a given stall selection.
	 * Returns the object if it's safe to click (not watched, or guard leaving soon),
	 * OR if we need to show switch highlight (other stall hit its limit).
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
		
		// Highlight if safe to click OR if we need to show switch highlight
		boolean showSwitchHighlight = shouldShowSwitchHighlightOn(stallType);
		if (isStallSafeToClick(stallType) || showSwitchHighlight)
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
	 * A stall is safe if it's not watched and hasn't reached its strategic limit.
	 */
	private boolean isStallSafeToClick(StallTypes stall)
	{
		if (stall == null || watching.get(stall))
		{
			return false;
		}
		
		int completed = completedThievesAtStall.getOrDefault(stall, 0);
		int limit = getStrategicLimit(stall);
		return completed < limit;
	}
	
	/**
	 * Returns true if this stall just hit 4 completed thieves and should show switch highlight on OTHER stall.
	 */
	public boolean shouldSwitchAwayFrom(PRThievingHelperConfig.StallSelection stallSelection)
	{
		StallTypes stallType = configStallToStallType(stallSelection);
		if (stallType == null)
		{
			return false;
		}
		return stallShouldSwitchAway.getOrDefault(stallType, false);
	}
	
	/**
	 * Calculates remaining thieves for a stall.
	 * When unwatched: 4 thieves possible before guard arrives
	 * This decreases as you complete thieves at that stall
	 * Resets to 4 when stall becomes unwatched again
	 */
	private int getRemainingThieves(StallTypes stall)
	{
		if (stall == null || watching.get(stall))
		{
			return 0;
		}
		
		int completed = completedThievesAtStall.getOrDefault(stall, 0);
		return Math.max(0, BASE_THIEVES_PER_STALL - completed);
	}
	
	/**
	 * Gets the display number for a stall (with strategic caps applied).
	 * Primary stall: max 4 thieves
	 * Secondary stall: max 2 thieves
	 */
	public Integer getPossibleThievesForDisplay(PRThievingHelperConfig.StallSelection stallSelection)
	{
		StallTypes stallType = configStallToStallType(stallSelection);
		if (stallType == null || !config.enableStallHighlighting())
		{
			return null;
		}
		
		// If showing switch highlight (other stall hit limit), show the fresh count
		if (shouldShowSwitchHighlightOn(stallType))
		{
			return getStrategicLimit(stallType);
		}
		
		// If watched, don't show
		if (watching.get(stallType))
		{
			return null;
		}
		
		// Normal case: show countdown
		int remaining = getRemainingThieves(stallType);
		if (remaining <= 0)
		{
			return 0;
		}
		
		int completed = completedThievesAtStall.getOrDefault(stallType, 0);
		int limit = getStrategicLimit(stallType);
		StallTypes primaryType = getPrimaryStallType();
		StallTypes secondaryType = getSecondaryStallType();
		
		if (stallType == primaryType)
		{
			return Math.min(remaining, limit);
		}
		else if (stallType == secondaryType)
		{
			return Math.max(0, limit - completed);
		}
		
		return remaining;
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
