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
package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Reflection for entity.
 * @author asofold
 *
 */
public class ReflectEntity extends ReflectGetHandleBase<Entity> {

    public final Field nmsWidth;
    public final Field nmsLength; // Something like height.
    @MostlyHarmless()
    public final Field nmsHeight; // Not anymore in 1.11.

    // TODO: Pre 1.7.10 would be a field: nmsBoundingBox
    public final Method nmsGetBoundingBox;

    public final Field nmsDead;

    public final Method nmsDamageEntity;

    @MostlyHarmless()
    public final Method nmsclearActiveItem;

    public final boolean nmsDamageEntityInt;

    public ReflectEntity(ReflectBase base, ReflectAxisAlignedBB reflectAxisAlignedBB, ReflectDamageSource damageSource) throws ClassNotFoundException {
        this(base, reflectAxisAlignedBB, damageSource, Class.forName(base.obcPackageName + ".entity.CraftEntity"), Class.forName(base.nmsPackageName + ".Entity"));
    }

    public ReflectEntity(ReflectBase base, ReflectAxisAlignedBB reflectAxisAlignedBB, ReflectDamageSource damageSource, Class<?> obcClass, Class<?> nmsClass) throws ClassNotFoundException {
        // base
        super(base, obcClass, nmsClass);

        // width, length (height)
        nmsWidth = ReflectionUtil.getField(nmsClass, "width", float.class);
        nmsLength = ReflectionUtil.getField(nmsClass, "length", float.class);
        nmsHeight = ReflectionUtil.getField(nmsClass, "height", float.class); // Rather old CB around 1.6.

        // dead
        nmsDead = ReflectionUtil.getField(nmsClass, "dead", boolean.class);

        // damageEntity(...)
        nmsDamageEntity = ReflectionUtil.getMethod(nmsClass, "damageEntity", 
                new Class<?>[]{damageSource.nmsClass, float.class}, new Class<?>[]{damageSource.nmsClass, int.class});
        if (nmsDamageEntity != null) {
            nmsDamageEntityInt = nmsDamageEntity.getParameterTypes()[1] == int.class;
        } else {
            nmsDamageEntityInt = true; // Uncertain.
        }
        nmsclearActiveItem = ReflectionUtil.getMethodNoArgs(nmsClass, "clearActiveItem");

        // getBoundingBox
        if (reflectAxisAlignedBB == null) {
            this.nmsGetBoundingBox = null;
        }
        else {
            this.nmsGetBoundingBox = ReflectionUtil.getMethodNoArgs(nmsClass, "getBoundingBox", reflectAxisAlignedBB.nmsClass);
        }
    }

    @Override
    protected void fail() {
        // Unused.
    }

}
