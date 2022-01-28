package org.samo_lego.icejar.check.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import org.samo_lego.icejar.check.CheckType;

import java.util.HashSet;
import java.util.Set;

public class AutoSign extends BlockCheck {

    /* From NCP */
    /** Fastest time "possible" estimate for an empty sign. */
    private static final long minEditTime = 150;
    /** Minimum time needed to add one extra line (not the first). */
    private static final long minLineTime = 50;
    /** Minimum time needed to type a character. */
    private static final long minCharTime = 50;

    private long lastPlacedSign;
    final Set<Character> chars = new HashSet<>(15 * 4);

    public AutoSign(ServerPlayer player) {
        super(CheckType.WORLD_BLOCK_AUTOSIGN, player);
    }

    @Override
    public boolean checkBlockAction(final Level level, final InteractionHand hand, final BlockPos blockPos, final Direction direction) {
        Item item = this.player.getItemInHand(hand).getItem();

        if (item instanceof SignItem) {
            this.lastPlacedSign = System.currentTimeMillis();
        }

        return true;
    }

    public boolean allowPlace(ServerboundSignUpdatePacket packet) {
        final long now = System.currentTimeMillis();
        final long editTime = now - this.lastPlacedSign;

        final String[] lines = packet.getLines();
        long expected = getExpectedEditTime(lines);

        if (expected > editTime) {
            if (this.increaseCheatAttempts() > this.getMaxAttemptsBeforeFlag())
                this.flag();

            return false;
        }
        this.decreaseCheatAttempts();
        return true;
    }


    /**
     * Gets expected sign edit time depending on lines.
     * Taken from NCP.
     *
     * @param lines sign lines.
     * @return expected time.
     */
    private long getExpectedEditTime(final String[] lines) {
        long expected = minEditTime;
        int n = 0;
        for (String line : lines){
            if (line != null){
                line = line.trim().toLowerCase();
                if (!line.isEmpty()){
                    chars.clear();
                    n += 1;
                    for (final char c : line.toCharArray()){
                        chars.add(c);
                    }
                    expected += minCharTime * chars.size();
                }
            }
        }
        if (n == 0) {
            return 0;
        }
        if (n > 1){
            expected += minLineTime * n;
        }
        return expected;
    }
}
