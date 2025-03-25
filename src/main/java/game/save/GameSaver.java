package game.save;

import game.GAME;
import game.GameSpec;
import init.RES;
import init.paths.PATHS;
import init.text.D;
import snake2d.CORE;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import view.main.VIEW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GameSaver {

	private final KeyMap<Savable> smap = new KeyMap<Savable>();
	private final ArrayListGrower<Savable> before = new ArrayListGrower<Savable>();
	private final ArrayListGrower<Savable> all = new ArrayListGrower<Savable>();
	
	private final ArrayListGrower<ACTION_O<Path>> beforeSave = new ArrayListGrower<>();
	private final ArrayListGrower<ACTION_O<Path>> afterSave = new ArrayListGrower<>();
	private final ArrayListGrower<ACTION_O<Path>> beforeLoad = new ArrayListGrower<>();
	private final ArrayListGrower<ACTION_O<Path>> afterLoad = new ArrayListGrower<>();
	
	private final PROP prop = new PROP(this);
	private final AutoSaver auto = new AutoSaver(this);
	
	private double timeOfLastSave;
	private static CharSequence ¤¤save = "Saving";
	private static CharSequence ¤¤savingDisk = "Saving to disk, please wait.";
	static {
		D.ts(GameSaver.class);
	}
	
	public GameSaver(GAME game ){
		
		add(prop);
		timeOfLastSave = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
	}
	
	public Path save(String name) {
		
		return save(name, false);
	}
	
	public Path save(String name, boolean minified) {
		return save(PATHS.local().SAVE.get().resolve(name + PATHS.local().SAVE.fileEnding()), minified);
	}
	
	public Path save(Path path, boolean minified) {
		boolean succ = false;
		RES.loader().minify(minified, ¤¤save);
		RES.loader().init();
		RES.loader().print("Saving the world...");
		auto.reset();
		try {
			if (Files.exists(path)) {
				Files.delete(path);
			}
			FilePutter fp = new FilePutter(path, (1<<30));
			save(fp);
			CORE.checkIn();
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					RES.loader().print(¤¤savingDisk);
				}
			};
			a.exe();
			succ = fp.zip(a);
			timeOfLastSave = CORE.getUpdateInfo().getSecondsSinceFirstUpdate();
			System.gc();
			CORE.getInput().clearAllInput();
			RES.loader().minify(false, ¤¤save);
			auto.reset();
			System.gc();
			return succ ? path : null;
			
		}catch(Exception e) {
			e.printStackTrace();
			RES.loader().minify(false, ¤¤save);
			return null;
		}
		
	}
	
	private void save(FilePutter f) {
		for (ACTION_O<Path> a : beforeSave)
			a.exe(f.path);
	
		GameSpec.save(f);
		save(f, before);
		save(f, all);
		
		for (ACTION_O<Path> a : afterSave)
			a.exe(f.path);
		
	}
	
	private void save(FilePutter f, LIST<Savable> li) {
		f.i(li.size());
		for (Savable s : li) {
			String k = s.key;
			f.chars(k);
			int pos = f.getPosition();
			f.i(0);
			s.save(f);
			int le = f.getPosition()-pos-4;
			f.setAtPosition(pos, le);
		}
		
	}

	void load(FileGetter f) throws IOException {

		
		for (ACTION_O<Path> a : beforeLoad)
			a.exe(f.path);
	
		load(f, before);
		load(f, all);
		
		for (ACTION_O<Path> a : afterLoad)
			a.exe(f.path);
	}

	private void load(FileGetter f, LIST<Savable> li) throws IOException {
		
		KeyMap<Savable> map = new KeyMap<Savable>();
		for (Savable e : li) {
			map.put(e.key, e);
		}
		
		int am = f.i();
		for (int i = 0; i < am; i++) {
			String k = f.chars();
			int pos = f.getPosition()+f.i()+4;
			Savable e = map.get(k);
			if (e != null) {
				try {
					e.load(f);
				}catch(IOException ee) {
					LOG.ln(k + " " + f.getPosition() + " " + pos + " " + e.getClass().getSimpleName());
					f.setPosition(pos);
				}
				
				CORE.checkIn();
				if (f.getPosition() != pos) {
					LOG.ln(k + " " + f.getPosition() + " " + pos + " " + e.getClass().getSimpleName());
					f.setPosition(pos);
				}
			}else {
				LOG.ln("skipping " + k);
				f.setPosition(pos);
			}
		}
	}



	
	public double getTimeSinceLastSave(){
		return CORE.getUpdateInfo().getSecondsSinceFirstUpdate() - timeOfLastSave;
	}
	
	public void quicksave() {
		saveNamed("QuickSave", 3, true);
	}
	
	public void saveNew() {
		saveNamed("A New Beginning", 3, false);
	}
	
	public boolean saveNamed(String sname, int max, boolean mini) {
		if (!VIEW.canSave())
			return false;
		int am = 0;
		for (String s : PATHS.local().SAVE.getFiles()) {
			if (SaveFile.name(s).equals(sname)){
				am++;
			}
		}
		if (am >= max) {
			String least = null;
			for (String s : PATHS.local().SAVE.getFiles()) {
				if (SaveFile.name(s).equals(sname)){
					if (least == null || SaveFile.time(s) < SaveFile.time(least)) {
						least = s;
					}
				}
			}
			PATHS.local().SAVE.delete(least);
		}
		return save(SaveFile.stamp(sname), mini) != null;
	}



	public void autoSave(float ds) {
		auto.autosave(ds);
		
	}
	
	public void addSpecialSaver(Savable s) {
		before.add(s);
		smap.put(s.key, s);
	}
	
	public void add(Savable s) {
		all.add(s);
		smap.put(s.key, s);
	}
	
	public void onBeforeSave(ACTION_O<Path> a) {
		beforeSave.add(a);
	}
	
	public void onAfterSave(ACTION_O<Path> a) {
		afterSave.add(a);
	}
	
	public void onBeforeLoad(ACTION_O<Path> a) {
		beforeLoad.add(a);
	}
	
	public void onAfterLoad(ACTION_O<Path> a) {
		afterLoad.add(a);
	}
	
}
