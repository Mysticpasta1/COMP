package com.mystic.comp.mixin;

import com.mystic.comp.Config;
import info.u_team.overworld_mirror.init.OverworldMirrorBlocks;
import info.u_team.overworld_mirror.portal.PortalManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.List;

@Mixin(PortalManager.class)
public class PortalManagerMixin {

    /**
     * Replaces the original flower+frame check so that:
     *  1) the frame must still be stone bricks
     *  2) each of the 3×3 “flower” positions must be an instanceof FlowerBlock
     *  3) AND its registry name must appear in ServerConfig.flowerWhitelist
     * @author Mysticpasta1
     * @reason No Config Yet Officially
     */
    @Overwrite(remap = false)
    private static boolean validatePortalFrameAndSpawnPortal(Level level, BlockPos pos) {
        // collect the 3×3 inner “flower” positions
        List<BlockPos> flowers = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                flowers.add(pos.offset(dx, 0, dz));
            }
        }

        // collect the 12 frame positions two blocks out
        List<BlockPos> frame = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            frame.add(pos.offset( 2, 0,  i));
            frame.add(pos.offset(-2, 0,  i));
            frame.add(pos.offset( i, 0,  2));
            frame.add(pos.offset( i, 0, -2));
        }

        // check the stone‐brick frame exactly as before
        boolean frameMatching = frame.stream()
                .allMatch(p -> level.getBlockState(p).is(Blocks.STONE_BRICKS));

        // pull your whitelist once
        List<? extends String> whitelist = Config.COMMON.flowerWhitelist.get().stream().toList();

        // now enforce instanceof FlowerBlock AND registry‐name whitelist
        boolean flowersMatching = true;
        for (BlockPos flowerPos : flowers) {
            var state = level.getBlockState(flowerPos);
            var block = state.getBlock();
            if (!(block instanceof FlowerBlock)) {
                flowersMatching = false;
                break;
            }
            // get its registry key, e.g. "minecraft:poppy"
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id == null || !whitelist.contains(id.toString())) {
                flowersMatching = false;
                break;
            }
        }

        // if both checks pass, place the portal blocks and broadcast
        if (frameMatching && flowersMatching) {
            @SuppressWarnings("resource")
            var portalBlock = OverworldMirrorBlocks.PORTAL.get().defaultBlockState();
            flowers.forEach(p -> level.setBlock(p, portalBlock, 2));

            PlayerList players = ((ServerLevel) level).getServer().getPlayerList();
            for (BlockPos p : flowers) {
                players.broadcast(
                        (Player) null,
                        p.getX(), p.getY(), p.getZ(),
                        64f,
                        level.dimension(),
                        new ClientboundBlockUpdatePacket(level, p)
                );
            }
            return true;
        }

        return false;
    }
}
