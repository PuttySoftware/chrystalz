/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package studio.ignitionigloogames.chrystalz.dungeon.objects;

import studio.ignitionigloogames.chrystalz.assetmanagers.ObjectImageConstants;
import studio.ignitionigloogames.chrystalz.dungeon.abc.AbstractShop;
import studio.ignitionigloogames.chrystalz.shops.ShopTypes;

public class Regenerator extends AbstractShop {
    // Constructors
    public Regenerator() {
        super(ShopTypes.SHOP_TYPE_REGENERATOR);
    }

    @Override
    public int getBaseID() {
        return ObjectImageConstants.OBJECT_IMAGE_REGENERATOR;
    }

    @Override
    public String getName() {
        return "Regenerator";
    }

    @Override
    public String getPluralName() {
        return "Regenerators";
    }

    @Override
    public String getDescription() {
        return "Regenerators restore magic, for a fee.";
    }
}
