/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin
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
package org.omegat.languagetools;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.mark.Mark;

public interface ILanguageToolBridge {

    /**
     * Handle project load
     */
    void onProjectLoad();

    /**
     * Handle project closing
     */
    void onProjectClose();

    /**
     * Free resources before destruction
     */
    void destroy();

    /**
     * Get marks for Entry
     * @param ste
     * @param sourceText
     * @param translationText
     * @return List of Marks or null
     * @throws java.lang.Exception
     */
    public List<Mark> getMarksForEntry(SourceTextEntry ste, String sourceText, String translationText)
        throws Exception;

}