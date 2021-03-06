package team.creative.enhancedvisuals.common.handler;

import java.awt.Color;
import java.util.ArrayList;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.premade.IntMinMax;
import com.creativemd.creativecore.common.config.premade.curve.DecimalCurve;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.event.FireParticlesEvent;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.api.type.VisualTypeParticle;
import team.creative.enhancedvisuals.client.VisualManager;
import team.creative.enhancedvisuals.common.packet.DamagePacket;

public class DamageHandler extends VisualHandler {
	
	public static final ArrayList<Item> sharpList = new ArrayList<>();
	public static final ArrayList<Item> bluntList = new ArrayList<>();
	public static final ArrayList<Item> pierceList = new ArrayList<>();
	
	static {
		sharpList.add(Items.IRON_SWORD);
		sharpList.add(Items.WOODEN_SWORD);
		sharpList.add(Items.STONE_SWORD);
		sharpList.add(Items.DIAMOND_SWORD);
		sharpList.add(Items.GOLDEN_SWORD);
		sharpList.add(Items.IRON_AXE);
		sharpList.add(Items.WOODEN_AXE);
		sharpList.add(Items.STONE_AXE);
		sharpList.add(Items.DIAMOND_AXE);
		sharpList.add(Items.GOLDEN_AXE);
		
		bluntList.add(Items.IRON_PICKAXE);
		bluntList.add(Items.WOODEN_PICKAXE);
		bluntList.add(Items.STONE_PICKAXE);
		bluntList.add(Items.DIAMOND_PICKAXE);
		bluntList.add(Items.GOLDEN_PICKAXE);
		bluntList.add(Items.IRON_SHOVEL);
		bluntList.add(Items.WOODEN_SHOVEL);
		bluntList.add(Items.STONE_SHOVEL);
		bluntList.add(Items.DIAMOND_SHOVEL);
		bluntList.add(Items.GOLDEN_SHOVEL);
		
		pierceList.add(Items.IRON_HOE);
		pierceList.add(Items.WOODEN_HOE);
		pierceList.add(Items.STONE_HOE);
		pierceList.add(Items.DIAMOND_HOE);
		pierceList.add(Items.GOLDEN_HOE);
		pierceList.add(Items.ARROW);
	}
	
	public VisualType splatter = new VisualTypeParticle("splatter", 1);
	public VisualType impact = new VisualTypeParticle("impact", 1);
	public VisualType slash = new VisualTypeParticle("slash", 1);
	public VisualType pierce = new VisualTypeParticle("pierce", 1);
	@CreativeConfig
	public IntMinMax bloodDuration = new IntMinMax(500, 1500);
	
	public static VisualType fire = new VisualTypeParticle("fire", 1);
	@CreativeConfig
	public int fireSplashes = 1;
	@CreativeConfig
	public IntMinMax fireDuration = new IntMinMax(100, 1000);
	
	@CreativeConfig
	public int drownSplashes = 4;
	@CreativeConfig
	public IntMinMax drownDuration = new IntMinMax(10, 15);
	public VisualType waterDrown = new VisualTypeParticle("water", 1);
	
	public static Color bloodColor = new Color(0.3F, 0.01F, 0.01F, 0.7F);
	
	public void playerDamaged(EntityPlayer player, DamagePacket packet) {
		
		if (packet.source == EnhancedDamageSource.ATTACKER) {
			if (packet.attackerClass.contains("arrow"))
				createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
			if (packet.stack != null) {
				if (isSharp(packet.stack))
					createVisualFromDamageAndDistance(slash, packet.damage, player, bloodDuration);
				else if (isBlunt(packet.stack))
					createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
				else if (isPierce(packet.stack))
					createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
				else
					createVisualFromDamageAndDistance(splatter, packet.damage, player, bloodDuration);
			} else if (packet.attackerClass.contains("zombie") || packet.attackerClass.contains("skeleton") || packet.attackerClass.contains("ocelot"))
				createVisualFromDamageAndDistance(slash, packet.damage, player, bloodDuration);
			else if (packet.attackerClass.contains("golem") || packet.attackerClass.contains("player"))
				createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
			else if (packet.attackerClass.contains("wolf") || packet.attackerClass.contains("spider"))
				createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
		} else if (packet.source == EnhancedDamageSource.CACTUS)
			createVisualFromDamageAndDistance(pierce, packet.damage, player, bloodDuration);
		else if (packet.source == EnhancedDamageSource.FALL)
			createVisualFromDamageAndDistance(impact, packet.damage, player, bloodDuration);
		else if (packet.source == EnhancedDamageSource.DROWN)
			VisualManager.addParticlesFadeOut(waterDrown, drownSplashes, drownDuration, true);
		else if (packet.source == EnhancedDamageSource.FIRE) {
			FireParticlesEvent event = new FireParticlesEvent(fireSplashes, fireDuration.min, fireDuration.max);
			MinecraftForge.EVENT_BUS.post(event);
			VisualManager.addParticlesFadeOut(fire, event.getNewFireSplashes(), new IntMinMax(event.getNewFireDurationMin(), event.getNewFireDurationMax()), true);
		}
		
	}
	
	public static void createVisualFromDamageAndDistance(VisualType type, float damage, EntityPlayer player, IntMinMax duration) {
		if (damage <= 0.0F)
			return;
		
		float rate = 0.0F;
		float health = player.getHealth() - damage;
		if (health > 12.0F)
			rate = 1.0F;
		
		if ((health <= 12.0F) && (health > 8.0F))
			rate = 1.5F;
		
		if ((health <= 8.0F) && (health > 4.0F))
			rate = 2.0F;
		
		if ((health <= 4.0F) && (health > 0.0F))
			rate = 2.5F;
		
		if (health <= 0.0F)
			rate = 3.0F;
		
		VisualManager.addParticlesFadeOut(type, (int) (damage * rate), new DecimalCurve(0, 1, duration.next(VisualManager.rand), 0), true, bloodColor);
	}
	
	private static boolean isSharp(ItemStack item) {
		return sharpList.contains(item.getItem());
	}
	
	private static boolean isBlunt(ItemStack item) {
		return bluntList.contains(item.getItem());
	}
	
	private static boolean isPierce(ItemStack item) {
		return pierceList.contains(item.getItem());
	}
	
	public static enum EnhancedDamageSource {
		
		ATTACKER, CACTUS, FALL, DROWN, FIRE, UNKOWN;
		
	}
	
}
