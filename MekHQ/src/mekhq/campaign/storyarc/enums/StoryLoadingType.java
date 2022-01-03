/*
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
package mekhq.campaign.storyarc.enums;

import megamek.common.Compute;

/**
 * This enum indicates whether a story arc has to start a new campaign, can be added to an existing
 * campaign or both.
 */
public enum StoryLoadingType {
    //region Enum Declarations
    START_NEW,
    LOAD_EXISTING,
    BOTH;
    //endregion Enum Declarations

    public boolean canStartNew() {
        switch (this) {
            case START_NEW:
            case BOTH:
                return true;
            default:
                return false;
        }
    }

    public boolean canLoadExisting() {
        switch (this) {
            case LOAD_EXISTING:
            case BOTH:
                return true;
            default:
                return false;
        }
    }



}
