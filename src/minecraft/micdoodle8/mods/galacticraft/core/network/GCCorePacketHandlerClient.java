package micdoodle8.mods.galacticraft.core.network;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import micdoodle8.mods.galacticraft.core.GCCoreConfigManager;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.ClientProxyCore;
import micdoodle8.mods.galacticraft.core.client.ClientProxyCore.GCKeyHandler;
import micdoodle8.mods.galacticraft.core.client.GCCorePlayerSP;
import micdoodle8.mods.galacticraft.core.client.fx.GCCoreEntityWeldingSmoke;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreGuiChoosePlanet;
import micdoodle8.mods.galacticraft.core.client.gui.GCCoreGuiGalaxyMap;
import micdoodle8.mods.galacticraft.core.dimension.GCCoreSpaceStationData;
import micdoodle8.mods.galacticraft.core.entities.EntitySpaceshipBase;
import micdoodle8.mods.galacticraft.core.tick.GCCoreTickHandlerClient;
import micdoodle8.mods.galacticraft.core.util.PacketUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.moon.GCMoonConfigManager;
import micdoodle8.mods.galacticraft.moon.client.ClientProxyMoon.TickHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.DimensionManager;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class GCCorePacketHandlerClient implements IPacketHandler
{
	Minecraft mc = FMLClientHandler.instance().getClient();

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player p)
	{
        final DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));

        final int packetType = PacketUtil.readPacketID(data);

        final EntityPlayer player = (EntityPlayer)p;

        GCCorePlayerSP playerBaseClient = null;

        if (player != null && GalacticraftCore.playersClient.size() > 0)
        {
        	playerBaseClient = PlayerUtil.getPlayerBaseClientFromPlayer(player);
        }

        if (packetType == 0)
        {
            final Class[] decodeAs = {Integer.class, Integer.class, String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            if (String.valueOf(packetReadout[2]).equals(String.valueOf(FMLClientHandler.instance().getClient().thePlayer.username)))
            {
                GCCoreTickHandlerClient.airRemaining = (Integer) packetReadout[0];
                GCCoreTickHandlerClient.airRemaining2 = (Integer) packetReadout[1];
            }
        }
        else if (packetType == 1)
        {
            final Class[] decodeAs = {Float.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            FMLClientHandler.instance().getClient().thePlayer.timeInPortal = (Float) packetReadout[0];
        }
        else if (packetType == 2)
        {
        	final Class[] decodeAs = {String.class, String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            if (String.valueOf(packetReadout[0]).equals(FMLClientHandler.instance().getClient().thePlayer.username))
            {
            	final String[] destinations = ((String)packetReadout[1]).split("\\.");

        		if (FMLClientHandler.instance().getClient().theWorld != null && !(FMLClientHandler.instance().getClient().currentScreen instanceof GCCoreGuiChoosePlanet || FMLClientHandler.instance().getClient().currentScreen instanceof GCCoreGuiGalaxyMap))
        		{
        			FMLClientHandler.instance().getClient().displayGuiScreen(new GCCoreGuiChoosePlanet(FMLClientHandler.instance().getClient().thePlayer, destinations));
        		}
        		else if (FMLClientHandler.instance().getClient().currentScreen instanceof GCCoreGuiChoosePlanet)
        		{
        			((GCCoreGuiChoosePlanet) FMLClientHandler.instance().getClient().currentScreen).updateDimensionList(destinations);
        		}
            }
        }
        else if (packetType == 3)
        {
        }
        else if (packetType == 4)
        {
        	final Class[] decodeAs = {String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            ClientProxyCore.playersUsingParachutes.add((String) packetReadout[0]);
        }
        else if (packetType == 5)
        {
        	final Class[] decodeAs = {String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            ClientProxyCore.playersUsingParachutes.remove(packetReadout[0]);
        }
        else if (packetType == 6)
        {
        	final Class[] decodeAs = {String.class, String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            ClientProxyCore.parachuteTextures.put((String)packetReadout[0], (String)packetReadout[1]);
        }
        else if (packetType == 7)
        {
        	final Class[] decodeAs = {String.class, String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            ClientProxyCore.parachuteTextures.remove(packetReadout[0]);
        }
        else if (packetType == 8)
        {
        	final Class[] decodeAs = {String.class};
            PacketUtil.readPacketData(data, decodeAs);

            if (playerBaseClient != null)
            {
            	playerBaseClient.setThirdPersonView(FMLClientHandler.instance().getClient().gameSettings.thirdPersonView);
            }

            FMLClientHandler.instance().getClient().gameSettings.thirdPersonView = 1;

        	player.sendChatToPlayer("SPACE - Launch");
        	player.sendChatToPlayer("A / D  - Turn left-right");
        	player.sendChatToPlayer("W / S  - Turn up-down");
        	player.sendChatToPlayer(Keyboard.getKeyName(GCKeyHandler.openSpaceshipInv.keyCode) + "       - Inventory / Fuel");
        }
        else if (packetType == 9)
        {
        	final Class[] decodeAs = {Integer.class, Integer.class, Integer.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            int x, y, z;
            x = (Integer) packetReadout[0];
            y = (Integer) packetReadout[1];
            z = (Integer) packetReadout[2];

        	for (int i = 0; i < 4; i++)
        	{
                if (this.mc != null && this.mc.renderViewEntity != null && this.mc.effectRenderer != null && this.mc.theWorld != null)
                {
            		final EntityFX fx = new GCCoreEntityWeldingSmoke(this.mc.theWorld, x - 0.15 + 0.5, y + 1.2, z + 0.15 + 0.5, this.mc.theWorld.rand.nextDouble() / 20 - this.mc.theWorld.rand.nextDouble() / 20, 0.06, this.mc.theWorld.rand.nextDouble() / 20 - this.mc.theWorld.rand.nextDouble() / 20, 1.0F);
            		if (fx != null)
            		{
                    	this.mc.effectRenderer.addEffect(fx);
            		}
                }
        	}
        }
        else if (packetType == 10)
        {
        	final Class[] decodeAs = {String.class, Integer.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            final int type = (Integer) packetReadout[1];

            switch (type)
            {
            case 0:
            	ClientProxyCore.playersWithOxygenMask.add((String) packetReadout[0]);
            	break;
            case 1:
            	ClientProxyCore.playersWithOxygenMask.remove(packetReadout[0]);
            	break;
            case 2:
            	ClientProxyCore.playersWithOxygenGear.add((String) packetReadout[0]);
            	break;
            case 3:
            	ClientProxyCore.playersWithOxygenGear.remove(packetReadout[0]);
            	break;
            case 4:
            	ClientProxyCore.playersWithOxygenTankLeftRed.add((String) packetReadout[0]);
            	break;
            case 5:
            	ClientProxyCore.playersWithOxygenTankLeftRed.remove(packetReadout[0]);
            	break;
            case 6:
            	ClientProxyCore.playersWithOxygenTankLeftOrange.add((String) packetReadout[0]);
            	break;
            case 7:
            	ClientProxyCore.playersWithOxygenTankLeftOrange.remove(packetReadout[0]);
            	break;
            case 8:
            	ClientProxyCore.playersWithOxygenTankLeftGreen.add((String) packetReadout[0]);
            	break;
            case 9:
            	ClientProxyCore.playersWithOxygenTankLeftGreen.remove(packetReadout[0]);
            	break;
            case 10:
            	ClientProxyCore.playersWithOxygenTankRightRed.add((String) packetReadout[0]);
            	break;
            case 11:
            	ClientProxyCore.playersWithOxygenTankRightRed.remove(packetReadout[0]);
            	break;
            case 12:
            	ClientProxyCore.playersWithOxygenTankRightOrange.add((String) packetReadout[0]);
            	break;
            case 13:
            	ClientProxyCore.playersWithOxygenTankRightOrange.remove(packetReadout[0]);
            	break;
            case 14:
            	ClientProxyCore.playersWithOxygenTankRightGreen.add((String) packetReadout[0]);
            	break;
            case 15:
            	ClientProxyCore.playersWithOxygenTankRightGreen.remove(packetReadout[0]);
            	break;
            }
        }
        else if (packetType == 11)
        {
            final Class[] decodeAs = {String.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

            this.mc.thePlayer.cloakUrl = (String) packetReadout[0];
        }
        else if (packetType == 12)
        {
            final Class[] decodeAs = {String.class};
            PacketUtil.readPacketData(data, decodeAs);

            FMLClientHandler.instance().getClient().displayGuiScreen(null);
        }
        else if (packetType == 13)
        {
            final Class[] decodeAs = {String.class};
            PacketUtil.readPacketData(data, decodeAs);

            if (playerBaseClient != null)
            {
                FMLClientHandler.instance().getClient().gameSettings.thirdPersonView = playerBaseClient.getThirdPersonView();
            }
        }
        else if (packetType == 14)
        {
            try
            {
	    		new GCCorePacketEntityUpdate().handlePacket(data, new Object[] {player}, Side.SERVER);
            }
            catch(Exception e)
            {
            	e.printStackTrace();
            }
        }
        else if (packetType == 15)
        {
            final Class[] decodeAs = {Integer.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);
            
        	if (player.ridingEntity != null && player.ridingEntity instanceof EntitySpaceshipBase)
        	{
        		((EntitySpaceshipBase) player.ridingEntity).fuel = (Integer) packetReadout[0];
        	}
        }
        else if (packetType == 16)
        {
            if (WorldUtil.registeredSpaceStations == null)
            {
            	WorldUtil.registeredSpaceStations = new ArrayList();
            }

			try 
			{
				int var1 = data.readInt();

                for (int var2 = 0; var2 < var1; ++var2)
                {
                    int var3 = data.readInt();

                    if (!WorldUtil.registeredSpaceStations.contains(Integer.valueOf(var3)))
                    {
                    	WorldUtil.registeredSpaceStations.add(Integer.valueOf(var3));
                        DimensionManager.registerDimension(var3, GCCoreConfigManager.idDimensionOverworldOrbit);
                    }
                }
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
        }
        else if (packetType == 17)
        {
            try
            {
                int var2 = data.readInt();
                NBTTagCompound var3;

                short var1 = data.readShort();

                if (var1 < 0)
                {
                	var3 = null;
                }
                else
                {
                    byte[] var21 = new byte[var1];
                    data.readFully(var21);
                    var3 = CompressedStreamTools.decompress(var21);
                }
                
                GCCoreSpaceStationData var4 = GCCoreSpaceStationData.getMPSpaceStationData(player.worldObj, var2, player);
                var4.readFromNBT(var3);
            }
            catch (IOException var5)
            {
                var5.printStackTrace();
            }
        }
        else if (packetType == 18)
        {
            final Class[] decodeAs = {Integer.class};
            final Object[] packetReadout = PacketUtil.readPacketData(data, decodeAs);

    		ClientProxyCore.clientSpaceStationID = (Integer) packetReadout[0];
        }
        else if (packetType == 19)
        {
            if (WorldUtil.registeredPlanets == null)
            {
            	WorldUtil.registeredPlanets = new ArrayList();
            }

			try 
			{
				int var1 = data.readInt();

                for (int var2 = 0; var2 < var1; ++var2)
                {
                    int var3 = data.readInt();

                    if (!WorldUtil.registeredPlanets.contains(Integer.valueOf(var3)))
                    {
                    	WorldUtil.registeredPlanets.add(Integer.valueOf(var3));
                        DimensionManager.registerDimension(var3, GCMoonConfigManager.dimensionIDMoon);
                    }
                }
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
        }
	}
}