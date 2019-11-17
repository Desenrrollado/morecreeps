package com.morecreepsrevival.morecreeps.common.entity;

import com.morecreepsrevival.morecreeps.common.helpers.InventoryHelper;
import com.morecreepsrevival.morecreeps.common.items.CreepsItemHandler;
import com.morecreepsrevival.morecreeps.common.networking.CreepsPacketHandler;
import com.morecreepsrevival.morecreeps.common.networking.message.MessageOpenGuiSneakySal;
import com.morecreepsrevival.morecreeps.common.sounds.CreepsSoundHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntitySneakySal extends EntityCreepBase implements IRangedAttackMob
{
    private static final DataParameter<Integer> dissedMax = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.VARINT);

    private static final DataParameter<Integer> sale = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.VARINT);

    private static final DataParameter<Float> salePrice = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.FLOAT);

    private static final DataParameter<Boolean> shooting = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.BOOLEAN);

    private static final DataParameter<Integer> shootingDelay = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.VARINT);

    private static final DataParameter<NBTTagCompound> shopItems = EntityDataManager.createKey(EntitySneakySal.class, DataSerializers.COMPOUND_TAG);

    public static final int[] itemPrices = {
            10, 200, 100, 20, 175, 150, 225, 50, 350, 100,
            150, 10, 200, 150, 250
    };

    public static final String[] itemDescriptions = {
            "BLORP COLA", "ARMY GEM", "HORSE HEAD GEM", "BAND AID", "SHRINK RAY", "EXTINGUISHER", "GROW RAY", "FRISBEE", "LIFE GEM", "GUN",
            "RAYGUN", "POPSICLE", "EARTH GEM", "FIRE GEM", "SKY GEM"
    };

    public static final Item[] itemDefinitions = {
            CreepsItemHandler.blorpCola,
            CreepsItemHandler.armyGem,
            CreepsItemHandler.horseHeadGem,
            CreepsItemHandler.bandaid,
            CreepsItemHandler.shrinkRay,
            CreepsItemHandler.extinguisher,
            CreepsItemHandler.growRay,
            CreepsItemHandler.frisbee,
            CreepsItemHandler.lifeGem,
            CreepsItemHandler.gun,
            CreepsItemHandler.raygun,
            CreepsItemHandler.popsicle,
            CreepsItemHandler.earthGem,
            CreepsItemHandler.fireGem,
            CreepsItemHandler.skyGem
    };

    public EntitySneakySal(World worldIn)
    {
        super(worldIn);

        setCreepTypeName("Sneaky Sal");

        setSize(1.5f, 4.0f);

        setModelSize(1.5f);

        setHeldItem(EnumHand.MAIN_HAND, new ItemStack(CreepsItemHandler.gun));

        baseHealth = (float)rand.nextInt(50) + 50.0f;

        baseAttackDamage = 3.0d;

        baseSpeed = 0.3f;

        updateAttributes();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();

        dataManager.register(dissedMax, rand.nextInt(4) + 1);

        dataManager.register(sale, rand.nextInt(2000) + 100);

        dataManager.register(salePrice, 0.0f);

        dataManager.register(shooting, false);

        dataManager.register(shootingDelay, 0);

        dataManager.register(shopItems, new NBTTagCompound());

        dataManager.get(shopItems).setIntArray("Items", new int[30]);

        dataManager.setDirty(shopItems);
    }

    @Override
    protected void updateTexture()
    {
        setTexture("textures/entity/sneakysal.png");
    }

    @Override
    protected void initEntityAI()
    {
        clearAITasks();

        NodeProcessor nodeProcessor = getNavigator().getNodeProcessor();

        nodeProcessor.setCanSwim(true);

        nodeProcessor.setCanEnterDoors(true);

        tasks.addTask(1, new EntityAISwimming(this));

        tasks.addTask(2, new EntityAIBreakDoor(this));

        tasks.addTask(3, new EntityAIAttackRanged(this, 1.0d, 25, 75, 50.0f));

        tasks.addTask(4, new EntityAIMoveTowardsRestriction(this, 0.5d));

        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0d));

        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0f));

        tasks.addTask(6, new EntityAILookIdle(this));

        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public void attackEntityWithRangedAttack(@Nonnull EntityLivingBase target, float distanceFactor)
    {
        dataManager.set(shooting, true);

        dataManager.set(shootingDelay, 10);
    }

    @Override
    public void setSwingingArms(boolean swingingArms)
    {
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        if (rand.nextInt(10) == 0)
        {
            return CreepsSoundHandler.giraffeSound;
        }

        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return CreepsSoundHandler.salHurtSound;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return CreepsSoundHandler.salDeadSound;
    }

    @Override
    public int getMaxSpawnedInChunk()
    {
        return 1;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand)
    {
        if (hand == EnumHand.OFF_HAND)
        {
            return super.processInteract(player, hand);
        }
        else if (dataManager.get(dissedMax) > 0)
        {
            if (getSalePrice() == 0.0f || dataManager.get(sale) < 1)
            {
                restock();
            }

            if (!(getAttackTarget() instanceof EntityPlayer))
            {
                if (!world.isRemote)
                {
                    CreepsPacketHandler.INSTANCE.sendTo(new MessageOpenGuiSneakySal(getEntityId()), (EntityPlayerMP)player);
                }
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public void onLivingUpdate()
    {
        if (dataManager.get(shootingDelay) > 0)
        {
            dataManager.set(shootingDelay, dataManager.get(shootingDelay) - 1);

            if (dataManager.get(shootingDelay) < 1)
            {
                dataManager.set(shooting, false);
            }
        }

        if (dataManager.get(sale) > 0)
        {
            dataManager.set(sale, dataManager.get(sale) - 1);
        }

        if (rand.nextInt(10) == 0)
        {
            smoke();
        }
    }

    @Override
    public void smoke()
    {
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (posX + (double)(rand.nextFloat() * width * 2.0f)) - (double)width, posY + (double)(rand.nextFloat() * height) + (double)i, (posZ + (double)(rand.nextFloat() * width * 2.0f)) - (double)width, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d, rand.nextGaussian() * 0.02d);
            }
        }
    }

    private void restock()
    {
        dataManager.set(sale, rand.nextInt(2000) + 100);

        dataManager.set(salePrice, 1.0f - (rand.nextFloat() * 0.25f - rand.nextFloat() * 0.25f));

        int[] currentItems = dataManager.get(shopItems).getIntArray("Items");

        for (int i = 0; i < itemDefinitions.length; i++)
        {
            currentItems[i] = i;
        }

        for (int i = 0; i < itemDefinitions.length; i++)
        {
            int k = rand.nextInt(itemDefinitions.length);

            int l = currentItems[i];

            currentItems[i] = currentItems[k];

            currentItems[k] = l;
        }

        dataManager.setDirty(shopItems);
    }

    @Override
    protected void dropItemsOnDeath()
    {
        if (rand.nextInt(10) == 0)
        {
            dropItem(CreepsItemHandler.rocket, rand.nextInt(5) + 1);
        }
    }

    @Override
    public void onDeath(@Nonnull DamageSource cause)
    {
        smoke();

        super.onDeath(cause);
    }

    public boolean getShooting()
    {
        return dataManager.get(shooting);
    }

    public int[] getShopItems()
    {
        return dataManager.get(shopItems).getIntArray("Items");
    }

    public float getSalePrice()
    {
        return dataManager.get(salePrice);
    }

    public void ripOff()
    {
        dataManager.set(dissedMax, dataManager.get(dissedMax) - 1);

        if (rand.nextInt(9) == 0)
        {
            playSound(SoundEvents.ENTITY_CHICKEN_EGG, getSoundVolume(), getSoundPitch());

            switch (rand.nextInt(15) + 1)
            {
                case 1:
                    dropItem(CreepsItemHandler.armyGem, 1);

                    break;
                case 2:
                    dropItem(CreepsItemHandler.horseHeadGem, 1);

                    break;
                case 4:
                    dropItem(CreepsItemHandler.shrinkRay, 1);

                    break;
                case 5:
                    dropItem(CreepsItemHandler.extinguisher, 1);

                    break;
                case 6:
                    dropItem(CreepsItemHandler.growRay, 1);

                    break;
                case 7:
                    dropItem(CreepsItemHandler.frisbee, 1);

                    break;
                case 8:
                    dropItem(CreepsItemHandler.lifeGem, 1);

                    break;
                case 9:
                    dropItem(CreepsItemHandler.gun, 1);

                    break;
                case 10:
                    dropItem(CreepsItemHandler.raygun, 1);

                    break;
                case 3:
                default:
                    dropItem(CreepsItemHandler.bandaid, 1);

                    break;
            }

            return;
        }

        int randInt = rand.nextInt(15) + 5;

        double d = -MathHelper.sin((rotationYaw * (float)Math.PI) / 180.0f);

        double d1 = MathHelper.cos((rotationYaw * (float)Math.PI) / 180.0f);

        for (int i = 0; i < randInt; i++)
        {
            EntityRatMan ratMan = new EntityRatMan(world);

            ratMan.setLocationAndAngles((posX + d * 1.0d + (double)rand.nextInt(4)) - 2.0d, posY - 1.0d, (posZ + d1 * 1.0d + (double)rand.nextInt(4)) - 2.0d, rotationYaw, 0.0f);

            ratMan.motionY = 1.0d;

            if (!world.isRemote)
            {
                world.spawnEntity(ratMan);
            }
        }

        playSound(CreepsSoundHandler.salRatsSound, 1.0f, 1.0f);
    }

    public void buyItem(EntityPlayer player, int itemId)
    {
        if (itemId < 0 || itemId >= itemDefinitions.length)
        {
            return;
        }

        int item = getShopItems()[itemId];

        if (InventoryHelper.takeItem(player.inventory, CreepsItemHandler.money, Math.round((float)itemPrices[item] * getSalePrice())))
        {
            dropItem(itemDefinitions[item], 1);

            playSound(CreepsSoundHandler.salSaleSound, 1.0f, 1.0f);
        }
        else
        {
            playSound(CreepsSoundHandler.salNoMoneySound, 1.0f, 1.0f);
        }
    }
}
