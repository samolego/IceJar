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
package fr.neatmonster.nocheatplus.compat.cbreflect;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.bukkit.BlockCacheBukkit;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class BlockCacheCBReflect extends BlockCacheBukkit {

    // TODO: Not sure if reflection can gain speed over Bukkit API anywhere (who wants to try?).

    protected final ReflectHelper helper;

    protected Object nmsWorld = null;

    public BlockCacheCBReflect(ReflectHelper reflectHelper, World world) {
        super(world);
        this.helper = reflectHelper;
    }

    @Override
    public BlockCache setAccess(World world) {
        super.setAccess(world);
        this.nmsWorld = world == null ? null : helper.getHandle(world);
        return this;
    }

    @Override
    public double[] fetchBounds(int x, int y, int z) {
        try {
            return helper.nmsWorld_fetchBlockShape(this.nmsWorld, this.getType(x, y, z), x, y, z);
        }
        catch (ReflectFailureException ex) {
            return super.fetchBounds(x, y, z);
        }
    }

    @Override
    public boolean standsOnEntity(Entity entity, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        // TODO: Implement once relevant.
        return super.standsOnEntity(entity, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.nmsWorld = null;
    }

}
