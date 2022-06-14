package org.samo_lego.icejar.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.samo_lego.icejar.IceJar;

import static net.minecraft.commands.Commands.literal;

public class IceJarCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        LiteralCommandNode<CommandSourceStack> edit = literal("edit").build();
        IceJar.getInstance().getConfig().generateCommand(edit);
        dispatcher.register(literal("icejar")
                .requires(source -> source.hasPermission(4))
                .then(literal("config")
                        .then(edit)
                        .then(literal("reload")
                                .executes(IceJarCommand::reloadConfig)
                        )
                        .then(literal("save")
                                .executes(IceJarCommand::saveConfig)
                        )
                )
        );
    }

    private static int saveConfig(CommandContext<CommandSourceStack> context) {
        IceJar.getInstance().getConfig().save();
        context.getSource().sendSuccess(Component.translatable("gui.done").withStyle(ChatFormatting.GREEN), true);

        return 1;
    }


    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        IceJar.getInstance().getConfig().reload(IceJar.getInstance().getConfigFile());
        context.getSource().sendSuccess(Component.translatable("gui.done").withStyle(ChatFormatting.GREEN), true);

        return 1;
    }
}
