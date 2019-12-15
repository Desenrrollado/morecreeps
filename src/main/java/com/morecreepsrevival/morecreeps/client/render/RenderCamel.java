package com.morecreepsrevival.morecreeps.client.render;

import com.morecreepsrevival.morecreeps.client.models.ModelCamel;
import com.morecreepsrevival.morecreeps.common.entity.EntityCamel;
import net.minecraft.client.renderer.entity.RenderManager;

public class RenderCamel<T extends EntityCamel> extends RenderCreep<T>
{
    public RenderCamel(RenderManager renderManager)
    {
        super(renderManager, new ModelCamel(), 0.5f);
    }
}
