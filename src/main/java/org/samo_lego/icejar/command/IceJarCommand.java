package org.samo_lego.icejar.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import org.samo_lego.icejar.IceJar;

import static net.minecraft.commands.Commands.literal;

public class IceJarCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralCommandNode<CommandSourceStack> edit = literal("edit").build();
        IceJar.getInstance().getConfig().generateCommand(edit);
        dispatcher.register(literal("icejar")
                .requires(source -> source.hasPermission(4))
                .then(literal("config")
                        .then(edit)
                        .then(literal("reload")
                                .executes(IceJarCommand::reloadConfig)
                        )
                )
        );
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        IceJar.getInstance().getConfig().reload(IceJar.getInstance().getConfigFile());
        context.getSource().sendSuccess(new TranslatableComponent("gui.done").withStyle(ChatFormatting.GREEN), true);

        return 1;
    }
}
