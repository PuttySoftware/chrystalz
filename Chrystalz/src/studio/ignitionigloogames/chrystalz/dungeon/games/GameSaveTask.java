/*  TallerTower: An RPG
Copyright (C) 2008-2012 Eric Ahnell

Any questions should be directed to the author via email at: products@puttysoftware.com
 */
package studio.ignitionigloogames.chrystalz.dungeon.games;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import studio.ignitionigloogames.chrystalz.Application;
import studio.ignitionigloogames.chrystalz.Chrystalz;
import studio.ignitionigloogames.chrystalz.dungeon.Dungeon;
import studio.ignitionigloogames.chrystalz.dungeon.Extension;
import studio.ignitionigloogames.chrystalz.dungeon.PrefixHandler;
import studio.ignitionigloogames.chrystalz.dungeon.SuffixHandler;
import studio.ignitionigloogames.common.fileio.ZipUtilities;

public class GameSaveTask extends Thread {
    // Fields
    private String filename;

    // Constructors
    public GameSaveTask(final String file) {
        this.filename = file;
        this.setName("Game Writer");
    }

    @Override
    public void run() {
        boolean success = true;
        try {
            final Application app = Chrystalz.getApplication();
            // filename check
            final boolean hasExtension = GameSaveTask
                    .hasExtension(this.filename);
            if (!hasExtension) {
                this.filename += Extension.getGameExtensionWithPeriod();
            }
            final File mazeFile = new File(this.filename);
            final File tempLock = new File(
                    Dungeon.getDungeonTempFolder() + "lock.tmp");
            // Set prefix handler
            app.getDungeonManager().getDungeon()
                    .setPrefixHandler(new PrefixHandler());
            // Set suffix handler
            app.getDungeonManager().getDungeon()
                    .setSuffixHandler(new SuffixHandler());
            app.getDungeonManager().getDungeon().writeDungeon();
            ZipUtilities.zipDirectory(
                    new File(
                            app.getDungeonManager().getDungeon().getBasePath()),
                    tempLock);
            // Lock the file
            GameFileManager.save(tempLock, mazeFile);
            final boolean delSuccess = tempLock.delete();
            if (!delSuccess) {
                throw new IOException("Failed to delete temporary file!");
            }
        } catch (final FileNotFoundException fnfe) {
            success = false;
        } catch (final Exception ex) {
            Chrystalz.getErrorLogger().logError(ex);
        }
        Chrystalz.getApplication().getDungeonManager()
                .handleDeferredSuccess(success, false, null);
    }

    private static boolean hasExtension(final String s) {
        String ext = null;
        final int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        if (ext == null) {
            return false;
        } else {
            return true;
        }
    }
}
