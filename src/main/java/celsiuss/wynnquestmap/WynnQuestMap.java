package celsiuss.wynnquestmap;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(modid = WynnQuestMap.MODID, name = WynnQuestMap.NAME, version = WynnQuestMap.VERSION)
public class WynnQuestMap {
    public static final String MODID = "wynnquestmap";
    public static final String NAME = "WynnQuestMap";
    public static final String VERSION = "0.0.1";

    private static Logger logger;

    private boolean isGuiOpen = false;
    private boolean isOnWynn = false;

    private List<ItemStack> questBooks;
    public static Quests quests = new Quests();

    private HTTPServer server;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void guiContainer(GuiContainerEvent event) {
        if (this.isGuiOpen) {
            return;
        }
        this.isGuiOpen = true;
        Container container = event.getGuiContainer().inventorySlots;
        if (!(container instanceof ContainerChest)) {
            return;
        }
        ContainerChest containerChest = (ContainerChest) container;
        String containerTitle = containerChest.getLowerChestInventory().getDisplayName().getFormattedText();

        if (!containerTitle.contains("Quest")) {
            return;
        }

        logger.info(containerChest.inventorySlots.size());
        List<Slot> inventory = containerChest.inventorySlots.subList(0, containerChest.inventorySlots.size() - 36);
        logger.info(inventory.size());
        List<ItemStack> books = new ArrayList<>();
        int failed = 0;
        for (Slot slot : inventory) {
            ItemStack item = slot.getStack();

            logger.info(item.getItem().iteml);
            if (item.getUnlocalizedName().equals("item.book")
                    || item.getUnlocalizedName().equals("item.writingBook")) {
                logger.info(item.getTagCompound());
                if (item.getTagCompound() == null) {
                    logger.info(Integer.toString(failed));
                    failed++;
                    continue;
                }
                NBTTagCompound NBTItem = item.getTagCompound();
                if (!item.getDisplayName().contains("Quests")
                        && NBTItem.hasKey("display")
                        && NBTItem.getCompoundTag("display").hasKey("Lore")) {
                    Iterator iterator = NBTItem.getCompoundTag("display").getTagList("Lore", 8).iterator();
                    List<NBTBase> NBTLore = Lists.newArrayList(iterator);
                    if (NBTLore.size() >= 5) {
                        books.add(slot.getStack());
                    }
                }
            }
        }
        if (failed > 0) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("Failed to get NBT from " + failed + " items"));
        }

        Pattern coordsPattern = Pattern.compile("\\[-?\\d*(, ?\\d*)?, ?-?\\d*\\]");

        this.questBooks = books;
        quests.clear();
        for (ItemStack book : books) {
            Iterator iterator = book.getTagCompound().getCompoundTag("display").getTagList("Lore", 8).iterator();
            List<NBTBase> NBTLore = Lists.newArrayList(iterator);

            StringBuilder descriptionBuilder = new StringBuilder();
            for (NBTBase line : NBTLore.subList(5, NBTLore.size() - 1)) {
                String newLine = line.toString().replace("\"", "").trim();
                logger.info(newLine);
                descriptionBuilder.append(" ");
                descriptionBuilder.append(newLine);
            }
            String description = descriptionBuilder.toString();
            Matcher matcher = coordsPattern.matcher(description);

            boolean started = NBTLore.get(1).toString().contains("Started...");
            if (matcher.find()) {
                logger.info(book.getDisplayName());
                quests.add(new Quest(
                        ChatFormatting.stripFormatting(book.getDisplayName()),
                        started,
                        matcher.group(0),
                        ChatFormatting.stripFormatting(description)
                ));
            }
        }
        logger.info(books);
        logger.info(quests.toString());
    }

    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event) {
        if (event.getGui() == null) {
            this.isGuiOpen = false;
        }
    }

    @SubscribeEvent
    public void network(FMLNetworkEvent event) {
        if (event.getType().equals(NetHandlerPlayClient.class) && event.getManager().getRemoteAddress().toString().contains("wynncraft.com")) {
            logger.info("Connected to Wynncraft");

            if (this.server == null) {
                this.server = new HTTPServer(9090);
                new Thread(server).start();
                logger.info("Starting local server");
            }

        } else {
            if (this.server != null) {
                this.server.stop();
                this.server = null;
            }
        }
    }
}
