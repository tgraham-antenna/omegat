/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.exttrans;

import java.awt.Window;

import org.omegat.util.Language;

/**
 * Interface for all machine translation systems.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public interface IMachineTranslation {
    /**
     * Get machine translation engine name.
     */
    String getName();

    /**
     * Determine whether or not the MT provider has been enabled by the user.
     */
    boolean isEnabled();

    /**
     * Turn the MT provider on or off
     */
    default void setEnabled(boolean enabled) {
        // Nothing
    }

    /**
     * Translate.
     * 
     * @param sLang
     *            source language
     * @param tLang
     *            target language
     * @param text
     *            text for translation
     * @return translated text, or null if translation impossible
     */
    String getTranslation(Language sLang, Language tLang, String text) throws Exception;
    
    /**
     * Get cached translation. Returns null if translation not present.
     * 
     * @param sLang
     *            source language
     * @param tLang
     *            target language
     * @param text
     *            text for translation
     * @return translated text, or null if translation impossible
     */
    String getCachedTranslation(Language sLang, Language tLang, String text);

    /**
     * Indicates that the MT provider has options that can be configured.
     * Configurable implementations should override this to return
     * <code>true</code>, and implement their configuration UI by overriding
     * {@link #showConfigurationUI(Window)}.
     */
    default boolean isConfigurable() {
        return false;
    }

    /**
     * Invoke the configuration UI of this MT provider.
     */
    default void showConfigurationUI(Window parent) {
    }
}
