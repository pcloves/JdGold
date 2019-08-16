package FileWatcher;

import java.io.File;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FileWatcher extends Thread
{
    private File file;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public void setFile(File file) { this.file = file.getAbsoluteFile(); }
    File getFile() { return file; }
    public void stopWatch() { stop.set(true); }
    public abstract void doOnChange();

    @Override
    public void run() {
        try (WatchService watcher = FileSystems.getDefault().newWatchService())
        {
            final Path path = file.toPath().getParent();
            path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (!stop.get())
            {
                WatchKey key;
                try { key = watcher.poll(25, TimeUnit.MILLISECONDS); }
                catch (InterruptedException e) { return; }
                if (key == null) { Thread.yield(); continue; }

                for (WatchEvent<?> event : key.pollEvents())
                {
                    final WatchEvent.Kind<?> kind = event.kind();
                    final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        Thread.yield();
                        continue;
                    } else if (kind == java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
                            && filename.toString().equals(file.getName())) {
                        doOnChange();
                    }

                    final boolean valid = key.reset();
                    if (!valid) { break; }
                }
                Thread.yield();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}