/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionData;
import fr.neatmonster.nocheatplus.config.ConfigFileWithActions;

/**
 * If an action can't be parsed correctly, at least keep it stored in this form to not lose it when loading/storing the
 * configuration file.
 */
public class DummyAction<D extends ActionData, L extends AbstractActionList<D, L>> extends Action<D, L> {
    /** The original string used for this action definition. */
    protected final String definition;

    /**
     * Instantiates a new dummy.
     * 
     * @param definition
     *            the definition
     */
    public DummyAction(final String definition) {
        super("dummyAction", 0, 0);
        this.definition = definition;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.actions.Action#execute(fr.neatmonster.nocheatplus.checks.ViolationData)
     */
    @Override
    public void execute(final D violationData) {
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return definition;
    }

    @Override
    public Action<D, L> getOptimizedCopy(final ConfigFileWithActions<D, L> config, final Integer threshold)
    {
        // Never execute this.
        return null;
    }

}
