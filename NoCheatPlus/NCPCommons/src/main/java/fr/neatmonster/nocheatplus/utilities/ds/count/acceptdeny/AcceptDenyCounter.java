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
package fr.neatmonster.nocheatplus.utilities.ds.count.acceptdeny;

/**
 * Default implementation for accept-deny counters,
 * 
 * @author asofold
 *
 */
public class AcceptDenyCounter implements IResettableAcceptDenyCounter, ICounterWithParent {

    private int acceptCount = 0;
    private int denyCount = 0;

    private IAcceptDenyCounter parent = null;

    @Override
    public AcceptDenyCounter setParentCounter(IAcceptDenyCounter parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public IAcceptDenyCounter getParentCounter() {
        return parent;
    }

    @Override
    public void accept() {
        acceptCount ++;
        if (parent != null) {
            parent.accept();
        }
    }

    @Override
    public int getAcceptCount() {
        return acceptCount;
    }

    @Override
    public void deny() {
        denyCount ++;
        if (parent != null) {
            parent.deny();
        }
    }

    @Override
    public int getDenyCount() {
        return denyCount;
    }

    @Override
    public void resetCounter() {
        acceptCount = denyCount = 0;
    }

}
