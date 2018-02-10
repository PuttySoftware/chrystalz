/*  TallerTower: An RPG
Copyright (C) 2011-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package studio.ignitionigloogames.chrystalz.battle;

import javax.swing.JFrame;

import studio.ignitionigloogames.chrystalz.creatures.AbstractCreature;

public abstract class AbstractBattle {
    // Constructors
    protected AbstractBattle() {
        // Do nothing
    }

    // Generic Methods
    public abstract JFrame getOutputFrame();

    public abstract void resetGUI();

    public abstract void doBattle();

    public abstract void doBattleByProxy();

    public abstract void setStatusMessage(final String msg);

    public abstract void executeNextAIAction();

    public abstract boolean getLastAIActionResult();

    public abstract boolean castSpell();

    public abstract boolean useItem();

    public abstract boolean steal();

    public abstract boolean drain();

    public abstract void endTurn();

    public abstract AbstractCreature getEnemy();

    public abstract void battleDone();

    public abstract void displayActiveEffects();

    public abstract void displayBattleStats();

    public abstract boolean doPlayerActions(final int actionType);

    public abstract int getResult();

    public abstract void doResult();

    public abstract void setResult(final int resultCode);

    public abstract void maintainEffects(final boolean player);

    public abstract boolean updatePosition(int x, int y);

    public abstract boolean isWaitingForAI();
}
