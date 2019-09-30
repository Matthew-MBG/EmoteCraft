package net.teamfruit.emojicord.compat;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.teamfruit.emojicord.CoreInvoke;

public class Compat {
	public static class CompatMinecraft {
		private final Minecraft mc;

		public CompatMinecraft(final Minecraft mc) {
			this.mc = mc;
		}

		public Minecraft getMinecraftObj() {
			return this.mc;
		}

		public static @Nonnull CompatMinecraft getMinecraft() {
			return new CompatMinecraft(FMLClientHandler.instance().getClient());
		}

		public @Nonnull CompatFontRenderer getFontRenderer() {
			return new CompatFontRenderer(this.mc.fontRendererObj);
		}

		public @Nullable CompatWorld getWorld() {
			final World world = this.mc.theWorld;
			if (world!=null)
				return new CompatWorld(world);
			return null;
		}

		public @Nullable CompatEntityPlayer getPlayer() {
			final EntityPlayer player = this.mc.thePlayer;
			if (player!=null)
				return new CompatEntityPlayer(player);
			return null;
		}

		public @Nonnull CompatGameSettings getSettings() {
			return new CompatGameSettings(this.mc.gameSettings);
		}

		public @Nullable CompatNetHandlerPlayClient getConnection() {
			final NetHandlerPlayClient connection = this.mc.getNetHandler();
			return connection!=null ? new CompatNetHandlerPlayClient(connection) : null;
		}

		public TextureManager getTextureManager() {
			return this.mc.getTextureManager();
		}

		public File getGameDir() {
			return this.mc.mcDataDir;
		}
	}

	public static class CompatFontRenderer {
		private final FontRenderer font;

		public CompatFontRenderer(final FontRenderer font) {
			this.font = font;
		}

		public int drawString(final String msg, final float x, final float y, final int color, final boolean shadow) {
			return this.font.drawString(msg, x, y, color, shadow);
		}

		public int drawString(final String msg, final float x, final float y, final int color) {
			return drawString(msg, x, y, color, false);
		}

		public int drawStringWithShadow(final String msg, final float x, final float y, final int color) {
			return drawString(msg, x, y, color, true);
		}

		public int getStringWidth(final @Nullable String s) {
			return this.font.getStringWidth(s);
		}

		public int getStringWidthWithoutFormattingCodes(final @Nullable String s) {
			return getStringWidth(EnumChatFormatting.getTextWithoutFormattingCodes(s));
		}

		public FontRenderer getFontRendererObj() {
			return this.font;
		}
	}

	public static class CompatGameSettings {
		private final GameSettings settings;

		public CompatGameSettings(final GameSettings settings) {
			this.settings = settings;
		}

		public GameSettings getSettingsObj() {
			return this.settings;
		}

		public int getAnisotropicFiltering() {
			return 0;
		}
	}

	public static class CompatBlockPos {
		public final @Nonnull BlockPos pos;

		public CompatBlockPos(final @Nonnull BlockPos pos) {
			Validate.notNull(pos, "MovePos needs position");
			this.pos = pos;
		}

		public int getX() {
			return this.pos.getX();
		}

		public int getY() {
			return this.pos.getY();
		}

		public int getZ() {
			return this.pos.getZ();
		}

		public @Nonnull CompatBlockPos offset(final CompatEnumFacing facing) {
			if (facing==null)
				return this;
			return fromCoords(getX()+facing.offsetX, getY()+facing.offsetY, getZ()+facing.offsetZ);
		}

		public static @Nonnull CompatBlockPos fromCoords(final int xCoord, final int yCoord, final int zCoord) {
			return new CompatBlockPos(new BlockPos(xCoord, yCoord, zCoord));
		}

		public static @Nonnull CompatBlockPos getTileEntityPos(@Nonnull final TileEntity tile) {
			return new CompatBlockPos(tile.getPos());
		}

