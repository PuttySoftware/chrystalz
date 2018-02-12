/*  Chrystalz: A dungeon-crawling, roguelike game
Licensed under MIT. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/IgnitionIglooGames/chrystalz
 */
package studio.ignitionigloogames.chrystalz.manager.name;

import studio.ignitionigloogames.chrystalz.manager.string.LocalizedFile;
import studio.ignitionigloogames.chrystalz.manager.string.StringManager;

public class ZoneNames {
    // Private constructor
    private ZoneNames() {
        // Do nothing
    }

    public static final String getName(final int ID) {
        String tempZoneID = Integer.toString(ID);
        String zoneID;
        if (tempZoneID.length() == 1) {
            zoneID = "0" + tempZoneID;
        } else {
            zoneID = tempZoneID;
        }
        return StringManager.getLocalizedString(LocalizedFile.ZONES, zoneID);
    }
}