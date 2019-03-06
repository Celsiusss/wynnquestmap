package celsiuss.wynnquestmap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod(modid = WynnQuestMap.MODID, name = WynnQuestMap.NAME, version = WynnQuestMap.VERSION)
public class WynnQuestMap {
    public static final String MODID = "wynnquestmap";
    public static final String NAME = "WynnQuestMap";
    public static final String VERSION = "0.0.1";

    private static Logger logger;

    private boolean isGuiOpen = false;
    private boolean isOnWynn = false;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        // some example code
        logger.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void clientChat(ClientChatEvent event) {
        if (!event.getMessage().equals("log")) {
            return;
        }
        event.setCanceled(true);
        String displayName = Minecraft.getMinecraft().player.inventory.getStackInSlot(7).getDisplayName();
        String NBT = Minecraft.getMinecraft().player.inventory.getStackInSlot(7).getTagCompound().toString();
        logger.info(displayName);
        logger.info(NBT);
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(displayName));
        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(NBT));
        //Function<String> sendMessage = EntityPlayer::sendMessage;
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

        List<ItemStack> books = new ArrayList<>();
        for (Slot slot : inventory) {
            if (slot.getStack().getUnlocalizedName().equals("item.book")
                || slot.getStack().getUnlocalizedName().equals("item.writingBook")) {
                if (!slot.getStack().getDisplayName().contains("Quests")) {
                    books.add(slot.getStack());
                }
            }
        }
        for (ItemStack book : books) {
            logger.info(book.getDisplayName());
        }
        logger.info(books);

        //Minecraft.getMinecraft().player.sendMessage(new TextComponentString(Integer.toString(event.getGuiContainer().inventorySlots.windowId)));
    }

    @SubscribeEvent
    public void guiOpen(GuiOpenEvent event) {
        if (event.getGui() == null) {
            this.isGuiOpen = false;
            logger.info("closed gui");
        }
    }

    @SubscribeEvent
    public void clientChatRecieved(ClientChatReceivedEvent event) {
        if (event.getMessage().toString().contains("Thank you for using the WynnPack.")) {
            this.isOnWynn = true;
        }
    }

    @SubscribeEvent
    public void network(FMLNetworkEvent event) {
        if (event.getType().equals(NetHandlerPlayClient.class)) {
            logger.info("yes");
            logger.info(event.getManager().getRemoteAddress());
        } else {
            logger.info("no");
        }
        logger.info(event.getType().toString());
        logger.info(event.getManager().isChannelOpen());
    }
}
