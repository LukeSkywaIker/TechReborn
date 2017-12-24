package techreborn.client.gui.slot;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import reborncore.client.gui.GuiUtil;
import techreborn.client.container.builder.BuiltContainer;
import techreborn.client.gui.GuiBase;
import techreborn.client.gui.slot.elements.ConfigSlotElement;
import techreborn.client.gui.slot.elements.ElementBase;
import techreborn.client.gui.slot.elements.SlotType;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSlotConfiguration {

	static HashMap<Integer, ConfigSlotElement> slotElementMap = new HashMap<>();

	public static int slectedSlot = 0;

	public static void reset() {
		slectedSlot = -1;
	}

	public static void init(GuiBase guiBase) {
		reset();
		slotElementMap.clear();

		BuiltContainer container = guiBase.container;
		for (Slot slot : container.inventorySlots) {
			if (guiBase.tile != slot.inventory) {
				continue;
			}
			ConfigSlotElement slotElement = new ConfigSlotElement(guiBase.getMachine(), slot.getSlotIndex(), SlotType.NORMAL, slot.xPos - guiBase.guiLeft + 50, slot.yPos - guiBase.guiTop - 25, guiBase);
			slotElementMap.put(slot.getSlotIndex(), slotElement);
		}

	}

	public static void draw(GuiBase guiBase, int mouseX, int mouseY) {
		BuiltContainer container = guiBase.container;
		for (Slot slot : container.inventorySlots) {
			if (guiBase.tile != slot.inventory) {
				continue;
			}
			GlStateManager.color(255, 0, 0);
			Color color = new Color(255, 0, 0, 128);
			GuiUtil.drawGradientRect(slot.xPos - 1, slot.yPos - 1, 18, 18, color.getRGB(), color.getRGB());
			GlStateManager.color(255, 255, 255);
		}

		if (slectedSlot != -1) {
			slotElementMap.get(slectedSlot).draw(guiBase);
		}
	}

	public static List<ConfigSlotElement> getVisibleElements() {
		if(slectedSlot == -1){
			return Collections.emptyList();
		}
		return slotElementMap.values().stream()
			.filter(configSlotElement -> configSlotElement.getId() == slectedSlot)
			.collect(Collectors.toList());
	}

	//Allows closing of the widget with the escape key
	@SubscribeEvent
	public static void keyboardEvent(GuiScreenEvent.KeyboardInputEvent event){
		if(!getVisibleElements().isEmpty() && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE){
			slectedSlot = -1;
			event.setCanceled(true);
		}
	}

	public static boolean mouseClicked(int mouseX, int mouseY, int mouseButton, GuiBase guiBase) throws IOException {
		if (mouseButton == 0) {
			for (ConfigSlotElement configSlotElement : getVisibleElements()) {
				for (ElementBase element : configSlotElement.elements) {
					if (element.isInRect(guiBase, element.x, element.y, element.getWidth(guiBase.getMachine()), element.getHeight(guiBase.getMachine()), mouseX, mouseY)) {
						element.isPressing = true;
						boolean action = element.onStartPress(guiBase.getMachine(), guiBase, mouseX, mouseY);
						for (ElementBase e : getVisibleElements()) {
							if (e != element) {
								e.isPressing = false;
							}
						}
						if (action)
							break;
					} else {
						element.isPressing = false;
					}
				}
			}
		}
		BuiltContainer container = guiBase.container;

		if(getVisibleElements().isEmpty()) {
			for (Slot slot : container.inventorySlots) {
				if (guiBase.tile != slot.inventory) {
					continue;
				}
				if (guiBase.isPointInRect(slot.xPos, slot.yPos, 18, 18, mouseX, mouseY)) {
					slectedSlot = slot.getSlotIndex();
					return true;
				}
			}
		}
		return !getVisibleElements().isEmpty();
	}

	public static void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick, GuiBase guiBase) {
		if (mouseButton == 0) {
			for (ConfigSlotElement configSlotElement : getVisibleElements()) {
				for (ElementBase element : configSlotElement.elements) {
					if (element.isInRect(guiBase, element.x, element.y, element.getWidth(guiBase.getMachine()), element.getHeight(guiBase.getMachine()), mouseX, mouseY)) {
						element.isDragging = true;
						boolean action = element.onDrag(guiBase.getMachine(), guiBase, mouseX, mouseY);
						for (ElementBase e : getVisibleElements()) {
							if (e != element) {
								e.isDragging = false;
							}
						}
						if (action)
							break;
					} else {
						element.isDragging = false;
					}
				}
			}
		}
	}

	public static boolean mouseReleased(int mouseX, int mouseY, int mouseButton, GuiBase guiBase) {
		boolean clicked = false;
		if (mouseButton == 0) {
			for (ConfigSlotElement configSlotElement : getVisibleElements()) {
				if (configSlotElement.isInRect(guiBase, configSlotElement.x, configSlotElement.y, configSlotElement.getWidth(guiBase.getMachine()), configSlotElement.getHeight(guiBase.getMachine()), mouseX, mouseY)) {
					clicked = true;
				}
				for (ElementBase element : Lists.reverse(configSlotElement.elements)) {
					if (element.isInRect(guiBase, element.x, element.y, element.getWidth(guiBase.getMachine()), element.getHeight(guiBase.getMachine()), mouseX, mouseY)) {
						element.isReleasing = true;
						boolean action = element.onRelease(guiBase.getMachine(), guiBase, mouseX, mouseY);
						for (ElementBase e : getVisibleElements()) {
							if (e != element) {
								e.isReleasing = false;
							}
						}
						if (action)
							clicked = true;
						break;
					} else {
						element.isReleasing = false;
					}
				}
			}
		}
		return clicked;
	}

}
