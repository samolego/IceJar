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
package fr.neatmonster.nocheatplus.utilities.location;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.components.location.IGetPosition;
import fr.neatmonster.nocheatplus.components.location.IGetPositionWithLook;


/**
 * Auxiliary static methods for trigonometry related tasks, mostly concerning
 * locations/positions and viewing direction, such as distances, directions,
 * angles.
 * 
 * @author asofold
 *
 */
public class TrigUtil {

    /** Used for internal calculations, no passing on, beware of nested calls. */
    private static final Vector vec1 = new Vector();
    /** Used for internal calculations, no passing on, beware of nested calls. */
    private static final Vector vec2 = new Vector();
    /** Multiply to get grad from rad. */
    public static final double fRadToGrad = 360.0 / (2.0 * Math.PI);
    /** Some default precision value for the classic fight.direction check. */
    public static final double DIRECTION_PRECISION = 2.6;
    /** Precision for the fight.direction check within the LocationTrace loop. */
    public static final double DIRECTION_LOOP_PRECISION = 0.5;

    /**
     * 3D-distance of two locations. This is obsolete, since it has been fixed. To ignore world checks it might be "useful".
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final Location location1, final Location location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3D-distance of two locations.
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final IGetPosition location1, final Location location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3D-distance of two locations.
     * 
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return The distance between the locations.
     */
    public static final double distance(final IGetPosition location1, final IGetPosition location2)
    {
        return distance(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * 3d-distance from location (exact) to block middle.
     *
     * @param location
     *            the location
     * @param block
     *            the block
     * @return the double
     */
    public static final double distance(final Location location, final Block block)
    {
        return distance(location.getX(), location.getY(), location.getZ(), 0.5 + block.getX(), 0.5 + block.getY(), 0.5 + block.getZ());
    }

    /**
     * 3D-distance.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the double
     */
    public static final double distance(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);
        final double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Distance squared.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double distanceSquared(final Location location1, final Location location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * Distance squared.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double distanceSquared(final IGetPosition location1, final IGetPosition location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * Horizontal: squared distance.
     *
     * @param location
     *            the location
     * @param x
     *            the x
     * @param z
     *            the z
     * @return the double
     */
    public static final double distanceSquared(final IGetPosition location, final double x, final double z)
    {
        return distanceSquared(location.getX(), location.getZ(), x, z);
    }

    /**
     * Distance squared.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double distanceSquared(final IGetPosition location1, final Location location2)
    {
        return distanceSquared(location1.getX(), location1.getY(), location1.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    /**
     * Distance squared.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the double
     */
    public static final double distanceSquared(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dy = Math.abs(y1 - y2);
        final double dz = Math.abs(z1 - z2);
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Distance squared.
     *
     * @param x1
     *            the x1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param z2
     *            the z2
     * @return the double
     */
    public static final double distanceSquared(final double x1, final double z1, final double x2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dz = Math.abs(z1 - z2);
        return dx * dx + dz * dz;
    }

    /**
     * 2D-distance in x-z plane.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double xzDistance(final Location location1, final Location location2)
    {
        return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
    }

    /**
     * 2D-distance in x-z plane.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double xzDistance(final IGetPosition location1, final IGetPosition location2)
    {
        return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
    }

    /**
     * 2D-distance in x-z plane.
     *
     * @param location1
     *            the location1
     * @param location2
     *            the location2
     * @return the double
     */
    public static final double xzDistance(final Location location1, final IGetPosition location2)
    {
        return distance(location1.getX(), location1.getZ(), location2.getX(), location2.getZ());
    }

    /**
     * 2D-distance.
     *
     * @param x1
     *            the x1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param z2
     *            the z2
     * @return the double
     */
    public static final double distance(final double x1, final double z1, final double x2, final double z2) {
        final double dx = Math.abs(x1 - x2);
        final double dz = Math.abs(z1 - z2);
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Positive angle between vector from source to target and the vector for
     * the given direction [0...PI].
     *
     * @param sourceX
     *            the source x
     * @param sourceY
     *            the source y
     * @param sourceZ
     *            the source z
     * @param dirX
     *            the dir x
     * @param dirY
     *            the dir y
     * @param dirZ
     *            the dir z
     * @param targetX
     *            the target x
     * @param targetY
     *            the target y
     * @param targetZ
     *            the target z
     * @return Positive angle between vector from source to target and the
     *         vector for the given direction [0...PI].
     */
    public static double angle(final double sourceX, final double sourceY, final double sourceZ, final double dirX, final double dirY, final double dirZ, final double targetX, final double targetY, final double targetZ) {
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLength == 0.0) {
            dirLength = 1.0; // ...
        }

        final double dX = targetX - sourceX;
        final double dY = targetY - sourceY;
        final double dZ = targetZ - sourceZ;

        vec1.setX(dX);
        vec1.setY(dY);
        vec1.setZ(dZ);
        vec2.setX(dirX);
        vec2.setY(dirY);
        vec2.setZ(dirZ);
        return angle(vec2, vec1);
    }

    /**
     * Angle of a 2d vector, x being the side at the angle. (radians).
     *
     * @param x
     *            the x
     * @param z
     *            the z
     * @return the double
     */
    public static final double angle(final double x, final double z){
        final double a;
        if (x > 0.0) {
            a = Math.atan(z / x);
        }
        else if  (x < 0.0) {
            a = Math.atan(z / x) + Math.PI;
        }
        else{
            if (z < 0.0) {
                a = 3.0 * Math.PI / 2.0;
            }
            else if (z > 0.0) {
                a = Math.PI / 2.0;
            }
            else {
                return Double.NaN;
            }
        }
        if (a < 0.0) {
            return a + 2.0 * Math.PI;
        }
        else {
            return a;
        }
    }

    /**
     * Angle between 2 non-zero vectors.
     *
     * @param a
     *            First vector
     * @param b
     *            Second vector
     * @return Angle (radians).
     */
    public static final double angle(Vector a, Vector b) {
        final double theta = Math.min(1, Math.max(a.dot(b) / (a.length() * b.length()), -1));
        return Math.acos(theta);
    }

    /**
     * Get the difference of angles (radians) as given from angle(x,z), from a1
     * to a2, i.e. rather a2 - a1 in principle.
     *
     * @param a1
     *            the a1
     * @param a2
     *            the a2
     * @return Difference of angle from -pi to pi
     */
    public static final double angleDiff(final double a1, final double a2){
        if (Double.isNaN(a1) || Double.isNaN(a2)) {
            return Double.NaN;
        }
        final double diff = a2 - a1;
        // TODO: What with resulting special values here?
        if (diff < -Math.PI) {
            return diff + 2.0 * Math.PI;
        }
        else if (diff > Math.PI) {
            return diff - 2.0 * Math.PI;
        }
        else {
            return diff;
        }
    }

    /**
     * Yaw (angle in grad) difference. This ensures inputs are interpreted
     * correctly (for 360 degree offsets).
     *
     * @param fromYaw
     *            the from yaw
     * @param toYaw
     *            the to yaw
     * @return Angle difference to get from fromYaw to toYaw. Result is in
     *         [-180, 180].
     */
    public static final float yawDiff(float fromYaw, float toYaw){
        if (fromYaw <= -360f) {
            fromYaw = -((-fromYaw) % 360f);
        }
        else if (fromYaw >= 360f) {
            fromYaw = fromYaw % 360f;
        }
        if (toYaw <= -360f) {
            toYaw = -((-toYaw) % 360f);
        }
        else if (toYaw >= 360f) {
            toYaw = toYaw % 360f;
        }
        float yawDiff = toYaw - fromYaw;
        if (yawDiff < -180f) {
            yawDiff += 360f;
        }
        else if (yawDiff > 180f) {
            yawDiff -= 360f;
        }
        return yawDiff;
    }

    /**
     * Manhattan distance.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return the int
     */
    public static int manhattan(final Location loc1, final Location loc2) {
        return manhattan(loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
    }

    /**
     * Manhattan distance.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param block
     *            the block
     * @return the int
     */
    public static int manhattan(final int x1, final int y1, final int  z1, final Block block) {
        return manhattan(x1, y1, z1, block.getX(), block.getY(), block.getZ());
    }

    /**
     * Manhattan.
     *
     * @param x1
     *            the x1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param z2
     *            the z2
     * @return the double
     */
    public static double manhattan(final double x1,final double  z1, final double x2, final double z2){
        return manhattan(Location.locToBlock(x1), Location.locToBlock(z1), Location.locToBlock(x2), Location.locToBlock(z2));
    }

    /**
     * Manhattan distance (steps along the sides of an orthogonal grid).
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the int
     */
    public static int manhattan(final int x1, final int y1, final int  z1, final int x2, final int y2, final int z2){
        return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
    }

    /**
     * Manhattan.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the double
     */
    public static double manhattan(final double x1, final double y1, final double  z1, final double x2, final double y2, final double z2){
        return manhattan(Location.locToBlock(x1), Location.locToBlock(y1), Location.locToBlock(z1), Location.locToBlock(x2), Location.locToBlock(y2), Location.locToBlock(z2));
    }

    /**
     * Maximum distance comparing dx, dy, dz.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the int
     */
    public static int maxDistance(final int x1, final int y1, final int  z1, final int x2, final int y2, final int z2){
        return Math.max(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.abs(z1 - z2));
    }

    /**
     * Maximum distance comparing dx, dy, dz.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return the double
     */
    public static double maxDistance(final double x1, final double y1, final double  z1, final double x2, final double y2, final double z2){
        return Math.max(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)), Math.abs(z1 - z2));
    }

    /**
     * Check if the x-z plane move is "any backwards" regarding the yaw
     * direction.
     *
     * @param xDistance
     *            the x distance
     * @param zDistance
     *            the z distance
     * @param yaw
     *            the yaw
     * @return true, if is moving backwards
     */
    public static boolean isMovingBackwards(final double xDistance, final double zDistance, final float yaw) {
        return xDistance < 0D && zDistance > 0D && yaw > 180F && yaw < 270F
                || xDistance < 0D && zDistance < 0D && yaw > 270F && yaw < 360F 
                || xDistance > 0D && zDistance < 0D && yaw > 0F && yaw < 90F 
                || xDistance > 0D && zDistance > 0D && yaw > 90F && yaw < 180F;
    }

    /**
     * Compare position and looking direction.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePosAndLook(final Location loc1, final Location loc2) {
        return isSamePos(loc1, loc2) && loc1.getPitch() == loc2.getPitch() && loc1.getYaw() == loc2.getYaw();
    }

    /**
     * Test if both locations have the exact same coordinates. Does not check
     * yaw/pitch.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePos(final Location loc1, final Location loc2) {
        return loc1 != null && loc2 != null 
                && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ() && loc1.getY() == loc2.getY();
    }

    /**
     * Compare position and looking direction.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePosAndLook(final IGetPositionWithLook loc1, final Location loc2) {
        return isSamePos(loc1, loc2) && loc1.getPitch() == loc2.getPitch() && loc1.getYaw() == loc2.getYaw();
    }

    /**
     * Test if both locations have the exact same coordinates. Does not check
     * yaw/pitch.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePos(final IGetPosition loc1, final Location loc2) {
        return loc1 != null && loc2 != null 
                && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ() && loc1.getY() == loc2.getY();
    }

    /**
     * Compare position and looking direction.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePosAndLook(final IGetPositionWithLook loc1, final IGetPositionWithLook loc2) {
        return isSamePos(loc1, loc2) && loc1.getPitch() == loc2.getPitch() && loc1.getYaw() == loc2.getYaw();
    }

    /**
     * Test if both locations have the exact same coordinates. Does not check
     * yaw/pitch.
     *
     * @param loc1
     *            the loc1
     * @param loc2
     *            the loc2
     * @return Returns false if either is null.
     */
    public static boolean isSamePos(final IGetPosition loc1, final IGetPosition loc2) {
        return loc1 != null && loc2 != null 
                && loc1.getX() == loc2.getX() && loc1.getZ() == loc2.getZ() && loc1.getY() == loc2.getY();
    }

    /**
     * Test if the coordinates represent the same position.
     *
     * @param loc
     *            the loc
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return Returns false if loc is null;
     */
    public static boolean isSamePos(final Location loc, final double x, final double y, final double z) {
        if (loc == null) {
            return false;
        }
        return loc.getX() == x && loc.getZ() == z && loc.getY() == y;
    }

    /**
     * Test if the coordinates represent the same position.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return true, if is same pos
     */
    public static boolean isSamePos(final double x1, final double y1, final double z1, final double x2, final double y2, final double z2){
        return x1 == x2 && y1 == y2 && z1 == z2;
    }

    /**
     * Test if the coordinates represent the same position (2D).
     *
     * @param x1
     *            the x1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param z2
     *            the z2
     * @return true, if is same pos
     */
    public static boolean isSamePos(final double x1, final double z1, final double x2, final double z2){
        return x1 == x2 && z1 == z2;
    }

    /**
     * Test if the given double-coordinates are on the same block as specified
     * by the int-coordinates.
     *
     * @param loc
     *            the loc
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is same block
     */
    public static boolean isSameBlock(final Location loc, final double x, final double y, final double z) {
        if (loc == null) {
            return false;
        }
        return loc.getBlockX() == Location.locToBlock(x) && loc.getBlockZ() == Location.locToBlock(z) && loc.getBlockY() == Location.locToBlock(y);
    }

    /**
     * Checks if is same block.
     *
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @return true, if is same block
     */
    public static boolean isSameBlock(final int x1, final int y1, final int z1, final double x2, final double y2, final double z2) {
        return x1 == Location.locToBlock(x2) && z1 == Location.locToBlock(z2) && y1 == Location.locToBlock(y2);
    }

    /**
     * Check if the block has the same coordinates.
     * 
     * @param x
     * @param y
     * @param z
     * @param block
     * @return
     */
    public static boolean isSameBlock(int x, int y, int z, Block block) {
        return x == block.getX() && y == block.getY() && z == block.getZ();
    }

}
