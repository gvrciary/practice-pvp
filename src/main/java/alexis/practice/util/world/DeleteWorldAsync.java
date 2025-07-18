package alexis.practice.util.world;

import alexis.practice.util.Utils;
import cn.nukkit.scheduler.AsyncTask;

import java.io.File;

public class DeleteWorldAsync extends AsyncTask {

    private final String worldName;
    private final String directory;

    public DeleteWorldAsync(String worldName, String directory) {
        this.worldName = worldName;
        this.directory = directory;
    }

    @Override
    public void onRun() {
        String path = directory + File.separator + worldName;

        Utils.deleteSource(new File(path));
    }

}

