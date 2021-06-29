/*
 * RATGeneratorConnector.java
 *
 * Copyright (c) 2016 - Carl Spain. All rights reserved.
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ. If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.universe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import megamek.client.ratgenerator.FactionRecord;
import megamek.client.ratgenerator.ModelRecord;
import megamek.client.ratgenerator.RATGenerator;
import megamek.client.ratgenerator.UnitTable;
import megamek.common.EntityMovementMode;
import megamek.common.MechSummary;
import megamek.common.UnitType;
import megamek.common.annotations.Nullable;
import mekhq.MekHQ;

/**
 * Provides access to RATGenerator through IUnitGenerator interface.
 * @author Neoancient
 */
public class RATGeneratorConnector extends AbstractUnitGenerator implements IUnitGenerator {
    /**
     * Initialize RATGenerator and load the data for the current game year
     */
    public RATGeneratorConnector(final int year) {
        while (!RATGenerator.getInstance().isInitialized()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                MekHQ.getLogger().error(e);
            }
        }
        RATGenerator.getInstance().loadYear(year);
    }

    private @Nullable UnitTable findTable(final String faction, final int unitType, final int weightClass,
                                          final int year, final int quality,
                                          final Collection<EntityMovementMode> movementModes) {
        final FactionRecord factionRecord = Factions.getInstance().getFactionRecordOrFallback(faction);
        if (factionRecord == null) {
            return null;
        }
        final String rating = getFactionSpecificRating(factionRecord, quality);
        final List<Integer> weightClasses = new ArrayList<>();
        if (weightClass >= 0) {
            weightClasses.add(weightClass);
        }
        return UnitTable.findTable(factionRecord, unitType, year, rating, weightClasses, ModelRecord.NETWORK_NONE,
                movementModes, new ArrayList<>(), 2, factionRecord);
    }

    /**
     * Helper function that extracts the string-based unit rating from the given int-based unit-rating
     * for the given faction.
     * @param factionRecord Faction record
     * @param quality Unit quality number
     * @return Unit quality string
     */
    public static String getFactionSpecificRating(final FactionRecord factionRecord, final int quality) {
        String rating = null;
        if (factionRecord.getRatingLevels().size() != 1) {
            final List<String> ratings = factionRecord.getRatingLevelSystem();
            rating = ratings.get(Math.min(quality, ratings.size() - 1));
        }
        return rating;
    }

    /* (non-Javadoc)
     * @see mekhq.campaign.universe.IUnitGenerator#isSupportedUnitType(int)
     */
    @Override
    public boolean isSupportedUnitType(final int unitType) {
        return (unitType != UnitType.GUN_EMPLACEMENT) && (unitType != UnitType.SPACE_STATION);
    }

    /* (non-Javadoc)
     * @see mekhq.campaign.universe.IUnitGenerator#generate(java.lang.String, int, int, int, int)
     */
    @Override
    public @Nullable MechSummary generate(final String faction, final int unitType, final int weightClass,
                                          final int year, final int quality) {
        return generate(faction, unitType, weightClass, year, quality, null);
    }

    /* (non-Javadoc)
     * @see mekhq.campaign.universe.IUnitGenerator#generate(java.lang.String, int, int, int, int, java.util.function.Predicate)
     */
    @Override
    public @Nullable MechSummary generate(final String faction, final int unitType, final int weightClass,
                                          final int year, final int quality,
                                          final @Nullable Predicate<MechSummary> filter) {
        return generate(faction, unitType, weightClass, year, quality, EnumSet.noneOf(EntityMovementMode.class), filter);
    }

    @Override
    public @Nullable MechSummary generate(final String faction, final int unitType, final int weightClass,
                                          final int year, final int quality,
                                          final Collection<EntityMovementMode> movementModes,
                                          final @Nullable Predicate<MechSummary> filter) {
        final UnitTable table = findTable(faction, unitType, weightClass, year, quality, movementModes);
        return (table == null) ? null : table.generateUnit((filter == null) ? null : filter::test);
    }

    /* (non-Javadoc)
     * @see mekhq.campaign.universe.IUnitGenerator#generate(int, java.lang.String, int, int, int, int)
     */
    @Override
    public List<MechSummary> generate(final int count, final String faction, final int unitType, final int weightClass,
                                      final int year, final int quality) {
        return generate(count, faction, unitType, weightClass, year, quality, null);
    }

    /* (non-Javadoc)
     * @see mekhq.campaign.universe.IUnitGenerator#generate(int, java.lang.String, int, int, int, int, java.util.function.Predicate)
     */
    @Override
    public List<MechSummary> generate(final int count, final String faction, final int unitType, final int weightClass,
                                      final int year, final int quality, final @Nullable Predicate<MechSummary> filter) {
        return generate(count, faction, unitType, weightClass, year, quality, EnumSet.noneOf(EntityMovementMode.class), filter);
    }

    @Override
    public List<MechSummary> generate(final int count, final String faction, final int unitType, final int weightClass,
                                      final int year, final int quality,
                                      final Collection<EntityMovementMode> movementModes,
                                      final @Nullable Predicate<MechSummary> filter) {
        final UnitTable table = findTable(faction, unitType, weightClass, year, quality, movementModes);
        return (table == null) ? new ArrayList<>() : table.generateUnits(count, (filter == null) ? null : filter::test);
    }

    /**
     * Generates a list of mech summaries from a RAT determined by the given faction, quality and other parameters.
     * @param count How many units to generate
     * @param parameters RATGenerator parameters
     */
    @Override
    public List<MechSummary> generate(final int count, final UnitGeneratorParameters parameters) {
        final UnitTable table = UnitTable.findTable(parameters.getRATGeneratorParameters());
        return table.generateUnits(count, (parameters.getFilter() == null) ? null : ms -> parameters.getFilter().test(ms));
    }

    /**
     * Generates a single mech summary from a RAT determined by the given faction, quality and other parameters.
     * @param parameters RATGenerator parameters
     */
    @Override
    public MechSummary generate(final UnitGeneratorParameters parameters) {
        final UnitTable table = UnitTable.findTable(parameters.getRATGeneratorParameters());
        return table.generateUnit((parameters.getFilter() == null) ? null : ms -> parameters.getFilter().test(ms));
    }
}
