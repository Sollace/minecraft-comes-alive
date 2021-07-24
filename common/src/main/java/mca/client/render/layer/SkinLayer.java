package mca.client.render.layer;

import mca.client.model.VillagerEntityModelMCA;
import mca.client.render.SkinColors;
import mca.entity.VillagerEntityMCA;
import mca.entity.ai.relationship.Gender;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;

public class SkinLayer extends VillagerLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public SkinLayer(FeatureRendererContext<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> renderer, VillagerEntityModelMCA<VillagerEntityMCA> model) {
        super(renderer, model);
    }

    @Override
    protected String getSkin(VillagerEntityMCA villager) {
        Gender gender = villager.getGenetics().getGender();
        int skin = (int) Math.min(4, Math.max(0, villager.getGenetics().skin.get() * 5));
        return String.format("mca:skins/skin/%s/%d.png", gender == Gender.FEMALE ? "female" : "male", skin);
    }

    @Override
    protected float[] getColor(VillagerEntityMCA villager) {
        float melanin = villager.getGenetics().melanin.get();
        float hemoglobin = villager.getGenetics().hemoglobin.get();
        double[] color = SkinColors.getColor(melanin, hemoglobin);
        return new float[]{(float) color[0], (float) color[1], (float) color[2]};
    }
}