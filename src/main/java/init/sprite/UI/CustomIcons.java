package init.sprite.UI;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sets.ArrayListGrower;
import java.io.IOException;

public class CustomIcons extends Icons {
        /////////////////////////////////////////////#!# This is a unique file that doesn't overwrite any of Jake's files.
        /////#!# This extends from Icons to add new custom icons of Small and Large size.
        public final CS s = new CS();
        public final CL l = new CL();

        CustomIcons() throws IOException {
        }

        public static class CS extends IconMaker{
                
                private static ArrayListGrower<IconS> all = new ArrayListGrower<>();

                {
                        all.clear();
                }

                private CS() throws IOException{
                        super("16", 16);
                }

                int i = 0;

                private IconS get() throws IOException {
                        int k = i;
                        i++;

                        return new IconS(super.get("_Icons", k));
                }

                private final static COLOR mask = new ColorImp(142, 134, 107);

                public static class IconS extends Icon {

                        public final int index;

                        IconS(Icon i) {
                                super(Icon.S, i);
                                index = all.add(this);
                        }

                        @Override
                        public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
                                COLOR c = CORE.renderer().colorGet();
                                if (c.red() == 127 && c.green() == 127 && c.blue() == 127) {
                                        mask.bind();
                                        super.render(r, X1, X2, Y1, Y2);
                                        COLOR.unbind();
                                }else {


                                        super.render(r, X1, X2, Y1, Y2);
                                }

                        }


                }

                public IconS get(int index) {
                        if (index > all.size())
                                return custom7;
                        return all.get(index);
                }
                
                public final IconS c_minus = get_custom();
                public final IconS c_plus = get_custom();
                public final IconS custom3 = get_custom();
                public final IconS custom4 = get_custom();

                public final IconS custom5 = get_custom();
                public final IconS custom6 = get_custom();
                public final IconS custom7 = get_custom();
                public final IconS custom8 = get_custom();

                int j = 0;

                private IconS get_custom() throws IOException {
                        j++;
                        return new IconS(super.get("_CustomIcons", j-1));
                }



        }

        public static class CL extends IconMaker {

                private CL() throws IOException {
                        super("32", 32);
                }


                public final Icon plus = get_custom();
                public final Icon minus = get_custom();
                public final Icon maint = get_custom();

                int j = 0;
                private Icon get_custom() throws IOException {
                        j++;
                        return get("_CustomIcons", j-1);
                }
        }
}
