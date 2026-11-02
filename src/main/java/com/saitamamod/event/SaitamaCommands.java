package com.saitamamod.event;

import com.mojang.brigadier.CommandDispatcher;
import com.saitamamod.SaitamaConfig;
import com.saitamamod.SaitamaData;
import com.saitamamod.SaitamaMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** /saitama on|off|reset|status [jogador] — para testar sem fazer o treino inteiro. */
@Mod.EventBusSubscriber(modid = SaitamaMod.MODID)
public final class SaitamaCommands {
    private SaitamaCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent e) {
        CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
        d.register(Commands.literal("saitama").requires(s -> s.hasPermission(2))
                .then(Commands.literal("on")
                        .executes(c -> on(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> on(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("off")
                        .executes(c -> off(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> off(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("reset")
                        .executes(c -> reset(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> reset(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                .then(Commands.literal("status")
                        .executes(c -> status(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> status(c.getSource(), EntityArgument.getPlayer(c, "player"))))));
    }

    private static int on(CommandSourceStack s, ServerPlayer p) {
        SaitamaData.setSaitama(p, true);
        s.sendSuccess(() -> Component.literal(p.getGameProfile().getName() + " agora é o Saitama."), true);
        return 1;
    }

    private static int off(CommandSourceStack s, ServerPlayer p) {
        SaitamaData.setSaitama(p, false);
        s.sendSuccess(() -> Component.literal(p.getGameProfile().getName() + " não é mais o Saitama."), true);
        return 1;
    }

    private static int reset(CommandSourceStack s, ServerPlayer p) {
        SaitamaData.reset(p);
        s.sendSuccess(() -> Component.literal("Treino de " + p.getGameProfile().getName() + " zerado."), true);
        return 1;
    }

    private static int status(CommandSourceStack s, ServerPlayer p) {
        String msg = "Flexões " + SaitamaData.getInt(p, SaitamaData.PUSHUPS) + "/" + SaitamaConfig.PUSHUPS.get()
                + " | Abdominais " + SaitamaData.getInt(p, SaitamaData.SITUPS) + "/" + SaitamaConfig.SITUPS.get()
                + " | Agachamentos " + SaitamaData.getInt(p, SaitamaData.SQUATS) + "/" + SaitamaConfig.SQUATS.get()
                + " | Corrida " + SaitamaData.getInt(p, SaitamaData.RUN) + "/" + SaitamaConfig.RUN_METERS.get() + " m"
                + " | Saitama: " + (SaitamaData.isSaitama(p) ? "sim" : "não");
        s.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }
}
