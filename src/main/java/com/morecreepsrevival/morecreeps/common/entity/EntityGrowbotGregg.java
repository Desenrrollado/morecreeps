package com.morecreepsrevival.morecreeps.common.entity;

import net.minecraft.world.World;

public class EntityGrowbotGregg extends EntityCreepBase
{
    public EntityGrowbotGregg(World worldIn)
    {
        super(worldIn);

        setCreepTypeName("Growbot Gregg");
    }

    @Override
    protected void updateTexture()
    {
        setTexture("textures/entity/growbotgregg.png");
    }
}