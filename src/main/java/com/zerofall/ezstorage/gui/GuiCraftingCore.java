package com.zerofall.ezstorage.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.Reference;
import com.zerofall.ezstorage.configuration.EZConfiguration;
import com.zerofall.ezstorage.container.ContainerStorageCoreCrafting;
import com.zerofall.ezstorage.network.client.MsgClearCraftingGrid;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiCheckBox;

public class GuiCraftingCore extends GuiStorageCore {

    protected GuiButtonExt btnClearCraftingPanel;
    protected GuiCheckBox chkDamageablePutAll;

    public GuiCraftingCore(EntityPlayer player, World world, int x, int y, int z) {
        super(new ContainerStorageCoreCrafting(player, world));
        this.xSize = 195;
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        btnClearCraftingPanel = new GuiButtonExt(10, guiLeft + 99, guiTop + 114, 8, 8, "");
        chkDamageablePutAll = new GuiCheckBox(15, guiLeft + 8, guiTop + 114, "D", EZConfiguration.damageablePutAll);
        buttonList.add(btnClearCraftingPanel);
        buttonList.add(chkDamageablePutAll);
    }

    @Override
    public int rowsVisible() {
        return 5;
    }

    @Override
    protected ResourceLocation getBackground() {
        return new ResourceLocation(Reference.MOD_ID, "textures/gui/storageCraftingGui.png");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button == btnClearCraftingPanel) {
            EZStorage.instance.network.sendToServer(new MsgClearCraftingGrid());
        } else if (button == chkDamageablePutAll) {
            EZConfiguration.damageablePutAll = chkDamageablePutAll.isChecked();
            EZConfiguration.save();
        }
    }
}
