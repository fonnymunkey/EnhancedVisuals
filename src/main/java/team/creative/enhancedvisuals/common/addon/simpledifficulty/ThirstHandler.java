package team.creative.enhancedvisuals.common.addon.simpledifficulty;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.config.api.CreativeConfig;
import com.creativemd.creativecore.common.config.premade.IntMinMax;

import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import team.creative.enhancedvisuals.api.Visual;
import team.creative.enhancedvisuals.api.VisualHandler;
import team.creative.enhancedvisuals.api.type.VisualType;
import team.creative.enhancedvisuals.api.type.VisualTypeShader;
import team.creative.enhancedvisuals.client.VisualManager;
import com.charles445.simpledifficulty.api.SDCapabilities;
import com.charles445.simpledifficulty.api.thirst.IThirstCapability;

public class ThirstHandler extends VisualHandler {

	@CreativeConfig
	public double defaultIntensity = 0F;
	
	@CreativeConfig
	public double maxIntensity = 5;
	
	@CreativeConfig
	public double fadeFactor = 0.05F;
	
	@CreativeConfig
	public IntMinMax thirstLevel = new IntMinMax(2, 8);
	
	@CreativeConfig
	public VisualType focus = new VisualTypeShader("focus", new ResourceLocation("shaders/post/blobs2.json")) {
		
		@Override
		public void changeProperties(float intensity) {
			for (Shader mcShader : shaderGroup.getShaders()) {
				ShaderUniform shaderuniform = mcShader.getShaderManager().getShaderUniform("Radius");
				
				if (shaderuniform != null) {
					shaderuniform.set(intensity);
				}
			}
		}
	};
	public Visual focusVisual;
	
	@Override
	public void tick(@Nullable EntityPlayer player) {
		if (focusVisual == null) {
			focusVisual = new Visual(focus, 0);
			VisualManager.add(focusVisual);
		}
		
		double aimedSaturation = defaultIntensity;
		if (player != null) {
			if (((IThirstCapability) player.getCapability(SDCapabilities.THIRST, null)).getThirstLevel() <= thirstLevel.max) {
				double leftFoodInSpan = ((IThirstCapability) player.getCapability(SDCapabilities.THIRST, null)).getThirstLevel() - thirstLevel.min;
				double spanLength = thirstLevel.spanLength();
				aimedSaturation = (1 - (leftFoodInSpan / spanLength)) * maxIntensity;
			}
		}
		
		if (focusVisual.opacity < aimedSaturation)
			focusVisual.opacity = (float) Math.min(focusVisual.opacity + fadeFactor, aimedSaturation);
		else if (focusVisual.opacity > aimedSaturation)
			focusVisual.opacity = (float) Math.max(focusVisual.opacity - fadeFactor, aimedSaturation);
	}
	
}