		public void setTileEntityPos(@Nonnull final TileEntity tile) {
			tile.setPos(this.pos);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime*result+this.pos.hashCode();
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this==obj)
				return true;
			if (obj==null)
				return false;
			if (!(obj instanceof CompatBlockPos))
				return false;
			final CompatBlockPos other = (CompatBlockPos) obj;
			if (!this.pos.equals(other.pos))
				return false;
			return true;
		}
	}

	public static class CompatSoundHandler {
		public static void playSound(final @Nonnull ResourceLocation location, final float volume) {
			CompatMinecraft.getMinecraft().getMinecraftObj().getSoundHandler().playSound(PositionedSoundRecord.create(location, volume));
		}
	}

	public static class CompatKeyRegistrar {
		public static void registerKeyBinding(final KeyBinding key) {
			ClientRegistry.registerKeyBinding(key);
		}
	}

	public static abstract class CompatTileEntitySignRenderer extends TileEntitySignRenderer {
		public void renderBaseTileEntityAt(final @Nullable TileEntitySign tile, final double x, final double y, final double z, final float partialTicks, final int destroy, final float alpha) {
			super.renderTileEntityAt(tile, x, y, z, partialTicks, destroy);
		}

		public abstract void renderTileEntityAtCompat(final @Nullable TileEntitySign tile, final double x, final double y, final double z, final float partialTicks, final int destroy, final float alpha);

		@Override
		public void renderTileEntityAt(final @Nullable TileEntitySign tile, final double x, final double y, final double z, final float partialTicks, final int destroy) {
			renderTileEntityAtCompat(tile, x, y, z, partialTicks, destroy, 1f);
		}
	}

	public static class CompatTileEntityRendererDispatcher {
		public static void renderTileEntityAt(final @Nullable TileEntitySign tile, final double x, final double y, final double z, final float partialTicks, final int destroy, final float alpha) {
			TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, x, y, z, partialTicks, destroy);
		}
	}

	public static class CompatEntityPlayer {
		private final EntityPlayer player;

		public CompatEntityPlayer(@Nonnull final EntityPlayer player) {
			this.player = player;
		}

		public EntityPlayer getPlayerObj() {
			return this.player;
		}

		public @Nullable ItemStack getHeldItemMainhand() {
			return this.player.getCurrentEquippedItem();
		}

		public @Nullable ItemStack getHeldItemOffhand() {
			return null;
		}
	}

	public static class CompatWorld {
		private final World world;

		public CompatWorld(@Nonnull final World world) {
			this.world = world;
		}

		public World getWorldObj() {
			return this.world;
		}

		public int getLightFor(final CompatBlockPos pos) {
			return this.world.getLightFor(EnumSkyBlock.SKY, pos.pos);
		}

		public CompatBlockState getBlockState(final CompatBlockPos pos) {
			return new CompatBlockState(this.world.getBlockState(pos.pos));
		}

		public @Nullable TileEntity getTileEntity(final CompatBlockPos pos) {
			return this.world.getTileEntity(pos.pos);
		}
	}

	public static class CompatBlockState {
		private final IBlockState blockstate;

		public CompatBlockState(final IBlockState blockstate) {
			this.blockstate = blockstate;
		}

		public IBlockState getBlockStateObj() {
			return this.blockstate;
		}

		public CompatBlock getBlock() {
			return new CompatBlock(this.blockstate.getBlock());
		}

		public Material getMaterial() {
			return this.blockstate.getBlock().getMaterial();
		}
	}

	public static class CompatBlock {
		private final Block block;

		public CompatBlock(final Block block) {
			this.block = block;
		}

		public Block getBlockObj() {
			return this.block;
		}

		public boolean canPlaceBlockAt(final CompatWorld world, final CompatBlockPos pos) {
			return this.block.canPlaceBlockAt(world.getWorldObj(), pos.pos);
		}
	}

	public static class CompatBlocks {
		public static final Block STANDING_SIGN = Blocks.standing_sign;
		public static final Block WALL_SIGN = Blocks.wall_sign;
	}

	public static class CompatItems {
		public static final Item SIGN = Items.sign;
	}

	public static class CompatGuiNewChat {
		public static int getChatWidth(final GuiNewChat chat) {
			return chat.getChatWidth();
		}

		public static float getChatScale(final GuiNewChat chat) {
			return chat.getChatScale();
		}
	}

	public static class CompatTextComponent {
		public static CompatTextComponent blank = fromText("");

		public final IChatComponent component;

		public CompatTextComponent(final IChatComponent component) {
			this.component = component;
		}

		public @Nonnull List<CompatClickEvent> getLinksFromChat() {
			final List<CompatClickEvent> list = Lists.newLinkedList();
			getLinksFromChat0(list, this.component);
			return list;
		}

		private void getLinksFromChat0(final @Nonnull List<CompatClickEvent> list, final @Nonnull IChatComponent pchat) {
			final List<?> chats = pchat.getSiblings();
			for (final Object o : chats) {
				final IChatComponent chat = (IChatComponent) o;
				final ClickEvent ev = chat.getChatStyle().getChatClickEvent();
				if (ev!=null&&ev.getAction()==ClickEvent.Action.OPEN_URL)
					list.add(new CompatClickEvent(ev));
				getLinksFromChat0(list, chat);
			}
		}

		public CompatTextComponent setChatStyle(final CompatTextStyle style) {
			this.component.setChatStyle(style.style);
			return this;
		}

		public String getUnformattedText() {
			return this.component.getUnformattedText();
		}

		public static CompatTextComponent jsonToComponent(final String json) {
			return new CompatTextComponent(IChatComponent.Serializer.jsonToComponent(json));
		}

		public static CompatTextComponent fromText(final String text) {
			return new CompatTextComponent(new ChatComponentText(text));
		}

		public static CompatTextComponent fromTranslation(final String text, final Object... params) {
			return new CompatTextComponent(new ChatComponentTranslation(text, params));
		}

		public void sendClient() {
			CompatMinecraft.getMinecraft().getMinecraftObj().ingameGUI.getChatGUI().printChatMessage(this.component);
		}

		public void sendClientWithId(final int id) {
			CompatMinecraft.getMinecraft().getMinecraftObj().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(this.component, id);
		}

		public void sendPlayer(final @Nonnull ICommandSender target) {
			target.addChatMessage(this.component);
		}
	}

	public static class CompatClickEvent {
		private final ClickEvent event;

		public CompatClickEvent(final ClickEvent event) {
			this.event = event;
		}

		public String getValue() {
			return this.event.getValue();
		}

		public static CompatClickEvent create(final CompatAction action, final String text) {
			return new CompatClickEvent(new ClickEvent(action.action, text));
		}

		public static enum CompatAction {
			OPEN_URL(ClickEvent.Action.OPEN_URL),
			OPEN_FILE(ClickEvent.Action.OPEN_FILE),
			RUN_COMMAND(ClickEvent.Action.RUN_COMMAND),
			SUGGEST_COMMAND(ClickEvent.Action.SUGGEST_COMMAND),
			;

			public final ClickEvent.Action action;

			private CompatAction(final ClickEvent.Action action) {
				this.action = action;
			}
		}
	}

	public static class CompatHoverEvent {
		public final HoverEvent event;

		public CompatHoverEvent(final HoverEvent event) {
			this.event = event;
		}

		public static CompatHoverEvent create(final CompatAction action, final CompatTextComponent text) {
			return new CompatHoverEvent(new HoverEvent(action.action, text.component));
		}

		public static enum CompatAction {
			SHOW_TEXT(HoverEvent.Action.SHOW_TEXT),
			SHOW_ITEM(HoverEvent.Action.SHOW_ITEM),
			;

			public final HoverEvent.Action action;

			private CompatAction(final HoverEvent.Action action) {
				this.action = action;
			}
		}
	}

	public static class CompatTextStyle {
		public final ChatStyle style;

		public CompatTextStyle(final ChatStyle style) {
			this.style = style;
		}

		public CompatTextStyle setColor(final CompatTextFormatting format) {
			this.style.setColor(format.format);
			return this;
		}

		public static CompatTextStyle create() {
			return new CompatTextStyle(new ChatStyle());
		}

		public CompatTextStyle setChatHoverEvent(final CompatHoverEvent event) {
			this.style.setChatHoverEvent(event.event);
			return this;
		}

		public CompatTextStyle setChatClickEvent(final CompatClickEvent event) {
			this.style.setChatClickEvent(event.event);
			return this;
		}
	}

	public static class CompatChatLine {
		public static CompatTextComponent getChatComponent(final ChatLine line) {
			return new CompatTextComponent(line.getChatComponent());
		}
	}

	public static enum CompatTextFormatting {
		BLACK(EnumChatFormatting.BLACK),
		DARK_BLUE(EnumChatFormatting.DARK_BLUE),
		DARK_GREEN(EnumChatFormatting.DARK_GREEN),
		DARK_AQUA(EnumChatFormatting.DARK_AQUA),
		DARK_RED(EnumChatFormatting.DARK_RED),
		DARK_PURPLE(EnumChatFormatting.DARK_PURPLE),
		GOLD(EnumChatFormatting.GOLD),
		GRAY(EnumChatFormatting.GRAY),
		DARK_GRAY(EnumChatFormatting.DARK_GRAY),
		BLUE(EnumChatFormatting.BLUE),
		GREEN(EnumChatFormatting.GREEN),
		AQUA(EnumChatFormatting.AQUA),
		RED(EnumChatFormatting.RED),
		LIGHT_PURPLE(EnumChatFormatting.LIGHT_PURPLE),
		YELLOW(EnumChatFormatting.YELLOW),
		WHITE(EnumChatFormatting.WHITE),
		OBFUSCATED(EnumChatFormatting.OBFUSCATED),
		BOLD(EnumChatFormatting.BOLD),
		STRIKETHROUGH(EnumChatFormatting.STRIKETHROUGH),
		UNDERLINE(EnumChatFormatting.UNDERLINE),
		ITALIC(EnumChatFormatting.ITALIC),
		RESET(EnumChatFormatting.RESET),
		;

		public final EnumChatFormatting format;

		private CompatTextFormatting(final EnumChatFormatting format) {
			this.format = format;
		}

		@Override
		public String toString() {
			return this.format.toString();
		}
	}

	public static class CompatSimpleNetworkWrapper {
		private final SimpleNetworkWrapper network;

		public CompatSimpleNetworkWrapper(final SimpleNetworkWrapper network) {
			this.network = network;
		}

		public CompatSimpleNetworkWrapper(final Object network) {
			this((SimpleNetworkWrapper) network);
		}

		public void sendToServer(final CompatMessage message) {
			this.network.sendToServer(message.message);
		}
	}

	public static class CompatMessage {
		public final IMessage message;

		public CompatMessage(final IMessage message) {
			this.message = message;
		}

		public CompatMessage(final Object message) {
			this((IMessage) message);
		}
	}

	public static class CompatNetHandlerPlayClient {
		private final NetHandlerPlayClient connection;

		public CompatNetHandlerPlayClient(final NetHandlerPlayClient connection) {
			this.connection = connection;
		}

		public void sendPacket(final CompatPacket packet) {
			this.connection.addToSendQueue(packet.packet);
		}
	}

	public static class CompatPacket {
		public final Packet<? extends INetHandler> packet;

		public CompatPacket(final Packet<? extends INetHandler> packet) {
			this.packet = packet;
		}
	}

	public static class CompatC12PacketUpdateSign extends CompatPacket {
		public CompatC12PacketUpdateSign(final C12PacketUpdateSign packet) {
			super(packet);
		}

		public static CompatC12PacketUpdateSign create(final CompatBlockPos pos, final List<CompatTextComponent> clines) {
			final List<IChatComponent> lines = Lists.transform(clines, input -> {
				return input==null ? null : input.component;
			});
			return new CompatC12PacketUpdateSign(new C12PacketUpdateSign(pos.pos, lines.toArray(new IChatComponent[lines.size()])));
		}
	}

	public static class CompatC17PacketCustomPayload extends CompatPacket {
		public CompatC17PacketCustomPayload(final C17PacketCustomPayload packet) {
			super(packet);
		}

		public static CompatC17PacketCustomPayload create(final String channel, final String data) {
			return new CompatC17PacketCustomPayload(new C17PacketCustomPayload(channel, new PacketBuffer(Unpooled.buffer()).writeString(data)));
		}
	}

	public static class CompatTileEntitySign {
		public static List<CompatTextComponent> getSignText(final TileEntitySign tile) {
			return Lists.transform(Lists.newArrayList(tile.signText), t -> new CompatTextComponent(t));
		}

		public static void setSignText(final TileEntitySign tile, final List<CompatTextComponent> clines) {
			final List<IChatComponent> lines = Lists.transform(clines, t -> t==null ? null : t.component);
			final Iterator<IChatComponent> itr = lines.iterator();
			for (int i = 0; i<tile.signText.length; i++)
				tile.signText[i] = itr.hasNext() ? itr.next() : CompatTextComponent.blank.component;
		}
	}

	public static enum CompatEnumFacing {
		DOWN(0, -1, 0, EnumFacing.DOWN),
		UP(0, 1, 0, EnumFacing.UP),
		NORTH(0, 0, -1, EnumFacing.NORTH),
		SOUTH(0, 0, 1, EnumFacing.SOUTH),
		WEST(-1, 0, 0, EnumFacing.WEST),
		EAST(1, 0, 0, EnumFacing.EAST);

		public final int offsetX;
		public final int offsetY;
		public final int offsetZ;
		private final EnumFacing facing;

		private CompatEnumFacing(final int offsetX, final int offsetY, final int offsetZ, final EnumFacing facing) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.offsetZ = offsetZ;
			this.facing = facing;
		}

		public int getIndex() {
			return ordinal();
		}

		public static @Nonnull CompatEnumFacing fromFacing(@Nonnull final EnumFacing facing) {
			for (final CompatEnumFacing cfacing : values())
				if (facing==cfacing.facing)
					return cfacing;
			return DOWN;
		}

		public static @Nonnull CompatEnumFacing fromFacingId(final int facing) {
			final CompatEnumFacing[] cfacings = values();
			if (0<=facing&&facing<cfacings.length)
				return cfacings[facing];
			return DOWN;
		}
	}

	public static class CompatTextureUtil {
		public static final DynamicTexture missingTexture = TextureUtil.missingTexture;

		public static void processPixelValues(final int[] pixel, final int displayWidth, final int displayHeight) {
			TextureUtil.processPixelValues(pixel, displayWidth, displayHeight);
		}

		public static void allocateTextureImpl(final int id, final int miplevel, final int width, final int height, final float anisotropicFiltering) {
			TextureUtil.allocateTextureImpl(id, miplevel, width, height);
		}
	}

	public static class CompatScreen {
		private GuiScreen screen;

		public CompatScreen(final GuiScreen screen) {
			this.screen = screen;
		}

		public CompatScreen(final CompatGuiConfig screen) {
			this.screen = screen;
		}

		public GuiScreen getScreenObj() {
			return this.screen;
		}
	}

	public static class CompatGuiConfig extends GuiConfig {
		public CompatGuiConfig(final CompatScreen parentScreen, final List<CompatConfigElement> configElements, final String modID, final boolean allRequireWorldRestart, final boolean allRequireMcRestart, final String title) {
			super(parentScreen.screen, CompatConfigElement.getConfigElements(configElements), modID, allRequireWorldRestart, allRequireMcRestart, GuiConfig.getAbridgedConfigPath(title));
		}
	}

	public static class CompatConfiguration {
		public final Configuration config;

		public CompatConfiguration(final Configuration config) {
			this.config = config;
		}

		public Set<String> getCategoryNames() {
			return this.config.getCategoryNames();
		}

		public CompatConfigCategory getCategory(final String category) {
			return new CompatConfigCategory(this.config.getCategory(category));
		}
	}

	public static class CompatConfigCategory {
		public final ConfigCategory category;

		public CompatConfigCategory(final ConfigCategory category) {
			this.category = category;
		}

		public boolean isChild() {
			return this.category.isChild();
		}
	}

	public static class CompatConfigProperty {
		public final Property property;

		public CompatConfigProperty(final Property property) {
			this.property = property;
		}
	}

	public static class CompatConfigElement {
		public final IConfigElement element;

		public CompatConfigElement(final IConfigElement element) {
			this.element = element;
		}

		public static List<IConfigElement> getConfigElements(final List<CompatConfigElement> elements) {
			return Lists.transform(elements, t -> t==null ? null : t.element);
		}

		public static CompatConfigElement fromCategory(final CompatConfigCategory category) {
			return new CompatConfigElement(new ConfigElement(category.category));
		}

		public static CompatConfigElement fromProperty(final CompatConfigProperty prop) {
			return new CompatConfigElement(new ConfigElement(prop.property));
		}
	}

	public static abstract class CompatModGuiFactory implements IModGuiFactory {
		@Override
		public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
			return null;
		}

		@Override
		public RuntimeOptionGuiHandler getHandlerFor(final RuntimeOptionCategoryElement element) {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public @Nullable Class<? extends GuiScreen> mainConfigGuiClass() {
			return (Class<? extends GuiScreen>) mainConfigGuiClassCompat();
		}

		public abstract @Nullable Class<?> mainConfigGuiClassCompat();

		public abstract CompatScreen createConfigGuiCompat(CompatScreen parentScreen);
	}

	public static class CompatCommand {
		public static @Nonnull String getCommandName(final ICommand command) {
			return command.getCommandName();
		}

		public static @Nullable List<String> getCommandAliases(final ICommand command) {
			return command.getCommandAliases();
		}

		public static @Nonnull String getCommandUsage(final ICommand command, final @Nullable ICommandSender sender) {
			return command.getCommandUsage(sender);
		}
	}

	public static class CompatCommandSender {
		public static boolean canCommandSenderUseCommand(final ICommandSender sender, final int level, final String name) {
			return sender.canCommandSenderUseCommand(level, name);
		}
	}

	public static abstract class CompatRootCommand extends CommandBase implements ICommand {
		@Override
		public @Nullable List<String> addTabCompletionOptions(final @Nullable ICommandSender sender, final @Nullable String[] args, final @Nullable BlockPos pos) {
			return addTabCompletionOptionCompat(sender, args);
		}

		public @Nullable abstract List<String> addTabCompletionOptionCompat(final @Nullable ICommandSender sender, final @Nullable String[] args);

		@Override
		public @Nonnull String getCommandName() {
			return getCommandNameCompat();
		}

		public abstract @Nonnull String getCommandNameCompat();

		@Override
		public @Nullable List<String> getCommandAliases() {
			return getCommandAliasesCompat();
		}

		public abstract @Nullable List<String> getCommandAliasesCompat();

		@Override
		public @Nonnull String getCommandUsage(final @Nullable ICommandSender sender) {
			return getCommandUsageCompat(sender);
		}

		public abstract @Nonnull String getCommandUsageCompat(final @Nullable ICommandSender sender);

		@Override
		public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
			processCommandCompat(sender, args);
		}

		public abstract void processCommandCompat(final @Nullable ICommandSender sender, final @Nullable String[] args) throws CommandException;
	}

	public static abstract class CompatSubCommand implements ICommand {
		@Override
		public @Nullable List<String> addTabCompletionOptions(final @Nullable ICommandSender sender, final @Nullable String[] args, final @Nullable BlockPos pos) {
			return addTabCompletionOptionCompat(sender, args);
		}

		public @Nullable abstract List<String> addTabCompletionOptionCompat(final @Nullable ICommandSender sender, final @Nullable String[] args);

		@Override
		public @Nonnull String getCommandName() {
			return getCommandNameCompat();
		}

		public abstract @Nonnull String getCommandNameCompat();

		@Override
		public @Nullable List<String> getCommandAliases() {
			return getCommandAliasesCompat();
		}

		public abstract @Nullable List<String> getCommandAliasesCompat();

		@Override
		public @Nonnull String getCommandUsage(final @Nullable ICommandSender sender) {
			return getCommandUsageCompat(sender);
		}

		public abstract @Nonnull String getCommandUsageCompat(final @Nullable ICommandSender sender);

		@Override
		public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
			processCommandCompat(sender, args);
		}

		public abstract void processCommandCompat(final @Nullable ICommandSender sender, final @Nullable String[] args) throws CommandException;

		@Override
		public boolean canCommandSenderUseCommand(final ICommandSender sender) {
			return canCommandSenderUseCommandCompat(sender);
		}

		public abstract boolean canCommandSenderUseCommandCompat(final @Nullable ICommandSender sender);

		public abstract int compare(final @Nullable ICommand command);

		@Override
		public int compareTo(final @Nullable ICommand command) {
			return compare(command);
		}
	}

	public static class CompatCommandBase {
		public static String buildString(final ICommandSender sender, final String[] args, final int startPos) {
			return CommandBase.buildString(args, startPos);
		}
	}

	public static class CompatMathHelper {
		public static int floor_float(final float value) {
			return MathHelper.floor_float(value);
		}

		public static int floor_double(final double value) {
			return MathHelper.floor_double(value);
		}
	}

	public static class CompatChatRender {
		public static abstract class CompatPicChatLine extends ChatLine {
			public static final @Nonnull CompatTextComponent dummytext = CompatTextComponent.fromText("");

			public CompatPicChatLine(final int updateCounterCreated, final int lineId) {
				super(updateCounterCreated, dummytext.component, lineId);
			}

			@CoreInvoke
			public @Nullable IChatComponent onClicked(final @Nonnull GuiNewChat chat, final int x) {
				final CompatTextComponent component = onClickedCompat(chat, x);
				if (component!=null)
					return component.component;
				return null;
			}

			public abstract @Nullable CompatTextComponent onClickedCompat(final @Nonnull GuiNewChat chat, final int x);
		}
	}

	public static class CompatI18n {
		public static String format(final String format, final Object... args) {
			return I18n.format(format, args);
		}

		public static String translateToLocal(final String text) {
			return StatCollector.translateToLocal(text);
		}
	}

	public static class CompatModel {
	}

	public static class CompatBakedModel {
		public final IBakedModel bakedModel;

		public CompatBakedModel(@Nonnull final IBakedModel bakedModel) {
			this.bakedModel = bakedModel;
		}
	}

	public static class CompatVertex {
		private static class CompatBaseVertexImpl implements CompatBaseVertex {
			public static final @Nonnull Tessellator t = Tessellator.getInstance();
			public static final @Nonnull WorldRenderer w = t.getWorldRenderer();

			public CompatBaseVertexImpl() {
			}

			@Override
			public void draw() {
				endVertex();
				t.draw();
			}

			@Override
			public @Nonnull CompatBaseVertex begin(final int mode) {
				w.begin(mode, DefaultVertexFormats.POSITION);
				init();
				return this;
			}

			@Override
			public @Nonnull CompatBaseVertex beginTexture(final int mode) {
				w.begin(mode, DefaultVertexFormats.POSITION_TEX);
				init();
				return this;
			}

			private void init() {
				this.stack = false;
			}

			private boolean stack;

			@Override
			public @Nonnull CompatBaseVertex pos(final double x, final double y, final double z) {
				endVertex();
				w.pos(x, y, z);
				this.stack = true;
				return this;
			}

			@Override
			public @Nonnull CompatBaseVertex tex(final double u, final double v) {
				w.tex(u, v);
				return this;
			}

			@Override
			public @Nonnull CompatBaseVertex color(final float red, final float green, final float blue, final float alpha) {
				return this.color((int) (red*255.0F), (int) (green*255.0F), (int) (blue*255.0F), (int) (alpha*255.0F));
			}

			@Override
			public @Nonnull CompatBaseVertex color(final int red, final int green, final int blue, final int alpha) {
				w.putColorRGBA(0, red, green, blue, alpha);
				return this;
			}

			@Override
			public @Nonnull CompatBaseVertex normal(final float nx, final float ny, final float nz) {
				w.normal(nx, ny, nz);
				return this;
			}

			@Override
			public void setTranslation(final double x, final double y, final double z) {
				w.setTranslation(x, y, z);
			}

			private void endVertex() {
				if (this.stack) {
					this.stack = false;
					w.endVertex();
				}
			}
		}

		public static @Nonnull CompatBaseVertex getTessellator() {
			return new CompatBaseVertexImpl();
		}
	}

	public static class CompatTexture {
		private final CompatSimpleTexture texture;

		public CompatTexture(final CompatSimpleTexture texture) {
			this.texture = texture;
		}

		public static CompatTexture getTexture(final CompatSimpleTexture texture) {
			return new CompatTexture(texture);
		}

		public CompatSimpleTexture getTextureObj() {
			return this.texture;
		}

		public void bindTexture() {
			OpenGL.glBindTexture(GL11.GL_TEXTURE_2D, this.texture.getGlTextureId());
		}

		public void uploadTexture(final InputStream image) throws IOException {
			this.texture.deleteGlTexture();

			final BufferedImage bufferedimage = TextureUtil.readBufferedImage(image);
			final boolean blur = true;
			final boolean clamp = false;

			TextureUtil.uploadTextureImageAllocate(this.texture.getRawGlTextureId(), bufferedimage, blur, clamp);
		}
	}

	public static class CompatResourceManager {
		private final IResourceManager manager;

		public CompatResourceManager(final IResourceManager manager) {
			this.manager = manager;
		}

		public IResourceManager getManagerObj() {
			return this.manager;
		}
	}

	public static class CompatSimpleTexture extends SimpleTexture {
		public CompatSimpleTexture(final ResourceLocation textureResourceLocation) {
			super(textureResourceLocation);
		}

		public int getRawGlTextureId() {
			return super.getGlTextureId();
		}

		@Override
		public void loadTexture(final IResourceManager manager) throws IOException {
			loadTexture(new CompatResourceManager(manager));
		}

		public void loadTexture(final CompatResourceManager manager) throws IOException {
			super.loadTexture(manager.getManagerObj());
		}
	}

	public enum CompatSide {
		COMMON,
		CLIENT,
		SERVER,
		;

		public Side toSide() {
			switch (this) {
				case CLIENT:
					return Side.CLIENT;
				case SERVER:
					return Side.SERVER;
				default:
					return Side.SERVER;
			}
		}

		public static CompatSide fromSide(final Side type) {
			switch (type) {
				case CLIENT:
					return CLIENT;
				case SERVER:
					return SERVER;
				default:
					return COMMON;
			}
		}
	}

	public static class CompatBufferBuilder {
		public CompatBufferBuilder() {
		}
	}

	public static abstract class CompatGlyph {
		public CompatGlyph(final ResourceLocation texture, final float width, final float height) {
		}

		public void onRender(final TextureManager textureManager, final boolean hasShadow, final float x, final float y, final CompatBufferBuilder vbuilder, final float red, final float green, final float blue, final float alpha) {
		}
	}
}