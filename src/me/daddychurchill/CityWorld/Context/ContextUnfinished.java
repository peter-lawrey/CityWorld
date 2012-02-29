package me.daddychurchill.CityWorld.Context;

import java.util.Random;

import me.daddychurchill.CityWorld.CityWorld;

public class ContextUnfinished extends PlatMapContext {

	public ContextUnfinished(CityWorld plugin, Random rand) {
		super(plugin, rand);

		setFloorRange(rand, 9, 4);

		oddsOfParks = oddsUnlikely;
		oddsOfIsolatedLots = oddsLikely;
		oddsOfIdenticalBuildingHeights = oddsExtremelyLikely;
		oddsOfSimilarBuildingHeights = oddsExtremelyLikely;
		oddsOfSimilarBuildingRounding = oddsExtremelyLikely;
		oddsOfMissingRoad = oddsLikely;
		oddsOfRoundAbouts = oddsLikely;
		 
		oddsOfUnfinishedBuildings = oddsExtremelyLikely;
		oddsOfOnlyUnfinishedBasements = oddsLikely;
		oddsOfCranes = oddsExtremelyLikely;
		
		oddsOfStairWallMaterialIsWallMaterial = oddsExtremelyLikely;
		oddsOfBuildingWallInset = oddsExtremelyLikely;
		oddsOfFlatWalledBuildings = oddsExtremelyLikely;
		oddsOfSimilarInsetBuildings = oddsExtremelyLikely;
		rangeOfWallInset = 2;
	}

}