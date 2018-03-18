/*

File: OSXAdapter.java

Abstract: Hooks existing preferences/about/quit functionality from an
existing Java app into handlers for the Mac OS X application menu.
Uses a Proxy object to dynamically implement the 
com.apple.eawt.ApplicationListener interface and register it with the
com.apple.eawt.Application object.  This allows the complete project
to be both built and run on any platform without any stubs or 
placeholders. Useful for developers looking to implement Mac OS X 
features while supporting multiple platforms with minimal impact.

Version: 2.0

Disclaimer: IMPORTANT:  This Apple software is supplied to you by 
Apple Inc. ("Apple") in consideration of your agreement to the
following terms, and your use, installation, modification or
redistribution of this Apple software constitutes acceptance of these
terms.  If you do not agree with these terms, please do not use,
install, modify or redistribute this Apple software.

In consideration of your agreement to abide by the following terms, and
subject to these terms, Apple grants you a personal, non-exclusive
license, under Apple's copyrights in this original Apple software (the
"Apple Software"), to use, reproduce, modify and redistribute the Apple
Software, with or without modifications, in source and/or binary forms;
provided that if you redistribute the Apple Software in its entirety and
without modifications, you must retain this notice and the following
text and disclaimers in all such redistributions of the Apple Software. 
Neither the name, trademarks, service marks or logos of Apple Inc. 
may be used to endorse or promote products derived from the Apple
Software without specific prior written permission from Apple.  Except
as expressly stated in this notice, no other rights or licenses, express
or implied, are granted by Apple herein, including but not limited to
any patent rights that may be infringed by your derivative works or by
other works in which the Apple Software may be incorporated.

The Apple Software is provided by Apple on an "AS IS" basis.  APPLE
MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND
OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.

IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED
AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

Copyright 2003-2007 Apple, Inc., All Rights Reserved

 */
package apple.dts.samplecode.osxadapter;

