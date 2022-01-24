package starpocalypse.submarket;

import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.MilitarySubmarketPlugin;
import lombok.extern.log4j.Log4j;
import starpocalypse.config.SimpleMap;
import starpocalypse.helper.ConfigUtils;

@Log4j
public class RegulatedMilitaryMarket extends MilitarySubmarketPlugin {

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, TransferAction action) {
        CommodityOnMarketAPI com = market.getCommodityData(commodityId);
        CommoditySpecAPI csa = com.getCommodity();
        if (isStabilityLegal(ConfigUtils.getRegulationStabilityItem(), csa.getBasePrice())) {
            log.debug("Making legal due to low stability " + commodityId);
            return false;
        }
        return super.isIllegalOnSubmarket(commodityId, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, TransferAction action) {
        if (isStabilityLegal(ConfigUtils.getRegulationStabilityItem(), stack.getBaseValuePerUnit())) {
            log.debug("Making legal due to low stability " + stack.getDisplayName());
            return false;
        }
        return super.isIllegalOnSubmarket(stack, action);
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, TransferAction action) {
        if (isStabilityLegal(ConfigUtils.getRegulationStabilityShip(), member.getBaseValue())) {
            log.debug("Making legal due to low stability " + member.getHullSpec().getHullName());
            return false;
        }
        return super.isIllegalOnSubmarket(member, action);
    }

    private boolean isStabilityLegal(SimpleMap stabilityMap, float baseValue) {
        if (!ConfigUtils.wantsRegulation(market.getFactionId())) {
            return false;
        }
        float stability = submarket.getMarket().getStabilityValue();
        if (stability <= 0) {
            return true;
        }
        if (stability >= 10) {
            return false;
        }
        String stabilityKey = String.format("%.0f", stability);
        if (!stabilityMap.containsKey(stabilityKey)) {
            log.error("Missing stability mapping for key " + stabilityKey);
            return false;
        }
        float stabilityValue = Float.parseFloat(stabilityMap.get(stabilityKey));
        return baseValue < stabilityValue;
    }
}