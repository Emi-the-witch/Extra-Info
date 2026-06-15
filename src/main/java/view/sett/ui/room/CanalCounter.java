package view.sett.ui.room;
import util.GUTIL;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.DIR;

public class CanalCounter {
        static public int countCanals(Room canal, int rx, int ry) {
                PathUtilOnline.Flooder f = GUTIL.flooder();
                f.init(canal);
                // how many canal tiles are orthogonally connected to passed canal tile
                int am = 0;
                f.pushSloppy(rx, ry, 0);
                Class<?> cCanal = canal.getClass();

                while(f.hasMore()) {
                        PathTile t = f.pollSmallest();
                        Room c = SETT.ROOMS().map.get(t.x(), t.y());
                        // if there is a room, check if it is same class as the canal
                        if (c == null || !(c.getClass().equals(canal.getClass()))) {
                                // if it's not a canal, check next tile
                                continue;
                        }
                        // it is a canal, so increase the counter
                        am++;
                        // add neighbouring tiles to check
                        for (DIR d : DIR.ORTHO) {
                                f.pushSloppy(t, d, 0);
                        }
                }
                f.done();
                return am;
        }
}