import java.awt.Image;
import java.awt.Window;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class OSXAdapter implements InvocationHandler {
    protected Object targetObject;
    protected Method targetMethod;
    protected String proxySignature;
    static Object macOSXApplication;

    // Pass this method an Object and Method equipped to perform application
    // shutdown logic
    // The method passed should return a boolean stating whether or not the quit
    // should occur
    public static void setQuitHandler(Object target, Method quitHandler) {
        OSXAdapter
                .setHandler(new OSXAdapter("handleQuit", target, quitHandler));
    }

    // Pass this method an Object and Method equipped to display application
    // info
    // They will be called when the About menu item is selected from the
    // application menu
    public static void setAboutHandler(Object target, Method aboutHandler) {
        boolean enableAboutMenu = (target != null && aboutHandler != null);
        if (enableAboutMenu) {
            OSXAdapter.setHandler(
                    new OSXAdapter("handleAbout", target, aboutHandler));
        }
        // If we're setting a handler, enable the About menu item by calling
        // com.apple.eawt.Application reflectively
        try {
            Method enableAboutMethod = OSXAdapter.macOSXApplication.getClass()
                    .getDeclaredMethod("setEnabledAboutMenu",
                            new Class<?>[] { boolean.class });
            enableAboutMethod.invoke(OSXAdapter.macOSXApplication,
                    new Object[] { Boolean.valueOf(enableAboutMenu) });
        } catch (Exception ex) {
            System.err.println("OSXAdapter could not access the About Menu");
            ex.printStackTrace();
        }
    }

    // Pass this method an Object and a Method equipped to display application
    // options
    // They will be called when the Preferences menu item is selected from the
    // application menu
    public static void setPreferencesHandler(Object target,
            Method prefsHandler) {
        boolean enablePrefsMenu = (target != null && prefsHandler != null);
        if (enablePrefsMenu) {
            OSXAdapter.setHandler(
                    new OSXAdapter("handlePreferences", target, prefsHandler));
        }
        // If we're setting a handler, enable the Preferences menu item by
        // calling
        // com.apple.eawt.Application reflectively
        try {
            Method enablePrefsMethod = OSXAdapter.macOSXApplication.getClass()
                    .getDeclaredMethod("setEnabledPreferencesMenu",
                            new Class<?>[] { boolean.class });
            enablePrefsMethod.invoke(OSXAdapter.macOSXApplication,
                    new Object[] { Boolean.valueOf(enablePrefsMenu) });
        } catch (Exception ex) {
            System.err.println("OSXAdapter could not access the About Menu");
            ex.printStackTrace();
        }
    }

    // Pass this method an Object and a Method equipped to handle document
    // events from the Finder
    // Documents are registered with the Finder via the CFBundleDocumentTypes
    // dictionary in the
    // application bundle's Info.plist
    public static void setFileHandler(Object target, Method fileHandler) {
        OSXAdapter.setHandler(
                new OSXAdapter("handleOpenFile", target, fileHandler) {
                    // Override OSXAdapter.callTarget to send information on the
                    // file to be opened
                    @Override
                    public boolean callTarget(Object appleEvent) {
                        if (appleEvent != null) {
                            try {
                                Method getFilenameMethod = appleEvent.getClass()
                                        .getDeclaredMethod("getFilename",
                                                (Class<?>[]) null);
                                String filename = (String) getFilenameMethod
                                        .invoke(appleEvent, (Object[]) null);
                                this.targetMethod.invoke(this.targetObject,
                                        new Object[] { filename });
                            } catch (Exception ex) {
                            }
                        }
                        return true;
                    }
                });
    }

    // setHandler creates a Proxy object from the passed OSXAdapter and adds it
    // as an ApplicationListener
    public static void setHandler(OSXAdapter adapter) {
        try {
            Class<?> applicationClass = Class
                    .forName("com.apple.eawt.Application");
            if (OSXAdapter.macOSXApplication == null) {
                OSXAdapter.macOSXApplication = applicationClass
                        .getConstructor((Class<?>[]) null)
                        .newInstance((Object[]) null);
            }
            Class<?> applicationListenerClass = Class
                    .forName("com.apple.eawt.ApplicationListener");
            Method addListenerMethod = applicationClass.getDeclaredMethod(
                    "addApplicationListener",
                    new Class<?>[] { applicationListenerClass });
            // Create a proxy object around this handler that can be
            // reflectively added as an Apple ApplicationListener
            Object osxAdapterProxy = Proxy.newProxyInstance(
                    OSXAdapter.class.getClassLoader(),
                    new Class<?>[] { applicationListenerClass }, adapter);
            addListenerMethod.invoke(OSXAdapter.macOSXApplication,
                    new Object[] { osxAdapterProxy });
        } catch (ClassNotFoundException cnfe) {
            System.err.println(
                    "This version of Mac OS X does not support the Apple EAWT.  ApplicationEvent handling has been disabled ("
                            + cnfe + ")");
        } catch (Exception ex) { // Likely a NoSuchMethodException or an
            // IllegalAccessException loading/invoking
            // eawt.Application methods
            System.err.println("Mac OS X Adapter could not talk to EAWT:");
            ex.printStackTrace();
        }
    }

    // Each OSXAdapter has the name of the EAWT method it intends to listen for
    // (handleAbout, for example),
    // the Object that will ultimately perform the task, and the Method to be
    // called on that Object
    protected OSXAdapter(String proxySig, Object target, Method handler) {
        this.proxySignature = proxySig;
        this.targetObject = target;
        this.targetMethod = handler;
    }

    // Override this method to perform any operations on the event
    // that comes with the various callbacks
    // See setFileHandler above for an example
    /**
     * 
     * @param appleEvent
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public boolean callTarget(Object appleEvent)
            throws InvocationTargetException, IllegalAccessException {
        Object result = this.targetMethod.invoke(this.targetObject);
        if (result == null) {
            return true;
        }
        return Boolean.parseBoolean(result.toString());
    }

    // InvocationHandler implementation
    // This is the entry point for our proxy object; it is called every time an
    // ApplicationListener method is invoked
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if (this.isCorrectMethod(method, args)) {
            boolean handled = this.callTarget(args[0]);
            OSXAdapter.setApplicationEventHandled(args[0], handled);
        }
        // All of the ApplicationListener methods are void; return null
        // regardless of what happens
        return null;
    }

    // Compare the method that was called to the intended method when the
    // OSXAdapter instance was created
    // (e.g. handleAbout, handleQuit, handleOpenFile, etc.)
    protected boolean isCorrectMethod(Method method, Object[] args) {
        return (this.targetMethod != null
                && this.proxySignature.equals(method.getName())
                && args.length == 1);
    }

    // It is important to mark the ApplicationEvent as handled and cancel the
    // default behavior
    // This method checks for a boolean result from the proxy method and sets
    // the event accordingly
    protected static void setApplicationEventHandled(Object event,
            boolean handled) {
        if (event != null) {
            try {
                Method setHandledMethod = event.getClass().getDeclaredMethod(
                        "setHandled", new Class<?>[] { boolean.class });
                // If the target method returns a boolean, use that as a hint
                setHandledMethod.invoke(event,
                        new Object[] { Boolean.valueOf(handled) });
            } catch (Exception ex) {
                System.err.println(
                        "OSXAdapter was unable to handle an ApplicationEvent: "
                                + event);
                ex.printStackTrace();
            }
        }
    }

    private static Method badgeMethod;
    private static Object[] badgeParams;

    public static void setDockIconBadge(String badge) {
        if (macOSXApplication == null)
            return;
        try {
            if (badgeParams == null) {
                badgeParams = new Object[1];
                badgeMethod = macOSXApplication.getClass().getDeclaredMethod(
                        "setDockIconBadge", new Class[] { String.class });
            }
            if (badgeMethod != null) {
                badgeParams[0] = badge;
                badgeMethod.invoke(macOSXApplication, badgeParams);
            }
        } catch (Exception ex) {
            // Ignore errors - they're expected under OSX 10.4 and earlier.
        }
    }

    private static Method iconMethod;
    private static Object[] iconParams;

    public static void setDockIconImage(Image icon) {
        if (macOSXApplication == null)
            return;
        try {
            if (iconParams == null) {
                iconParams = new Object[1];
                iconMethod = macOSXApplication.getClass().getDeclaredMethod(
                        "setDockIconImage", new Class[] { Image.class });
            }
            if (iconMethod != null) {
                iconParams[0] = icon;
                iconMethod.invoke(macOSXApplication, iconParams);
            }
        } catch (Exception ex) {
            // Ignore errors - they're expected under OSX 10.4 and earlier.
        }
    }

    private static Class<?> macOSXFullScreenUtilities;
    private static Method fullscreenMethod;
    private static Object[] fullscreenParams;

    public static void setWindowCanFullScreen(Window window,
            boolean canFullScreen) {
        if (macOSXApplication == null)
            return;
        try {
            if (macOSXFullScreenUtilities == null) {
                try {
                    macOSXFullScreenUtilities = Class
                            .forName("com.apple.eawt.FullScreenUtilities");
                } catch (ClassNotFoundException cnfe) {
                    // Ignore errors
                }
            }
            if (macOSXFullScreenUtilities != null && fullscreenParams == null) {
                fullscreenParams = new Object[2];
                fullscreenMethod = macOSXFullScreenUtilities.getMethod(
                        "setWindowCanFullScreen",
                        new Class[] { Window.class, Boolean.TYPE });
            }
            if (fullscreenMethod != null) {
                fullscreenParams[0] = window;
                fullscreenParams[1] = Boolean.valueOf(canFullScreen);
                fullscreenMethod.invoke(macOSXFullScreenUtilities,
                        fullscreenParams);
            }
        } catch (Exception ex) {
            // Ignore errors - they're expected under OSX 10.6 and earlier.
        }
    }
}