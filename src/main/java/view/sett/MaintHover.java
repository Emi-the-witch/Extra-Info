package view.sett;

import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.tilemap.floor.Floors;
import view.ui.goods.UIMaintenance;

import java.util.Arrays;

import static java.lang.Math.*;
import static settlement.main.SETT.*;
import view.ui.goods.UIMaintenance.*;
import settlement.maintenance.MAINTENANCE.*;

public class MaintHover {
        public static int[] last_coords;
        public static long target;
        public static int maint_val;

        public static void not_drag(){
                last_coords = new int[4];
        }
        public static String drag(int x1, int y1, int x2, int y2){
                int[] coords = { x1, y1, x2, y2 };

                if (! Arrays.equals(last_coords, coords) ){
                        target =  System.currentTimeMillis() + 3000;
                }
                if (  System.currentTimeMillis() > target ){
                        // Calculate shit
                        for (int i = 1; i < ( max(x1,x2)-min(x1,x2) * max(y1,y2)-min(y1,y2) ); i++ ) {

                        }

                        MaintHover.last_coords = coords;
                        return ("Coordinates are "+ x1 + " "+ y1 + " "+ x2 + " "+ y2 );
                }
                last_coords = coords;
                return("");
        }
        public static double maint_calc(int x1, int y1, int x2, int y2){
//                for (int y = min(y1,y2); y <= max(y1,y2); y++) {
//                        for (int x = min(x1,x2); x <= max(x1,x2); x++) {
//
//                        }
//                }
//                return (double);
                return(0);
        }

}
