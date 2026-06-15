package EmiWitch;

import game.faction.FACTIONS;
import game.faction.player.PTech;
import settlement.main.SETT;
import settlement.room.infra.admin.AdminData;
import settlement.room.infra.admin.ROOM_ADMIN;
import settlement.room.main.RoomInstance;
import snake2d.util.sets.KeyMap;

public class Knowledge_Costs2 {
    public static KeyMap<Knowledge_Costs2> CURRCOSTMAP = new KeyMap<>();
    private static boolean isSet = false;

    // for each tech currency
    public PTech.TechCurr curr;// currency this is for
    public double know_tot    ;// total value the player has
    public double know_emp    ;// employment in buildings that give this currency
    public double know_worker ;// tech value per worker
    public double cost_total  ;// Total costs per worker
    public double cost_inputs ;// input costs
    public double cost_maint  ;// maintenance costs
    public double cost_tools  ;// tools costs

    public Knowledge_Costs2(PTech.TechCurr curr) {
        this.curr = curr;
    }

    private static void setup() {
        if (isSet) {
            return;
        }

        for (PTech.TechCurr curr : FACTIONS.player().tech().currs()) {
            CURRCOSTMAP.put(curr.cu.bo.key, new Knowledge_Costs2(curr));
        }
        isSet = true;
    }

    public static void costs()
    {
        setup();

        for (ROOM_ADMIN r : SETT.ROOMS().ADMINS) {
            Knowledge_Costs2 costs = CURRCOSTMAP.get(r.data.target.key);

            for (RoomInstance r_ins : r.all()) {
                RoomCostUtil rcu = new RoomCostUtil(r_ins, false);
                costs.know_emp += rcu.employees;
                costs.cost_inputs += rcu.inputs;
                costs.cost_tools += rcu.equipment;
                costs.cost_maint += rcu.maintenance;

                AdminData.ROOM_ADMIN_HOLDER admin_room = (AdminData.ROOM_ADMIN_HOLDER) r_ins.blueprint();
                costs.know_tot += admin_room.admin().value();
            }
        }

        for (Knowledge_Costs2 costs : CURRCOSTMAP.all()) {
            if (costs.know_emp == 0) {
                costs.know_worker = 0.0;
                costs.cost_total = 0.0;
            } else {
                costs.know_worker = costs.know_tot / costs.know_emp;
                costs.cost_total = (costs.cost_inputs + costs.cost_tools) / costs.know_emp;
            }
        }

    }
}