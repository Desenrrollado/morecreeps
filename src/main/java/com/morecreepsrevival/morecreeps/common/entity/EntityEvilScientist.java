package com.morecreepsrevival.morecreeps.common.entity;

import com.morecreepsrevival.morecreeps.common.sounds.CreepsSoundHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityEvilScientist extends EntityCreepBase
{
    private static final DataParameter<Integer> stage = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> trulyEvil = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Boolean> towerBuilt = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> experimentTimer = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.VARINT);

    private static final DataParameter<Boolean> experimentStart = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> numExperiments = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.VARINT);

    private static final DataParameter<Integer> towerHeight = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.VARINT);

    private static final DataParameter<BlockPos> towerPos = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.BLOCK_POS);

    private static final DataParameter<Boolean> water = EntityDataManager.createKey(EntityEvilScientist.class, DataSerializers.BOOLEAN);

    public EntityEvilScientist(World worldIn)
    {
        super(worldIn);

        setCreepTypeName("Evil Scientist");

        creatureType = EnumCreatureType.MONSTER;

        isImmuneToFire = true;

        baseHealth = (float)rand.nextInt(30) + 10.0f;

        baseSpeed = 0.3d;

        updateAttributes();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        dataManager.register(stage, 0);

        dataManager.register(trulyEvil, false);

        dataManager.register(experimentTimer, rand.nextInt(100) + 100);

        dataManager.register(numExperiments, rand.nextInt(3) + 1);

        dataManager.register(towerBuilt, false);

        dataManager.register(experimentStart, false);

        dataManager.register(towerHeight, 0);

        dataManager.register(towerPos, new BlockPos(0, 0, 0));

        dataManager.register(water, false);
    }

    @Override
    public void initEntityAI()
    {
        clearAITasks();

        NodeProcessor nodeProcessor = getNavigator().getNodeProcessor();

        nodeProcessor.setCanSwim(true);

        tasks.addTask(1, new EntityAISwimming(this));

        tasks.addTask(2, new EntityAIBreakDoor(this));

        tasks.addTask(3, new EntityAIAttackMelee(this, 1.0d, true));

        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 0.5d));

        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0d));

        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0f));

        tasks.addTask(6, new EntityAILookIdle(this));

        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));

        if (getTrulyEvil())
        {
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        }
    }

    @Override
    protected void updateTexture()
    {
        if (getTrulyEvil())
        {
            setTexture("textures/entity/evilscientistblown.png");
        }
        else
        {
            setTexture("textures/entity/evilscientist.png");
        }
    }

    protected void setStage(int i)
    {
        dataManager.set(stage, i);
    }

    public int getStage()
    {
        return dataManager.get(stage);
    }

    protected void setTrulyEvil(boolean b)
    {
        dataManager.set(trulyEvil, b);

        updateAttributes();
    }

    @Override
    protected double getMoveSpeed()
    {
        if (getTrulyEvil())
        {
            return 0.5d;
        }

        return super.getMoveSpeed();
    }

    public boolean getTrulyEvil()
    {
        try
        {
            return dataManager.get(trulyEvil);
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        if (getStage() > 3)
        {
            return CreepsSoundHandler.evilLaughSound;
        }

        return CreepsSoundHandler.evilExplosionSound;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return CreepsSoundHandler.evilHurtSound;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return CreepsSoundHandler.evilExperimentSound;
    }

    @Override
    protected void dropItemsOnDeath()
    {
        if (rand.nextInt(5) == 0)
        {
            dropItem(Items.COOKED_PORKCHOP, rand.nextInt(3) + 1);
        }
        else
        {
            dropItem(Item.getItemFromBlock(Blocks.SAND), rand.nextInt(3) + 1);
        }
    }

    @Override
    public void onDeath(@Nonnull DamageSource damageSource)
    {
        tearDownTower();

        super.onDeath(damageSource);
    }

    @Override
    public void setDead()
    {
        tearDownTower();

        super.setDead();
    }

    private void tearDownTower()
    {
        if (!getTowerBuilt())
        {
            return;
        }

        BlockPos blockPos = getTowerPos();

        int towerX = blockPos.getX();

        int towerY = blockPos.getY();

        int towerZ = blockPos.getZ();

        for (int i = 0; i < getTowerHeight() + i; i++)
        {
            world.setBlockToAir(new BlockPos(towerX, towerY + i, towerZ));

            for (int j = 0; j < 3; j++)
            {
                for (int k = 0; k < 3; k++)
                {
                    for (int l = 0; l < 4; l++)
                    {
                        for (int m = 0; m < 10; m++)
                        {
                            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, ((double)(2.0F + (float)towerX) + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, (double)(1.0F + (float)towerY + (float)i) + (double)(rand.nextFloat() * height) + 2D, (2D + ((double)towerZ + (double)(rand.nextFloat() * width * 2.0F))) - (double)width, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d);
                        }
                    }

                    world.setBlockToAir(new BlockPos(towerX + k, towerY + i, towerZ + j + 1));

                    world.setBlockToAir(new BlockPos(towerX + j + 1, towerY + i, towerZ + k));
                }
            }
        }
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 1;
    }

    protected void setTowerBuilt(boolean b)
    {
        dataManager.set(towerBuilt, b);
    }

    public boolean getTowerBuilt()
    {
        try
        {
            return dataManager.get(towerBuilt);
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    protected void setExperimentStart(boolean b)
    {
        dataManager.set(experimentStart, b);
    }

    public boolean getExperimentStart()
    {
        try
        {
            return dataManager.get(experimentStart);
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    protected void setExperimentTimer(int i)
    {
        dataManager.set(experimentTimer, i);
    }

    public int getExperimentTimer()
    {
        return dataManager.get(experimentTimer);
    }

    protected void setNumExperiments(int i)
    {
        dataManager.set(numExperiments, i);
    }

    public int getNumExperiments()
    {
        return dataManager.get(numExperiments);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);

        NBTTagCompound props = compound.getCompoundTag("MoreCreepsEvilScientist");

        props.setInteger("ExperimentTimer", getExperimentTimer());

        props.setBoolean("ExperimentStart", getExperimentStart());

        props.setInteger("Stage", getStage());

        props.setInteger("NumExperiments", getNumExperiments());

        props.setBoolean("TrulyEvil", getTrulyEvil());

        props.setBoolean("TowerBuilt", getTowerBuilt());

        props.setInteger("TowerHeight", getTowerHeight());

        BlockPos blockPos = getTowerPos();

        props.setInteger("TowerPosX", blockPos.getX());

        props.setInteger("TowerPosY", blockPos.getY());

        props.setInteger("TowerPosZ", blockPos.getZ());

        compound.setTag("MoreCreepsEvilScientist", props);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        NBTTagCompound props = compound.getCompoundTag("MoreCreepsEvilScientist");

        if (props.hasKey("ExperimentTimer"))
        {
            setExperimentTimer(props.getInteger("ExperimentTimer"));
        }

        if (props.hasKey("ExperimentStart"))
        {
            setExperimentStart(props.getBoolean("ExperimentStart"));
        }

        if (props.hasKey("Stage"))
        {
            setStage(props.getInteger("Stage"));
        }

        if (props.hasKey("NumExperiments"))
        {
            setNumExperiments(props.getInteger("NumExperiments"));
        }

        if (props.hasKey("TrulyEvil"))
        {
            setTrulyEvil(props.getBoolean("TrulyEvil"));
        }

        if (props.hasKey("TowerBuilt"))
        {
            setTowerBuilt(props.getBoolean("TowerBuilt"));
        }

        if (props.hasKey("TowerHeight"))
        {
            setTowerHeight(props.getInteger("TowerHeight"));
        }

        if (props.hasKey("TowerPosX") && props.hasKey("TowerPosY") && props.hasKey("TowerPosZ"))
        {
            setTowerPos(new BlockPos(props.getInteger("TowerPosX"), props.getInteger("TowerPosY"), props.getInteger("TowerPosZ")));
        }
    }

    @Override
    public void onLivingUpdate()
    {
        super.onLivingUpdate();

        fallDistance = 0.0f;

        if (getStage() == 3 && (posY + 3.0d) < (double)(getTowerPos().getY() + getTowerHeight()))
        {
            setStage(2);
        }

        if (getStage() == 0)
        {
            if (getExperimentTimer() > 0 && !getExperimentStart())
            {
                setExperimentTimer(getExperimentTimer() - 1);
            }

            if (getExperimentTimer() < 1)
            {
                setExperimentStart(true);

                setStage(1);

                setExperimentTimer(rand.nextInt(5000) + 100);
            }
        }

        if (getStage() == 1 && onGround && getExperimentStart() && posY > 63.0d)
        {
            int towerX = MathHelper.floor(posX) + 2;

            int towerY = MathHelper.floor(posY);

            int towerZ = MathHelper.floor(posZ) + 2;

            setTowerPos(new BlockPos(towerX, towerY, towerZ));

            setTowerHeight(rand.nextInt(20) + 10);

            int iTowerHeight = getTowerHeight();

            int area = 0;

            for (int i = 0; i < iTowerHeight; i++)
            {
                for (int q = 0; q < 3; q++)
                {
                    for (int k = 0; k < 3; k++)
                    {
                        area += Block.getIdFromBlock(world.getBlockState(new BlockPos(towerX + k, towerY + i, towerZ + q + 1)).getBlock());

                        area += Block.getIdFromBlock(world.getBlockState(new BlockPos(towerX + q + 1, towerY + i, towerZ + k)).getBlock());
                    }
                }
            }

            boolean housesNear = false;

            for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().expand(30.0d, 30.0d, 30.0d)))
            {
                if (entity instanceof EntityDogHouse)
                {
                    housesNear = true;

                    break;
                }
            }

            Block checkBlock = world.getBlockState(new BlockPos(towerX + 2, towerY - 1, towerZ + 2)).getBlock();

            if (!housesNear && area == 0 && checkBlock != Blocks.AIR && checkBlock != Blocks.WATER && checkBlock != Blocks.FLOWING_WATER)
            {
                setTowerBuilt(true);

                for (int a = 0; a < iTowerHeight; a++)
                {
                    for (int b = 0; b < 3; b++)
                    {
                        for (int c = 0; c < 3; c++)
                        {
                            Block block = Blocks.COBBLESTONE;

                            if (rand.nextInt(5) == 0)
                            {
                                block = Blocks.MOSSY_COBBLESTONE;
                            }

                            world.setBlockState(new BlockPos(towerX + c, towerY + a, towerZ + b + 1), block.getDefaultState());

                            block = Blocks.COBBLESTONE;

                            if (rand.nextInt(5) == 0)
                            {
                                block = Blocks.MOSSY_COBBLESTONE;
                            }

                            world.setBlockState(new BlockPos(towerX + b + 1, towerY + a, towerZ + c), block.getDefaultState());
                        }
                    }
                }

                world.setBlockState(new BlockPos(towerX + 2, towerY + iTowerHeight, towerZ + 2), Blocks.CRAFTING_TABLE.getDefaultState());

                for (int i = 0; i < iTowerHeight; i++)
                {
                    world.setBlockState(new BlockPos(towerX, towerY + i, towerZ), Blocks.LADDER.getDefaultState());
                }

                setStage(2);
            }
            else
            {
                setStage(0);

                setExperimentTimer(rand.nextInt(100) + 50);

                setExperimentStart(false);
            }
        }

        if (getStage() == 2)
        {
            BlockPos blockPos = getTowerPos();

            int towerX = blockPos.getX();

            int towerY = blockPos.getY();

            int towerZ = blockPos.getZ();

            if (posX < (double)towerX)
            {
                motionX = 0.20000000298023224d;
            }

            if (posZ < (double)towerZ)
            {
                motionZ = 0.20000000298023224d;
            }

            if (Math.abs(posX - (double)towerX) < 0.40000000596046448d && Math.abs(posZ - (double)towerZ) < 0.40000000596046448d)
            {
                motionX = 0.0d;

                motionZ = 0.0d;

                motionY = 0.30000001192092896d;

                world.setBlockToAir(new BlockPos(MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) + 2, MathHelper.floor(posZ)));

                if (posY > (double)(towerY + getTowerHeight()))
                {
                    motionY = 0.0d;

                    posZ++;

                    posX++;

                    setStage(3);

                    setExperimentTimer(rand.nextInt(1000) + 500);

                    setExperimentStart(false);
                }
            }
        }

        if (getStage() == 3)
        {
            BlockPos blockPos = getTowerPos();

            int towerX = blockPos.getX();

            int towerY = blockPos.getY();

            int towerZ = blockPos.getZ();

            int iTowerHeight = getTowerHeight();

            posY = towerY + iTowerHeight;

            posX = towerX + 2;

            posZ = towerZ + 2;

            setPosition(towerX + 2, towerY + iTowerHeight, towerZ + 2);

            motionX = 0.0d;

            motionY = 0.0d;

            motionZ = 0.0d;

            baseSpeed = 0.0d;

            updateMoveSpeed();

            if (getExperimentTimer() > 0)
            {
                setExperimentTimer(getExperimentTimer() - 1);
            }

            if (rand.nextInt(200) == 0)
            {
                world.addWeatherEffect(new EntityLightningBolt(world, MathHelper.floor(posX), MathHelper.floor(getEntityBoundingBox().minY) + 3, MathHelper.floor(posZ), false));
            }

            if (rand.nextInt(150) == 0 && !getWater())
            {
                world.setBlockState(new BlockPos(towerX + 2, towerY + iTowerHeight, towerZ + 1), Blocks.FLOWING_WATER.getDefaultState());

                world.setBlockState(new BlockPos(towerX + 3, towerY + iTowerHeight, towerZ + 2), Blocks.FLOWING_WATER.getDefaultState());

                world.setBlockState(new BlockPos(towerX + 1, towerY + iTowerHeight, towerZ + 2), Blocks.FLOWING_WATER.getDefaultState());

                world.setBlockState(new BlockPos(towerX + 2, towerY + iTowerHeight, towerZ + 3), Blocks.FLOWING_WATER.getDefaultState());

                setWater(true);
            }

            if (rand.nextInt(8) == 0)
            {
                // TODO: spawn evil light
            }

            if (rand.nextInt(10) == 0)
            {
                for (int i = 0; i < 4; i++)
                {
                    for (int k = 0; k < 10; k++)
                    {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, ((double)(2.0F + (float)towerX) + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, (double)(1.0F + (float)towerY + (float)iTowerHeight) + (double)(rand.nextFloat() * height) + 2D, (2D + ((double)towerZ + (double)(rand.nextFloat() * width * 2.0F))) - (double)width, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d);
                    }
                }
            }

            if (!getExperimentStart())
            {
                for (int i = 0; i < 4; i++)
                {
                    for (int k = 0; k < 10; k++)
                    {
                        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, ((double)(2.0F + (float)towerX) + (double)(rand.nextFloat() * width * 2.0F)) - (double)width, (double)(1.0F + (float)towerY + (float)iTowerHeight) + (double)(rand.nextFloat() * height) + 2D, (2D + ((double)towerZ + (double)(rand.nextFloat() * width * 2.0F))) - (double)width, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d);
                    }
                }

                // TODO: spawn evil light

                setExperimentStart(true);
            }

            if (getExperimentTimer() < 1)
            {
                world.createExplosion(null, towerX + 2, towerY + iTowerHeight + 4, towerZ + 2, 2.0f, true);

                setExperimentStart(true);

                setStage(4);
            }
        }

        if (getStage() == 4)
        {
            int x = MathHelper.floor(posX);

            int y = MathHelper.floor(getEntityBoundingBox().minY);

            int z = MathHelper.floor(posZ);

            int randInt = rand.nextInt(5) + 1;

            for (int i = 0; i < randInt; i++)
            {
                world.addWeatherEffect(new EntityLightningBolt(world, x + rand.nextInt(4) - 2, y + 6, z + rand.nextInt(4) - 2, false));
            }

            playSound(CreepsSoundHandler.evilExperimentSound, getSoundVolume(), getSoundPitch());

            setTrulyEvil(true);

            updateAttributes();

            randInt = rand.nextInt(4) + 1;

            BlockPos blockPos = getTowerPos();

            int towerX = blockPos.getX();

            int towerY = blockPos.getY();

            int towerZ = blockPos.getZ();

            int iTowerHeight = getTowerHeight();

            for (int i = 0; i < randInt; i++)
            {
                switch (rand.nextInt(4))
                {
                    case 0:
                    case 4:
                        // TODO: spawn snowman

                        break;
                    case 1:
                        EntityEvilScientist evilScientist = new EntityEvilScientist(world);

                        evilScientist.setLocationAndAngles(towerX, towerY + iTowerHeight + 1, towerZ, rotationYaw, 0.0f);

                        evilScientist.determineBaseTexture();

                        evilScientist.setInitialHealth();

                        if (!world.isRemote)
                        {
                            world.spawnEntity(evilScientist);
                        }

                        break;
                    case 2:
                        EntityEvilPig evilPig = new EntityEvilPig(world);

                        evilPig.setLocationAndAngles(towerX, towerY + iTowerHeight + 1, towerZ, rotationYaw, 0.0f);

                        evilPig.determineBaseTexture();

                        evilPig.setInitialHealth();

                        if (!world.isRemote)
                        {
                            world.spawnEntity(evilPig);
                        }

                        break;
                    case 3:
                        EntityEvilChicken evilChicken = new EntityEvilChicken(world);

                        evilChicken.setLocationAndAngles(towerX + rand.nextInt(3), towerY + iTowerHeight + 1, towerZ + rand.nextInt(3), rotationYaw, 0.0f);

                        evilChicken.motionX = rand.nextFloat() * 0.3f;

                        evilChicken.motionY = rand.nextFloat() * 0.4f;

                        evilChicken.motionZ = rand.nextFloat() * 0.4f;

                        if (!world.isRemote)
                        {
                            world.spawnEntity(evilChicken);
                        }

                        break;
                    default:
                        break;
                }
            }

            setNumExperiments(getNumExperiments() - 1);

            if (getNumExperiments() < 1)
            {
                setNumExperiments(rand.nextInt(4) + 1);

                setStage(5);
            }
            else
            {
                setStage(3);

                setExperimentTimer(rand.nextInt(500) + 500);

                setExperimentStart(false);
            }
        }

        if (getStage() == 5)
        {
            tearDownTower();

            setStage(6);
        }

        if (getStage() == 6)
        {
            setExperimentTimer(rand.nextInt(5000) + 100);

            setStage(0);
        }
    }

    protected void setTowerHeight(int i)
    {
        dataManager.set(towerHeight, i);
    }

    public int getTowerHeight()
    {
        return dataManager.get(towerHeight);
    }

    protected void setTowerPos(BlockPos pos)
    {
        dataManager.set(towerPos, new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
    }

    public BlockPos getTowerPos()
    {
        return dataManager.get(towerPos);
    }

    protected void setWater(boolean b)
    {
        dataManager.set(water, b);
    }

    public boolean getWater()
    {
        try
        {
            return dataManager.get(water);
        }
        catch (Exception ignored)
        {
        }

        return false;
    }

    @Override
    protected float getBaseHealth()
    {
        if (getTrulyEvil())
        {
            return 90.0f;
        }

        return super.getBaseHealth();
    }
}
