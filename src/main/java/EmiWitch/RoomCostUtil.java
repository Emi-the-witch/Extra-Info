package EmiWitch;

import game.faction.FACTIONS;
import game.time.TIME;
import init.resources.RESOURCE;
import settlement.maintenance.ROOM_DEGRADER;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEquip;
import snake2d.util.sets.LIST;

import static settlement.main.SETT.MAINTENANCE;

public class RoomCostUtil {

    public int employees = 0;
    public double inputs = 0.0;
    public double outputs = 0.0;
    public double maintenance = 0.0;
    public double equipment = 0.0;

    public RoomCostUtil(Room r, boolean calcMaintenance) {
        if (!(r instanceof RoomInstance)) {
            return;
        }
        RoomInstance r_ins = (RoomInstance) r;
        if (r_ins.employees() != null) {
            employees += r_ins.employees().employed();
        }

        if (r instanceof ROOM_PRODUCER_INSTANCE) {
            ROOM_PRODUCER_INSTANCE r_prod = (ROOM_PRODUCER_INSTANCE) r;

            inputs = getIndustryValue(r_prod, r_prod.industry().ins(), true);
            outputs = getIndustryValue(r_prod, r_prod.industry().outs(), false);
        }

        equipment = getEquipmentCost(r_ins);
        if (calcMaintenance) {
            maintenance = getMaintenanceCost(r_ins);
        }
    }

    public static double getIndustryValue(ROOM_PRODUCER_INSTANCE r, LIST<IndustryResource> rs, boolean buying) {
        double total = 0.0;

        for (IndustryResource i : rs) {
            double n = i.dayPrev.get(r);

            if (buying) {
                total -= n * FACTIONS.player().trade.pricesBuy.get(i.resource);
            } else {
                total += n * FACTIONS.player().trade.pricesSell.get(i.resource);
            }
        }

        return total;
    }

    public static double getEquipmentCost(RoomInstance r) {
        double total = 0.0;

        for (RoomEquip w : r.blueprint().employment().tools()) {
            total += w.degradePerDay * r.employees().tools(w) * FACTIONS.player().trade.pricesBuy.get(w.resource);
        }

        return total;
    }

    public double getMaintenanceCost(RoomInstance r) {
        double total = 0.0;

        ROOM_DEGRADER deg = r.degrader(r.mX(), r.mY());
        double iso = r.isolation(r.mX(), r.mY());
        double boost = MAINTENANCE().speed();

        if (deg == null) {
            return 0.0;
        }

        for (int i = 0; i < deg.resSize(); i++) {
            if (deg.resAmount(i) <= 0)
                continue;
            RESOURCE res = deg.res(i);

            double n = ROOM_DEGRADER.rateResource(boost, deg.base(), iso, deg.resAmount(i)) * TIME.years().bitConversion(TIME.days()) / 16.0;
            total -= n * FACTIONS.player().trade.pricesBuy.get(res);
        }
        return total;
    }
}