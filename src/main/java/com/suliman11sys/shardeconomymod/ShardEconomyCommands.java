package com.suliman11sys.shardeconomymod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ShardEconomyCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bal").executes(ShardEconomyCommands::balance));
        dispatcher.register(literal("pay").then(argument("player", EntityArgumentType.player()).then(argument("amount", LongArgumentType.longArg(1)).executes(ShardEconomyCommands::pay))));
        dispatcher.register(literal("sell").executes(ShardEconomyCommands::sell));
        dispatcher.register(literal("buy").then(argument("amount", LongArgumentType.longArg(1)).executes(ShardEconomyCommands::buyDiamonds)));
        dispatcher.register(literal("rules").executes(ShardEconomyCommands::rules));
    }

    private static int balance(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            long balance = ShardEconomyMod.getBalance(player);
            String formattedBalance = String.format(",d", balance);
            player.sendMessage(Text.translatable("command.shardeconomymod.bal", formattedBalance), false);
        }
        return 1;
    }

    private static int pay(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity sender = context.getSource().getPlayer();
        ServerPlayerEntity receiver = EntityArgumentType.getPlayer(context, "player");
        long amount = LongArgumentType.getLong(context, "amount");

        if (sender != null && receiver != null) {
            if (ShardEconomyMod.subtractBalance(sender, amount)) {
                ShardEconomyMod.addBalance(receiver, amount);
                String formattedAmount = String.format(",d", amount);
                sender.sendMessage(Text.translatable("command.shardeconomymod.pay", formattedAmount, receiver.getName().getString()), false);
            } else {
                sender.sendMessage(Text.literal("Not enough Shards!"), false);
            }
        }
        return 1;
    }

    private static int sell(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            ItemStack heldItem = player.getMainHandStack();
            if (!heldItem.isEmpty()) {
                int count = heldItem.getCount();
                long value = count * 10L; // Example value, adjust as needed
                ShardEconomyMod.addBalance(player, value);
                heldItem.setCount(0);
                String formattedValue = String.format(",d", value);
                player.sendMessage(Text.translatable("command.shardeconomymod.sell", count, formattedValue), false);
            } else {
                player.sendMessage(Text.literal("You are not holding any item to sell!"), false);
            }
        }
        return 1;
    }

    private static int buyDiamonds(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        long amount = LongArgumentType.getLong(context, "amount");
        long totalCost = amount * ShardEconomyMod.getDiamondPrice();

        if (player != null) {
            if (ShardEconomyMod.subtractBalance(player, totalCost)) {
                ItemStack diamonds = new ItemStack(Items.DIAMOND, (int) amount);
                player.giveItemStack(diamonds);
                String formattedCost = String.format(",d", totalCost);
                player.sendMessage(Text.translatable("command.shardeconomymod.buy", amount, formattedCost), false);
            } else {
                player.sendMessage(Text.literal("Not enough Shards!"), false);
            }
        }
        return 1;
    }

    private static int rules(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            player.sendMessage(Text.translatable("command.shardeconomymod.rules"), false);
            // Add more rule details as needed
        }
        return 1;
    }
}