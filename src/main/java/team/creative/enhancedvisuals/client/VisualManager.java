package team.creative.enhancedvisuals.client;

import java.awt.Color;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.config.premade.IntMinMax;
import com.creativemd.creativecore.common.config.premade.curve.Curve;
import com.creativemd.creativecore.common.config.premade.curve.DecimalCurve;
import com.creativemd.creativecore.common.utils.type.HashMapList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import team.creative.enhancedvisuals.EnhancedVisuals;
import team.creative.enhancedvisuals.api.Particle;
import team.creative.enhancedvisuals.api.Visual;
import team.creative.enhancedvisuals.api.VisualCategory;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.common.event.EVEvents;
import team.creative.enhancedvisuals.common.visual.VisualRegistry;

public class VisualManager {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	public static Random rand = new Random();
	private static HashMapList<VisualCategory, Visual> visuals = new HashMapList<>();
	
	public static void onTick(@Nullable EntityPlayer player) {
		boolean areEyesInWater = player != null && EVEvents.areEyesInWater(player);
		
		synchronized (visuals) {
			for (Iterator<Visual> iterator = visuals.iterator(); iterator.hasNext();) {
				Visual visual = iterator.next();
				
				int factor = 1;
				if (areEyesInWater && visual.isAffectedByWater())
					factor = EnhancedVisuals.CONFIG.waterSubstractFactor;
				
				for (int i = 0; i < factor; i++) {
					if (!visual.tick()) {
						visual.removeFromDisplay();
						iterator.remove();
						break;
					}
				}
			}
			
			for (VisualHandler handler : VisualRegistry.handlers()) {
				if (handler.isEnabled(player))
					handler.tick(player);
			}
		}
		
		if (player != null && !player.isEntityAlive())
			VisualManager.clearParticles();
	}
	
	public static Collection<Visual> visuals(VisualCategory category) {
		return visuals.getValues(category);
	}
	
	public static void clearParticles() {
		synchronized (visuals) {
			visuals.removeKey(VisualCategory.particle);
		}
	}
	
	public static void add(Visual visual) {
		visual.addToDisplay();
		visuals.add(visual.getCategory(), visual);
	}
	
	public static Visual addVisualFadeOut(VisualType vt, IntMinMax time) {
		return addVisualFadeOut(vt, new DecimalCurve(0, 1, time.next(rand), 0));
	}
	
	public static Visual addVisualFadeOut(VisualType vt, int time) {
		return addVisualFadeOut(vt, new DecimalCurve(0, 1, time, 0));
	}
	
	public static Visual addVisualFadeOut(VisualType vt, Curve curve) {
		Visual v = new Visual(vt, curve, vt.getVariantAmount() > 1 ? rand.nextInt(vt.getVariantAmount()) : 0);
		add(v);
		return v;
	}
	
	public static void addParticlesFadeOut(VisualType vt, int count, IntMinMax time, boolean rotate) {
		addParticlesFadeOut(vt, count, new DecimalCurve(0, 1, time.next(rand), 0), rotate, null);
	}
	
	public static void addParticlesFadeOut(VisualType vt, int count, int time, boolean rotate) {
		addParticlesFadeOut(vt, count, new DecimalCurve(0, 1, time, 0), rotate, null);
	}
	
	public static void addParticlesFadeOut(VisualType vt, int count, Curve curve, boolean rotate, Color color) {
		for (int i = 0; i < count; i++) {
			ScaledResolution resolution = new ScaledResolution(mc);
			int screenWidth = resolution.getScaledWidth();
			int screenHeight = resolution.getScaledHeight();
			
			int width = vt.getWidth(screenWidth);
			int height = vt.getHeight(screenHeight);
			
			Particle particle = new Particle(vt, curve, generateOffset(rand, screenWidth, width), generateOffset(rand, screenHeight, height), width, height, rotate ? rand.nextFloat() * 360 : 0, rand.nextInt(vt.getVariantAmount()));
			particle.color = color;
			add(particle);
		}
	}
	
	public static int generateOffset(Random rand, int dimensionLength, int spacingBuffer) {
		int half = dimensionLength / 2;
		float multiplier = (float) (1 - Math.pow(rand.nextDouble(), 2));
		float textureCenterPosition = rand.nextInt(2) == 0 ? half + half * multiplier : half - half * multiplier;
		return (int) (textureCenterPosition - (spacingBuffer / 2.0F));
	}
	
}
