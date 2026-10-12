package com.suliman11sys.shardeconomymod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ShardEconomyMod implements ModInitializer {
    public static final String MOD_ID = "shardeconomymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Item RULEBOOK = new Item(new Item.Settings());
    private static final Map<ServerPlayerEntity, Long> playerBalances = new HashMap<>();
    private static final long STARTING_BALANCE = 1000L;
    private static final long DIAMOND_PRICE = 100L;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Shard Economy Mod");
        
        // Register the rulebook item
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "rulebook"), RULEBOOK);
        
        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ShardEconomyCommands.register(dispatcher);
        });
        
        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Server started, initializing economy");
        // Give new players starting balance and rulebook
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            server.getPlayerManager().getPlayerList().forEach(player -> {
                if (!playerBalances.containsKey(player)) {
                    playerBalances.put(player, STARTING_BALANCE);
                    player.giveItemStack(new ItemStack(RULEBOOK));
                }
            });
        });
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Server stopping, saving economy data");
        // Save player balances to disk or database
    }

    public static void addBalance(ServerPlayerEntity player, long amount) {
        playerBalances.put(player, playerBalances.getOrDefault(player, 0L) + amount);
    }

    public static boolean subtractBalance(ServerPlayerEntity player, long amount) {
        long currentBalance = playerBalances.getOrDefault(player, 0L);
        if (currentBalance >= amount) {
            playerBalances.put(player, currentBalance - amount);
            return true;
        }
        return false;
    }

    public static long getBalance(ServerPlayerEntity player) {
        return playerBalances.getOrDefault(player, 0L);
    }

    public static void sendMessage(ServerPlayerEntity player, String message) {
        player.sendMessage(Text.literal(message), false);
    }

    public static long getDiamondPrice() {
        return DIAMOND_PRICE;
    }
}