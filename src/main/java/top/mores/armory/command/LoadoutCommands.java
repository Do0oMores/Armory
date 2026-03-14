package top.mores.armory.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.mores.armory.config.LoadoutCatalogConfigLoader;
import top.mores.armory.data.PlayerUnlockData;
import top.mores.armory.data.PlayerUnlockStore;
import top.mores.armory.service.LoadoutSyncService;

public final class LoadoutCommands {
    private LoadoutCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("taczloadout")
                .then(Commands.literal("reloadconfig")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(ctx -> {
                            LoadoutCatalogConfigLoader.LoadResult result =
                                    LoadoutCatalogConfigLoader.reload();

                            LoadoutSyncService.syncAllPlayers(ctx.getSource().getServer());

                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("军械库配置已热重载: guns="
                                            + result.gunCount()
                                            + ", attachments="
                                            + result.attachmentCount()
                                            + ", file="
                                            + result.path()), true);

                            if (!result.warnings().isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal(
                                        "重载完成，但有 " + result.warnings().size() + " 条警告，请查看控制台日志"
                                ));
                            }
                            return 1;
                        }))
                .then(Commands.literal("unlockgun")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    if (ctx.getSource().getPlayer() == null) return 0;

                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                    PlayerUnlockData data = PlayerUnlockStore.get(ctx.getSource().getPlayer());
                                    data.unlockedGuns().add(id);
                                    PlayerUnlockStore.save(ctx.getSource().getPlayer(), data);
                                    LoadoutSyncService.syncAll(ctx.getSource().getPlayer());

                                    ctx.getSource().sendSuccess(() -> Component.literal("已解锁枪械: " + id), true);
                                    return 1;
                                })))
                .then(Commands.literal("unlockattachment")
                        .requires(cs -> cs.hasPermission(2))
                        .then(Commands.argument("id", ResourceLocationArgument.id())
                                .executes(ctx -> {
                                    if (ctx.getSource().getPlayer() == null) return 0;

                                    ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                    PlayerUnlockData data = PlayerUnlockStore.get(ctx.getSource().getPlayer());
                                    data.unlockedAttachments().add(id);
                                    PlayerUnlockStore.save(ctx.getSource().getPlayer(), data);
                                    LoadoutSyncService.syncAll(ctx.getSource().getPlayer());

                                    ctx.getSource().sendSuccess(() -> Component.literal("已解锁配件: " + id), true);
                                    return 1;
                                })))
                .then(Commands.literal("sync")
                        .executes(ctx -> {
                            if (ctx.getSource().getPlayer() == null) return 0;
                            LoadoutSyncService.syncAll(ctx.getSource().getPlayer());
                            ctx.getSource().sendSuccess(() -> Component.literal("已同步军械库数据"), false);
                            return 1;
                        })));
    }
}