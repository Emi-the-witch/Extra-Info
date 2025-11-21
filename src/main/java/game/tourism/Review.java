package game.tourism;

import game.time.TIME;
import game.tourism.Text.InsertData;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.service.StatService;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Str.StringReusableSer;
import util.colors.GCOLOR;
import util.text.DicTime;
import util.text.Inserter;

import java.io.IOException;
import java.io.Serializable;

public final class Review implements Serializable, SAVABLE{
	//#!# Explain the tourism rating by recording the 3 main scores that go into the payout.

	// scores for the most recent one
	public double attraction_score;
	public double service_score;
	public double inn_score;
	public double score_rand;
	private static final long serialVersionUID = 1L;
	/// //
	public Review() {
		
	}
	
	public final StringReusableSer name = new StringReusableSer(64);
	public final StringReusableSer rating = new StringReusableSer(128);
	public final StringReusableSer attraction = new StringReusableSer(128);
	public final StringReusableSer service = new StringReusableSer(128);
	public final StringReusableSer inn = new StringReusableSer(128);
	public double score = 0;
	public int credits;
	
	@Override
	public void save(FilePutter file) {
		name.save(file);
		rating.save(file);
		attraction.save(file);
		service.save(file);
		inn.save(file);
		file.d(score);
		file.i(credits);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		name.load(file);
		rating.load(file);
		attraction.load(file);
		service.load(file);
		inn.load(file);
		score = file.d();
		credits = file.i();
	}
	
	public void copyOther(Review other) {
		name.clear().add(other.name);
		rating.clear().add(other.rating);
		attraction.clear().add(other.attraction);
		service.clear().add(other.service);
		inn.clear().add(other.inn);
		score = other.score;
		credits = other.credits;
	}
	
	public boolean has() {
		return name.length() > 0;
	}
	
	@Override
	public void clear() {
		name.clear();
		score = 0;
	}

	public void make(Induvidual indu, COORDINATE inn) {
		Inserter.setRandom(RND.rLong());
		
		name.clear();
		name.s().add('/').s();
		name.add(STATS.APPEARANCE().name(indu));
		name.NL();
		name.add(DicTime.setDate(Str.TMP.clear(), (int) TIME.currentSecond()));
		
		InsertData d = Text.dd;
		d.i = indu;
		d.inn = inn;
		

		score = 0;
		Race race = indu.race();
		credits = 0;
		Text da = race.tourism().data;

		
		
		{
			RoomBlueprintImp a = TOURISM.attraction(indu);
			double s = CLAMP.d((a.employment().employed()-TOURISM.MIN_EMPLOYEES)/(double)TOURISM.MAX_EMPLOYEES, 0, 1);
			//#!# attraction_score
			attraction_score = s;

			score += s;
			if (s == 0)
				d.rating = 0;
			else
				d.rating = 0.5+s*0.5+RND.rFloat0(0.15);
			attraction.clear().add(da.attraction.get(d));
		}


		//#!# service_score
		service_score = setService(d, service.clear());
		score += service_score;

		if (inn != null && SETT.ROOMS().INN.get(inn) != null) {
			ROOM_SERVICER in = (ROOM_SERVICER) SETT.ROOMS().INN.get(inn);
			inn_score = in.quality();
			d.rating = 0.35 + 0.65*in.quality();
			this.inn.clear().add(da.inn.get(d));
			score += inn_score;
		}

		score /=3;
		
		credits = (int) (score*race.tourism().credits*TOURISM.CREDITS*RND.rFloat1(0.2));
		score_rand = RND.rFloat0(0.2);
		score += score_rand;
		score = CLAMP.d(score, 0, 1);
		
		d.rating = score;
		rating.clear().add(da.rating.get(d));
		
	}
	
	private double setService(InsertData d, Str tmp) {
		Text da = d.i.race().tourism().data;
		
		StatService ss = TOURISM.service(d.i);
		
		if (ss != null) {
			d.rating = ss.total().indu().getD(d.i);
			if (d.rating == 0)
				d.rating = 0;
			else
				d.rating = 0.5+d.rating*0.5+RND.rFloat0(0.15);
			
			
			
			
			tmp.add(da.service.get(d));
			return d.rating;
			
		}
		return 0;
	}
	
	public int render(SPRITE_RENDERER r, int x1, int y1, int width) {
		
		{
			int icons = (int) (1 + 4*score);
			int x = x1 + width/2 - Icon.S*icons/2;
			for (int i = 0; i < icons; i++) {
				SPRITES.icons().s.star.render(r, x, y1);
				x+= Icon.S;
			}
			
			Str.TMP.clear().add(credits);
			x = x1+width-128;
			SPRITES.icons().s.money.render(r, x, y1);
			GCOLOR.T().H1.bind();
			UI.FONT().S.render(r, Str.TMP, x+6+Icon.M, y1);
			COLOR.unbind();
			
			y1 += Icon.S + 4;
			
		}
		
		GCOLOR.T().NORMAL2.bind();
		if (rating != null)
		y1 += 8 + UI.FONT().M.render(r, rating, x1, y1, width, 1);
		if (attraction != null) {
			y1 += 8 + UI.FONT().M.render(r, attraction, x1, y1, width, 1);
			}
		if (service != null) {
			y1 += 8 + UI.FONT().M.render(r, service, x1, y1, width, 1);

		}
		if (inn != null){
			y1 += 8 + UI.FONT().M.render(r, inn, x1, y1, width, 1);

		}
		y1 += 8 + UI.FONT().M.render(r, "--------------------------- Extra Info -------------------------", x1, y1, width, 1);
		y1 += 8 + UI.FONT().M.render(r, "Rating score from 0 to 1 is based on the average of the following: " +score + "   " + ( Math.round(((attraction_score + service_score + inn_score) /3+score_rand) *100) /100.0), x1, y1, width, 1);
		y1 += 8 + UI.FONT().M.render(r, "Attraction score from 0 to 1 based on employment between 100-1100: " + (Math.round(attraction_score*100.0)/100.0), x1, y1, width, 1);
		y1 += 8 + UI.FONT().M.render(r, "Service score from 0 to 1 based on the service and some randomness: " + (Math.round(service_score*100.0)/100.0), x1, y1, width, 1);
		y1 += 8 + UI.FONT().M.render(r, "Inn quality from 0 to 1: " + (Math.round(inn_score*100.0)/100.0), x1, y1, width, 1);
		y1 += 8 + UI.FONT().M.render(r, "+-20% overall rating randomness:" + (Math.round(score_rand*100.0)/100.0), x1, y1, width, 1);


		GCOLOR.T().H2.bind();
		y1 += UI.FONT().S.render(r, name, x1+30, y1, width-30, 1);
		COLOR.unbind();
	
		
		
		return y1;
		
	}
	
	public void renderScore(SPRITE_RENDERER r, int cx, int y1) {
		
		{
			int icons = (int) (1 + 4*score);
			int x = icons*Icon.S/2;
			x = cx - x;
			for (int i = 0; i < icons; i++) {
				SPRITES.icons().s.star.render(r, x, y1);
				x+= Icon.S;
			}
		}
		
	}
	public void renderCred(SPRITE_RENDERER r, int x1, int y1) {
		
		Str.TMP.clear().add(credits);
		SPRITES.icons().s.money.render(r, x1, y1);
		GCOLOR.T().H1.bind();
		UI.FONT().S.render(r, Str.TMP, x1+6+Icon.M, y1);
		COLOR.unbind();
		
	}
	


	
}