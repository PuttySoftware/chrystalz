/*  Chrystalz: A dungeon-crawling, roguelike game
Licensed under MIT. See the LICENSE file for details.

All support is handled via the GitHub repository: https://github.com/IgnitionIglooGames/chrystalz
 */
package studio.ignitionigloogames.chrystalz.items;

import studio.ignitionigloogames.chrystalz.manager.string.LocalizedFile;
import studio.ignitionigloogames.chrystalz.manager.string.StringManager;

public class ArmorConstants {
    // Private Constructor
    private ArmorConstants() {
        // Do nothing
    }

    public static synchronized String getArmor(final int index) {
        return StringManager.getLocalizedString(LocalizedFile.ARMOR_TYPES,
                index);
    }
}
