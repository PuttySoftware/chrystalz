/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package studio.ignitionigloogames.chrystalz.spells;

import studio.ignitionigloogames.chrystalz.creatures.castes.CasteConstants;
import studio.ignitionigloogames.chrystalz.creatures.castes.predefined.AnnihilatorSpellBook;
import studio.ignitionigloogames.chrystalz.creatures.castes.predefined.BufferSpellBook;
import studio.ignitionigloogames.chrystalz.creatures.castes.predefined.CurerSpellBook;
import studio.ignitionigloogames.chrystalz.creatures.castes.predefined.DebufferSpellBook;

public class SpellBookLoader {
    // Constructors
    private SpellBookLoader() {
        // Do nothing
    }

    // Methods
    public static SpellBook loadSpellBook(final int sbid) {
        if (sbid == CasteConstants.CASTE_ANNIHILATOR) {
            return new AnnihilatorSpellBook();
        } else if (sbid == CasteConstants.CASTE_BUFFER) {
            return new BufferSpellBook();
        } else if (sbid == CasteConstants.CASTE_CURER) {
            return new CurerSpellBook();
        } else if (sbid == CasteConstants.CASTE_DEBUFFER) {
            return new DebufferSpellBook();
        } else {
            // Invalid caste name
            return null;
        }
    }
}